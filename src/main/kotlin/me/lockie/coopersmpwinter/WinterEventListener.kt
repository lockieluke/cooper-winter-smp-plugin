package me.lockie.coopersmpwinter

import de.themoep.inventorygui.GuiElement
import de.themoep.inventorygui.GuiElement.Action
import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Damageable
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.io.path.nameWithoutExtension

class WinterEventListener(private val plugin: CooperSMPWinter, private val audioEngine: AudioEngine) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action.isRightClick && event.hand == EquipmentSlot.HAND && event.hasBlock() && !Objects.isNull(event.clickedBlock)) {
            val block = event.clickedBlock!!
            val player = event.player
            val itemInHand = player.inventory.itemInMainHand

            if (itemInHand.isDisc())
                return

            if (block.type == Material.JUKEBOX && !player.isSneaking) {
                event.isCancelled = true

                val audioSourceUUID = this.audioEngine.negotiateAudioSource(block)

                val audioFiles = this.audioEngine.listAudioFiles()
                var key = 'a'
                val rows = audioFiles.chunked(9) { chunk ->
                    chunk.map {
                        it.toString()
                    }.plus(List(9 - chunk.size) { " " }).joinToString("") { if (it == " ") it else {
                        key++
                        key.toString()
                    } }
                }.toMutableList()

                val lastRow = rows.last().toCharArray()
                lastRow[lastRow.lastIndexOf(' ')] = 'z'
                rows[rows.size - 1] = String(lastRow)
                val guiSetup: Array<String> = rows.toTypedArray()

                val playingAudioDefinition = this.audioEngine.getPlayingAudioDefinitionAtLocation(block.location)
                val gui = InventoryGui(this.plugin, player, if (playingAudioDefinition == null) "Speaker" else if (playingAudioDefinition.audioName == "") "Speaker" else "Speaker - Playing ${playingAudioDefinition.audioName}", guiSetup)
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
                                return@Action this.audioEngine.sendAudioStream(audioFile, audioSourceUUID)
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

                gui.addElement(StaticGuiElement('z', ItemStack(Material.BARRIER), Action { click ->
                    if (click.whoClicked.type == EntityType.PLAYER) {
                        this.audioEngine.sendStop(audioSourceUUID)
                        click.gui.close()
                        return@Action true
                    }
                    false
                }, "Stop"))

                gui.show(player)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDamageBlock(event: BlockDamageEvent) {
        val itemInHand = event.player.inventory.itemInMainHand
        val id = itemInHand.itemMeta.persistentDataContainer.get(
            requireNotNull(NamespacedKey.fromString("id")),
            PersistentDataType.STRING
        )
        if (!Objects.isNull(id) && id == "snow_shovel" && !event.block.type.toString()
                .contains("SNOW")
        ) event.isCancelled = true
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block

        if (block.type == Material.JUKEBOX) {
            this.audioEngine.removeAudioSourceAtLocation(block.location)
        }
    }

    //    @EventHandler
    //    public void onBlockPlaced(BlockPlaceEvent event) {
    //        Block placedBlock = event.getBlockPlaced();
    //        Material placedBlockType = placedBlock.getType();
    //
    //        if (placedBlockType.equals(Material.PLAYER_HEAD)) {
    //            ItemStack item = (ItemStack) placedBlock.getDrops().toArray()[0];
    //            SkullMeta meta = (SkullMeta) item.getItemMeta();
    //            ProfileProperty profileProperty = Objects.requireNonNull(meta.getPlayerProfile()).getProperties().stream().filter(p -> p.getName().equals("textures")).findFirst().orElse(null);
    //            String texture = Objects.requireNonNull(profileProperty).getValue();
    //            Location blockLocation = placedBlock.getLocation();
    //
    //            if (texture.equals(SkullTexture.HotChocolate)) {
    //                BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
    //                    Bukkit.getLogger().info("running");
    //                    World world = placedBlock.getWorld();
    //                    if (world.getBlockAt(blockLocation) == placedBlock)
    //                        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, blockLocation.add(0, 0.5, 0), 20, 0.1);
    //                }, 0, 100);
    //            }
    //        }
    //    }
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onSnowballHit(event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity
        if (!Objects.isNull(hitEntity) && hitEntity is Damageable) {
            hitEntity.damage(plugin.getConfig().getDouble("snowball-damage"))
        }
    }
}
