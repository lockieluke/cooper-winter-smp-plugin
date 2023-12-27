package me.lockie.coopersmpwinter

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.Messenger
import org.bukkit.scheduler.BukkitTask
import java.nio.file.Path
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.*

@Serializable
data class AudioSource(val x: Int, val y: Int, val z: Int, val world: String, val uuid: String, val global: Boolean = false)

@Serializable
data class AudioStreamDefinition(val audioName: String, val audioPacketsCount: Int, val audioUUID: String, val audioSize: Int, val audioSourceUUID: String)

@OptIn(ExperimentalPathApi::class, ExperimentalEncodingApi::class)
class AudioEngine(private val plugin: Plugin) {

    companion object {
        const val AUDIO_PLAYBACK_CHANNEL = "coopersmpwinter:audio_playback"

        fun getAudioFileDurationInSeconds(audioFile: Path): Int {
            val ffprobe = ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", audioFile.absolutePathString())
                .redirectErrorStream(true)
                .start()
            ffprobe.waitFor()
            val duration = ffprobe.inputStream.bufferedReader().readLine().toDouble()
            ffprobe.destroy()

            return duration.toInt()
        }

    }

    private var audioSources = arrayOf<AudioSource>()
    private var audioDefinitions = arrayOf<AudioStreamDefinition>()
    private val audioPath = Path(this.plugin.dataFolder.absolutePath, "audio")

    private val timers: MutableMap<String, BukkitTask> = mutableMapOf()
    private val queues: MutableMap<String, Queue<Path>> = mutableMapOf()

    fun installFFProbe() {
        try {
            this.plugin.logger.info("Checking for ffprobe")
            val ffprobe = ProcessBuilder("ffprobe", "-version")
                .redirectErrorStream(true)
                .start()
            ffprobe.waitFor()
            val version = ffprobe.inputStream.bufferedReader().readLine()
            ffprobe.destroy()

            this.plugin.logger.info("Found ffprobe $version")
        } catch (e: Exception) {
            this.plugin.logger.info("Installing ffprobe")
            val ffprobeInstaller = ProcessBuilder("apt", "install", "ffmpeg")
                .redirectErrorStream(true)
                .start()
            ffprobeInstaller.waitFor()
            ffprobeInstaller.destroy()

            val output = ffprobeInstaller.inputStream.bufferedReader().readLine()

            if (ffprobeInstaller.exitValue() == 0)
                this.plugin.logger.info("Installed ffprobe")
            else
                this.plugin.logger.info("Failed to install ffprobe $output")
        }

    }

    fun listAudioFiles(): List<Path> {
        if (!this.plugin.dataFolder.exists())
            this.plugin.dataFolder.mkdir()

        if (!this.audioPath.exists())
            this.audioPath.createDirectory()

        return this.audioPath.walk().filter { it.isRegularFile() && (it.extension == "mp3" || it.extension == "wav") }
            .toList()
    }

    fun removeAudioSourceAtLocation(location: Location) {
        val uuidOfReplacingAudioSource = this.audioSources.first { it.x == location.blockX && it.y == location.blockY && it.z == location.blockZ && it.world == location.world!!.name }.uuid
        this.requestRemoveAudioSource(uuidOfReplacingAudioSource)
        this.audioSources = this.audioSources.filter { it.uuid != uuidOfReplacingAudioSource }.toTypedArray()
        this.audioDefinitions = this.audioDefinitions.filter { it.audioSourceUUID != uuidOfReplacingAudioSource }.toTypedArray()
        this.cancelTimer(uuidOfReplacingAudioSource)
    }

    private fun requestRemoveAudioSource(uuid: String) {
        val audioSource = this.audioSources.first { it.uuid == uuid }
        this.plugin.server.onlinePlayers.forEach { player ->
            player.sendPluginMessage(
                this.plugin,
                AUDIO_PLAYBACK_CHANNEL,
                "REMOVE_AUDIO_SOURCE ${Json.encodeToString(audioSource)}".encodeToByteArray()
            )
        }
    }

