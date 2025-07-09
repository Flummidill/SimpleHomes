package com.flummidill.simplehomes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.ChatColor;

public class JoinListener implements Listener {

    private boolean sendUpdateNotification = false;

    public void setUpdateAvailable(boolean updateAvailable) {
        this.sendUpdateNotification = updateAvailable;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (sendUpdateNotification && player.hasPermission("simplehomes.admin")) {
            // Prefix
            TextComponent prefix = new TextComponent();

            TextComponent prefixPart1 = new TextComponent("[");
            prefixPart1.setColor(ChatColor.RED);
            prefixPart1.setBold(true);
            prefix.addExtra(prefixPart1);

            TextComponent prefixPart2 = new TextComponent("Simple");
            prefixPart2.setColor(ChatColor.GREEN);
            prefixPart2.setBold(true);
            prefix.addExtra(prefixPart2);

            TextComponent prefixPart3 = new TextComponent("Homes");
            prefixPart3.setColor(ChatColor.BLUE);
            prefixPart3.setBold(true);
            prefix.addExtra(prefixPart3);

            TextComponent prefixPart4 = new TextComponent("]");
            prefixPart4.setColor(ChatColor.RED);
            prefixPart4.setBold(true);
            prefix.addExtra(prefixPart4);

            TextComponent prefixPart5 = new TextComponent(" ");
            prefixPart5.setColor(ChatColor.WHITE);
            prefixPart5.setBold(false);
            prefix.addExtra(prefixPart5);


            // Message 1
            TextComponent message1 = new TextComponent();

            message1.addExtra(prefix);

            TextComponent Text1 = new TextComponent("A new Update is available!");
            Text1.setColor(ChatColor.GOLD);
            message1.addExtra(Text1);

            player.spigot().sendMessage(message1);


            // Message 2
            TextComponent message2 = new TextComponent();

            message2.addExtra(prefix);

            TextComponent Text2 = new TextComponent("Click here to Download it!");
            Text2.setColor(ChatColor.AQUA);
            Text2.setUnderlined(true);
            Text2.setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL,
                "https://github.com/Flummidill/SimpleHomes/releases/latest"
            ));
            message2.addExtra(Text2);

            player.spigot().sendMessage(message2);
        }
    }
}