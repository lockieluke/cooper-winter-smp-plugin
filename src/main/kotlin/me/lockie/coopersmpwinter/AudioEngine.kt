package me.lockie.coopersmpwinter

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.Messenger
import java.nio.file.Path
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.*

@Serializable
data class AudioSource(val x: Int, val y: Int, val z: Int, val world: String, val uuid: String)

@Serializable
data class AudioStreamDefinition(val audioName: String, val audioPacketsCount: Int, val audioUUID: String, val audioSize: Int, val audioSourceUUID: String)

@OptIn(ExperimentalPathApi::class, ExperimentalEncodingApi::class)
class AudioEngine(private val plugin: Plugin) {

    companion object {
        const val AUDIO_PLAYBACK_CHANNEL = "coopersmpwinter:audio_playback"
    }

    private var audioSources = arrayOf<AudioSource>()
    private var audioDefinitions = arrayOf<AudioStreamDefinition>()
    private val audioPath = Path(this.plugin.dataFolder.absolutePath, "audio")

    fun listAudioFiles(): List<Path> {
        if (!this.plugin.dataFolder.exists())
            this.plugin.dataFolder.mkdir()

        if (!this.audioPath.exists())
            this.audioPath.createDirectory();

        return this.audioPath.walk().filter { it.isRegularFile() && (it.extension == "mp3" || it.extension == "wav") }
            .toList()
    }

    fun removeAudioSourceAtLocation(location: Location) {
        val uuidOfReplacingAudioSource = this.audioSources.first { it.x == location.blockX && it.y == location.blockY && it.z == location.blockZ && it.world == location.world!!.name }.uuid
        this.requestRemoveAudioSource(uuidOfReplacingAudioSource)
        this.audioSources = this.audioSources.filter { it.uuid != uuidOfReplacingAudioSource }.toTypedArray()
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

        this.audioSources = this.audioSources.filter { it.uuid != uuid }.toTypedArray()
    }

    fun doesAudioSourceExistAtLocation(location: Location): Boolean {
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

    fun sendStop(audioSourceUUID: String) {
        this.plugin.server.onlinePlayers.forEach { player ->
            player.sendPluginMessage(
                this.plugin,
                AUDIO_PLAYBACK_CHANNEL,
                "STOP_AUDIO_STREAM $audioSourceUUID".encodeToByteArray()
            )
        }
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

        return true
    }

    fun saveAudioFile(filename: String, audioBuffer: ByteArray) {
        val audioPath = Path(this.plugin.dataFolder.absolutePath, "audio")
        if (!this.audioPath.exists())
            this.audioPath.createDirectory();

        val audioFile = Path(audioPath.absolutePathString(), filename)
        if (audioFile.exists())
            audioFile.deleteIfExists()

        audioFile.writeBytes(audioBuffer)
    }

}