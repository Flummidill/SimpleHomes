package com.flummidill.simplehomes;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportTaskAdmin {

    private final SimpleHomes plugin;
    private final Player player;
    private final Location targetLocation;
    private final String targetPlayerName;
    private final int homeNum;

    private int secondsLeft;
    private Location startLocation;
    private BukkitRunnable task;

    public TeleportTaskAdmin(SimpleHomes plugin, Player player, Location targetLocation, int seconds, int homeNum, String targetPlayerName) {
        this.plugin = plugin;
        this.player = player;
        this.targetLocation = targetLocation;
        this.secondsLeft = seconds;
        this.homeNum = homeNum;
        this.targetPlayerName = targetPlayerName;
    }

    public void start() {
        startLocation = player.getLocation().getBlock().getLocation();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location current = player.getLocation().getBlock().getLocation();
                if (!current.equals(startLocation)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§cTeleport cancelled! You moved!"));
                    cancel();
                    return;
                }

                if (secondsLeft <= 0) {
                    player.teleport(targetLocation);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§aTeleported to Home " + homeNum + " of " + targetPlayerName + "!"));
                    cancel();
                    return;
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6Teleporting in §a" + secondsLeft + " §6Seconds!"));
                secondsLeft--;
            }
        };

        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }
}