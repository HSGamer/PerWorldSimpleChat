package me.hsgamer.perworldsimplechat;

import me.clip.placeholderapi.PlaceholderAPI;
import me.hsgamer.hscore.bukkit.baseplugin.BasePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.stream.Collectors;

public final class PerWorldSimpleChat extends BasePlugin implements Listener {

    private final Map<String, String> nameWithFormat = new HashMap<>();
    private final List<String> enabledWorld = new ArrayList<>();

    private static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    @Override
    public void enable() {
        registerListener(this);
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();

        config.options().copyDefaults(true);
        for (World world : Bukkit.getWorlds()) {
            config.addDefault("format." + world.getName(), "&f%s: &7%s");
        }
        config.addDefault("enabled-worlds", Bukkit.getWorlds().stream().map(World::getName).collect(
                Collectors.toList()));

        config.getConfigurationSection("format").getValues(false)
                .forEach((k, v) -> nameWithFormat.put(k, String.valueOf(v)));
        enabledWorld.addAll(config.getStringList("enabled-worlds"));

        saveConfig();
    }

    @Override
    public void disable() {
        nameWithFormat.clear();
        enabledWorld.clear();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        String worldName = world.getName();
        UUID worldUID = world.getUID();

        if (!enabledWorld.contains(worldName)) {
            return;
        }

        Set<Player> recipients = event.getRecipients();
        recipients.removeIf(player1 -> !player1.getWorld().getUID().equals(worldUID));

        if (nameWithFormat.containsKey(worldName)) {
            event.setFormat(
                    colorize(PlaceholderAPI.setPlaceholders(player, nameWithFormat.get(worldName))));
        }
    }
}
