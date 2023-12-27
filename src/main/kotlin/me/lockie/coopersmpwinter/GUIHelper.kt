package me.lockie.coopersmpwinter

import de.themoep.inventorygui.GuiElement
import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import de.themoep.inventorygui.GuiElement.Action
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

class GUIHelper(private val plugin: CooperSMPWinter, private val audioEngine: AudioEngine) {

    fun openGUIAndPickAudioFile(player: Player, audioSourceBlock: Block, addPlaybackControl: Boolean = true, onSelectAudioFile: ((audioFile: Path, audioSourceUUID: String) -> Unit)? = null) {
        val audioSourceUUID = this.audioEngine.negotiateAudioSource(audioSourceBlock)

        val audioFiles = this.audioEngine.listAudioFiles()
        val stopKey = '0'
        val globalKey = '1'
        val queueKey = '2'
        val numberOfActionButtons = if (addPlaybackControl) 3 else 0

        var key = 'a'
        val rows = audioFiles.chunked(9) { chunk ->
            chunk.map {
                it.toString()
            }.plus(List(9 - chunk.size) { " " }).joinToString("") { if (it == " ") it else {
                key++
                key.toString()
            } }
        }.toMutableList()

        if (addPlaybackControl) {
            var lastRow = rows.last().toCharArray()
            if (lastRow.filter { it == ' ' }.size < numberOfActionButtons) {
                rows.add(" ".repeat(9))
                lastRow = rows.last().toCharArray()
            }
            lastRow[lastRow.lastIndexOf(' ')] = globalKey
            lastRow[lastRow.lastIndexOf(' ')] = stopKey
            lastRow[lastRow.lastIndexOf(' ')] = queueKey
            rows[rows.size - 1] = String(lastRow)
        }

        val guiSetup: Array<String> = rows.toTypedArray()

        val playingAudioDefinition = this.audioEngine.getPlayingAudioDefinitionAtLocation(audioSourceBlock.location)
        val gui = InventoryGui(this.plugin, player, if (playingAudioDefinition == null) "Speaker" else if (!this.audioEngine.isPlaying(audioSourceUUID)) "Speaker" else "Speaker - Playing ${playingAudioDefinition.audioName}", guiSetup)
        var guiElements = arrayOf<GuiElement>()

        key = 'a'
        audioFiles.forEach { audioFile ->
            val item = ItemStack(Material.MUSIC_DISC_13)
            item.editMeta { meta ->
                meta.displayName(Component.text(audioFile.nameWithoutExtension))
                meta.lore(listOf(Component.space()))
            }

            key++
            guiElements += StaticGuiElement(
                key,
                item,
                1,
                Action { click ->
                    if (click.whoClicked.type == EntityType.PLAYER) {
                        click.gui.close()
                        if (addPlaybackControl)
                            return@Action this.audioEngine.sendAudioStream(audioFile, audioSourceUUID)

                        onSelectAudioFile?.invoke(audioFile, audioSourceUUID)
                        return@Action true
                    }

                    false
                }
            )
        }

        gui.closeAction = InventoryGui.CloseAction { close ->
            close.gui.close()
            true
        }

        guiElements.forEach { gui.addElement(it) }

        if (addPlaybackControl) {
            gui.addElement(StaticGuiElement(stopKey, ItemStack(Material.BARRIER), Action { click ->
                if (click.whoClicked.type == EntityType.PLAYER) {
                    this.audioEngine.sendStop(audioSourceUUID)
                    click.gui.close()
                    return@Action true
                }
                false
            }, "Stop"))

            val isSpeakerGlobal = this.audioEngine.isSpeakerGlobal(audioSourceUUID)
            gui.addElement(StaticGuiElement(globalKey, ItemStack(Material.GOAT_HORN), Action { click ->
                if (click.whoClicked.type == EntityType.PLAYER) {
                    if (isSpeakerGlobal) this.audioEngine.resetGlobalSpeakers() else this.audioEngine.makeSpeakerGlobal(audioSourceUUID)
                    player.sendMessage(
                        Component.text(if (isSpeakerGlobal) "Speaker is no longer global" else "Speaker is now global").color(
                            NamedTextColor.YELLOW))
                    click.gui.close()
                    return@Action true
                }
                false
            }, if (isSpeakerGlobal) "Reset global speaker status" else "Make speaker global"))

            gui.addElement(StaticGuiElement(queueKey, ItemStack(Material.JUKEBOX), Action { click ->
                if (click.whoClicked.type == EntityType.PLAYER) {
                    if (this.audioEngine.isPlaying(audioSourceUUID))
                        this.openGUIAndPickAudioFile(player, audioSourceBlock, false) { audioFile, audioSourceUUID ->
                            this.audioEngine.addAudioToQueue(audioFile, audioSourceUUID)
                            player.sendMessage(Component.text("Added ${audioFile.nameWithoutExtension} to queue").color(NamedTextColor.YELLOW))
                        }

                    click.gui.close()
                    return@Action true
                }
                false
            }, "Queue", if (this.audioEngine.hasNextInQueue(audioSourceUUID)) "Next: ${this.audioEngine.getNextInQueue(audioSourceUUID)?.nameWithoutExtension}" else "No audio queued"))
        }

        gui.show(player)
    }

}