    private fun cancelTimer(audioSourceUUID: String) {
        this.timers[audioSourceUUID]?.cancel()
        this.timers.remove(audioSourceUUID)
    }

    private fun doesAudioSourceExistAtLocation(location: Location): Boolean {
        return this.audioSources.any { it.x == location.blockX && it.y == location.blockY && it.z == location.blockZ && it.world == location.world!!.name }
    }

    fun negotiateAudioSource(block: Block): String {
        val alreadyExists = this.doesAudioSourceExistAtLocation(block.location)
        val audioSourceUUID = if (alreadyExists) this.audioSources.first { it.x == block.x && it.y == block.y && it.z == block.z && it.world == block.world.name }.uuid else UUID.randomUUID().toString()
        val audioSource = AudioSource(block.x, block.y, block.z, block.world.name, audioSourceUUID)

        this.plugin.logger.info("Negotiating audio source $audioSourceUUID")
        this.plugin.server.onlinePlayers.forEach { player ->
            player.sendPluginMessage(
                this.plugin,
                AUDIO_PLAYBACK_CHANNEL,
                "NEGOTIATE_AUDIO_SOURCE ${Json.encodeToString(audioSource)}".encodeToByteArray()
            )
        }

        if (!alreadyExists)
            this.audioSources += audioSource

        return audioSourceUUID
    }

    fun isSpeakerGlobal(audioSourceUUID: String): Boolean {
        return this.audioSources.first { it.uuid == audioSourceUUID }.global
    }

    fun resetGlobalSpeakers() {
        this.audioSources = this.audioSources.map { it.copy(global = false) }.toTypedArray()
        this.plugin.server.onlinePlayers.forEach { player ->
            player.sendPluginMessage(
                this.plugin,
                AUDIO_PLAYBACK_CHANNEL,
                "RESET_GLOBAL_SPEAKERS".encodeToByteArray()
            )
        }
    }

    fun makeSpeakerGlobal(audioSourceUUID: String) {
        this.resetGlobalSpeakers()
        this.audioSources = this.audioSources.map { it.copy(global = it.uuid == audioSourceUUID) }.toTypedArray()
        this.plugin.server.onlinePlayers.forEach { player ->
            player.sendPluginMessage(
                this.plugin,
                AUDIO_PLAYBACK_CHANNEL,
                "MAKE_SPEAKER_GLOBAL $audioSourceUUID".encodeToByteArray()
            )
        }
    }

    fun sendStop(audioSourceUUID: String) {
        this.plugin.server.onlinePlayers.forEach { player ->
            this.audioDefinitions = this.audioDefinitions.filter { it.audioSourceUUID != audioSourceUUID }.toTypedArray()

            player.sendPluginMessage(
                this.plugin,
                AUDIO_PLAYBACK_CHANNEL,
                "STOP_AUDIO_STREAM $audioSourceUUID".encodeToByteArray()
            )
        }

        this.clearQueue(audioSourceUUID)
        this.cancelTimer(audioSourceUUID)
    }

    fun stopAll() {
        this.audioSources.forEach { this.sendStop(it.uuid) }
    }

    fun addAudioToQueue(audioFile: Path, audioSourceUUID: String) {
        if (!this.queues.containsKey(audioSourceUUID))
            this.queues[audioSourceUUID] = LinkedList()

        this.queues[audioSourceUUID]?.add(audioFile)
    }

    private fun clearQueue(audioSourceUUID: String) {
        this.queues[audioSourceUUID]?.clear()
    }

    fun hasNextInQueue(audioSourceUUID: String): Boolean {
        return this.queues.containsKey(audioSourceUUID) && this.queues[audioSourceUUID]?.isNotEmpty() == true
    }

    private fun playNextInQueue(audioSourceUUID: String) {
        val nextAudioFile = this.queues[audioSourceUUID]?.poll()
        if (nextAudioFile != null)
            this.sendAudioStream(nextAudioFile, audioSourceUUID)
    }

    fun getNextInQueue(audioSourceUUID: String): Path? {
        return this.queues[audioSourceUUID]?.peek()
    }

