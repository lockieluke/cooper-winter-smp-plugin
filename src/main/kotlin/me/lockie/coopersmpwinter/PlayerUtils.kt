package me.lockie.coopersmpwinter

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

class PlayerUtils {

    companion object {
        val scoreboardHide = Bukkit.getScoreboardManager().newScoreboard
        val teamHide = scoreboardHide.registerNewTeam("hide_name_tag")

        val bossBarHide = Bukkit.createBossBar(Component.text("Your nametag is hidden").color(NamedTextColor.YELLOW).content(), org.bukkit.boss.BarColor.RED, org.bukkit.boss.BarStyle.SOLID)

        fun initHideNameTag() {
            this.teamHide.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
        }
    }

}

fun Player.revertHideNameTag() {
    PlayerUtils.teamHide.removePlayer(this)
    PlayerUtils.bossBarHide.removePlayer(this)
    this.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
}

fun Player.hideNameTag() {
    PlayerUtils.teamHide.addPlayer(this)
    PlayerUtils.bossBarHide.addPlayer(this)
    this.scoreboard = PlayerUtils.scoreboardHide
}