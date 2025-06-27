package com.flummidill.simplehomes;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandHandler implements CommandExecutor {

    private final SimpleHomes plugin;
    private final HomeManager manager;

    private final Map<UUID, TeleportTask> teleportTasks = new HashMap<>();
    private final Map<UUID, TeleportTaskAdmin> teleportTasksAdmin = new HashMap<>();

    public CommandHandler(SimpleHomes plugin, HomeManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;

        String cmd = command.getName().toLowerCase();

        manager.saveOfflinePlayer(player.getName(), player.getUniqueId());

        switch (cmd) {
            case "sethome":
                if (!player.hasPermission("simplehomes.use")) {
                    player.sendMessage("§cYou don’t have permission to use this command.");
                    return true;
                }
                if (args.length != 1) {
                    player.sendMessage("Usage: /sethome <number>");
                    return true;
                }
                return handleSetHome(player, args[0]);

            case "home":
                if (!player.hasPermission("simplehomes.use")) {
                    player.sendMessage("§cYou don’t have permission to use this command.");
                    return true;
                }
                if (args.length != 1) {
                    player.sendMessage("Usage: /home <number>");
                    return true;
                }
                return handleHome(player, args[0]);

            case "delhome":
                if (!player.hasPermission("simplehomes.use")) {
                    player.sendMessage("§cYou don’t have permission to use this command.");
                    return true;
                }
                if (args.length != 1) {
                    player.sendMessage("Usage: /delhome <number>");
                    return true;
                }
                return handleDelHome(player, args[0]);

            case "homeadmin":
                if (!player.hasPermission("simplehomes.admin")) {
                    player.sendMessage("§cYou don’t have permission to use this command.");
                    return true;
                }
                if (args.length != 3) {
                    player.sendMessage("Usage: /homeadmin <sethome|home|delhome|maxhomes> <player> <number>");
                    return true;
                }
                return handleHomeAdmin(player, args);

            default:
                return false;
        }
    }

    private boolean handleSetHome(Player player, String arg) {
        UUID uuid = player.getUniqueId();
        int maxHomes = manager.getMaxHomes(uuid);

        int homeNum;
        try {
            homeNum = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            player.sendMessage("§cHome number must be a valid integer.");
            return true;
        }

        if (!player.hasPermission("simplehomes.admin")) {
            if (homeNum < 1 || homeNum > maxHomes) {
                player.sendMessage("§cYou can only set homes between 1 and " + maxHomes + ".");
                return true;
            }
        }

        if (manager.homeExists(uuid, homeNum)) {
            player.sendMessage("§cHome " + homeNum + " already exists.");
            return true;
        } else {
            manager.setHome(uuid, homeNum, player.getLocation());
            player.sendMessage("§aHome " + homeNum + " set!");
        }
        return true;
    }

    private boolean handleHome(Player player, String arg) {
        UUID uuid = player.getUniqueId();
        int maxHomes = manager.getMaxHomes(uuid);

        int homeNum;
        try {
            homeNum = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            player.sendMessage("§cHome number must be a valid integer.");
            return true;
        }

        if (!player.hasPermission("simplehomes.admin")) {
            if (homeNum < 1 || homeNum > maxHomes) {
                player.sendMessage("§cYou can only use homes between 1 and " + maxHomes + ".");
                return true;
            }
        }

        if (!manager.homeExists(uuid, homeNum)) {
            player.sendMessage("§cHome " + homeNum + " does not exist.");
            return true;
        }

        // Cancel any ongoing Teleport Tasks for Player
        cancelTeleport(player);

        TeleportTask task = new TeleportTask(plugin, player, manager.getHome(uuid, homeNum), 5, homeNum);
        task.start();

        teleportTasks.put(player.getUniqueId(), task);

        return true;
    }

    private boolean handleDelHome(Player player, String arg) {
        UUID uuid = player.getUniqueId();
        int maxHomes = manager.getMaxHomes(uuid);

        int homeNum;
        try {
            homeNum = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            player.sendMessage("§cHome number must be a valid integer.");
            return true;
        }

        if (!player.hasPermission("simplehomes.admin")) {
            if (homeNum < 1 || homeNum > maxHomes) {
                player.sendMessage("§cYou can only delete homes between 1 and " + maxHomes + ".");
                return true;
            }
        }

        if (!manager.homeExists(uuid, homeNum)) {
            player.sendMessage("§cHome " + homeNum + " does not exist.");
            return true;
        }

        manager.deleteHome(uuid, homeNum);
        player.sendMessage("§aHome " + homeNum + " deleted!");
        return true;
    }

    private boolean handleHomeAdmin(Player sender, String[] args) {
        String action = args[0].toLowerCase();
        String targetName = args[1];
        String numberStr = args[2];
        UUID targetUUID = null;

        Player target = Bukkit.getPlayerExact(targetName);
        if (target != null) {
            targetUUID = target.getUniqueId();
        } else if (manager.getOfflinePlayerUUID(targetName) != null) {
            targetUUID = manager.getOfflinePlayerUUID(targetName);
        } else {
            sender.sendMessage("§cPlayer '" + targetName + "' not found.");
            return true;
        }

        targetName = manager.getOfflinePlayerName(targetUUID);

        int number;
        try {
            number = Integer.parseInt(numberStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cNumber must be a valid integer.");
            return true;
        }

        switch (action) {
            case "sethome":
                if (manager.homeExists(targetUUID, number)) {
                    sender.sendMessage("§cHome " + number + " already exists for " + targetName + ".");
                    return true;
                } else {
                    manager.setHome(targetUUID, number, sender.getLocation());
                    sender.sendMessage("§aSet Home " + number + " of " + targetName + ".");
                }
                break;

            case "home":
                Location loc = manager.getHome(targetUUID, number);
                if (loc == null) {
                    sender.sendMessage("§cHome " + number + " does not exist for " + targetName + ".");
                    return true;
                }
                
                // Cancel any ongoing Teleport Tasks for Player
                cancelTeleport(sender);
                TeleportTaskAdmin task = new TeleportTaskAdmin(plugin, sender, loc, 5, number, targetName);
                task.start();
                teleportTasksAdmin.put(sender.getUniqueId(), task);
                break;

            case "delhome":
                if (!manager.homeExists(targetUUID, number)) {
                    sender.sendMessage("§cHome " + number + " does not exist for " + targetName + ".");
                    return true;
                }
                manager.deleteHome(targetUUID, number);
                sender.sendMessage("§aDeleted Home " + number + " of " + targetName + ".");
                break;

            case "maxhomes":
                if (number < 1 || number > 50) {
                    sender.sendMessage("§cNumber must be between 1 and 50.");
                    return true;
                }
                manager.setMaxHomes(targetUUID, number);
                sender.sendMessage("§aSet max homes for " + targetName + " to " + number + ".");
                break;

            default:
                sender.sendMessage("§cUsage: /homeadmin <sethome|home|delhome|maxhomes> <player> <number>");
                return true;
        }

        return true;
    }

    private void cancelTeleport(Player player) {
        TeleportTask task = teleportTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        TeleportTaskAdmin taskAdmin = teleportTasksAdmin.remove(player.getUniqueId());
        if (taskAdmin != null) {
            taskAdmin.cancel();
        }
    }
}