    fun isPlaying(audioSourceUUID: String): Boolean {
        return this.audioDefinitions.any { it.audioSourceUUID == audioSourceUUID }
    }

    private fun defineAudioStream(audioStreamDefinition: AudioStreamDefinition) {
        this.plugin.server.onlinePlayers.forEach { player ->
            player.sendPluginMessage(
                this.plugin,
                AUDIO_PLAYBACK_CHANNEL,
                "DEFINE_AUDIO_STREAM ${Json.encodeToString(audioStreamDefinition)}".encodeToByteArray()
            )
        }

        this.audioDefinitions += audioStreamDefinition
    }

    fun sendAudioStream(audioFile: Path, audioSourceUUID: String): Boolean {
        return this.sendAudioStream(audioFile.readBytes(), audioFile.fileName.toString(), audioSourceUUID)
    }

    private fun sendAudioStream(audioBuffer: ByteArray, audioName: String, audioSourceUUID: String): Boolean {
        val audioRequestUUID = UUID.randomUUID()

        val fillerBytesSize = "COMMAND_FILLER_BYTES $audioRequestUUID 1000 ".encodeToByteArray().size
        val partSize = Messenger.MAX_MESSAGE_SIZE - fillerBytesSize
        val parts = Base64.encode(audioBuffer).chunked(partSize)
        if (parts.size > 1000) {
            this.plugin.logger.info("${parts.size} is too many packets to send")
            return false
        }

        this.plugin.server.onlinePlayers.forEach { player ->
            this.defineAudioStream(
                AudioStreamDefinition(
                    audioName,
                    parts.size,
                    audioRequestUUID.toString(),
                    audioBuffer.size,
                    audioSourceUUID
                )
            )

            parts.forEachIndexed { index, part ->
//            this.plugin.logger.info("Sending audio stream $audioRequestUUID with index $index, audio packet hash is ${Base64.decode(part).contentHashCode()}")
                val payload = "SEND_AUDIO_STREAM $audioRequestUUID $index $part".encodeToByteArray()
                if (payload.size > Messenger.MAX_MESSAGE_SIZE) {
                    player.sendMessage("Payload size is too large! ${payload.size} > ${Messenger.MAX_MESSAGE_SIZE}")
                    return false
                }

                player.sendPluginMessage(
                    this.plugin,
                    AUDIO_PLAYBACK_CHANNEL,
                    payload
                )
            }
        }

        val realSeconds = getAudioFileDurationInSeconds(Path(this.audioPath.absolutePathString(), audioName))
        val seconds = realSeconds + (this.plugin.config.getInt("song-delay", 2))

        this.plugin.logger.info("Playing audio $audioName for $realSeconds seconds, with a buffer of $seconds seconds")
        this.timers[audioSourceUUID] = Bukkit.getScheduler().runTaskLater(this.plugin, Runnable {
            this.audioDefinitions = this.audioDefinitions.filter { it.audioUUID != audioRequestUUID.toString() }.toTypedArray()
            this.cancelTimer(audioSourceUUID)

            if (this.hasNextInQueue(audioSourceUUID))
                this.playNextInQueue(audioSourceUUID)
            else
                this.sendStop(audioSourceUUID)
        }, (seconds * 20).toLong())

        return true
    }

    fun getPlayingAudioDefinitionAtLocation(location: Location): AudioStreamDefinition? {
        val audioSource = this.audioSources.firstOrNull { it.x == location.blockX && it.y == location.blockY && it.z == location.blockZ && it.world == location.world!!.name }
        if (audioSource == null)
            return null

        return this.audioDefinitions.firstOrNull { it.audioSourceUUID == audioSource.uuid }
    }

    fun saveAudioFile(filename: String, audioBuffer: ByteArray) {
        val audioPath = Path(this.plugin.dataFolder.absolutePath, "audio")
        if (!this.audioPath.exists())
            this.audioPath.createDirectory()

        val audioFile = Path(audioPath.absolutePathString(), filename)
        if (audioFile.exists())
            audioFile.deleteIfExists()

        audioFile.writeBytes(audioBuffer)
    }

}