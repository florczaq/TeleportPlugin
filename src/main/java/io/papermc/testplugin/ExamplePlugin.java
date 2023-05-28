package io.papermc.testplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unused")
public class ExamplePlugin extends JavaPlugin implements Listener, CommandExecutor {
	private final Map<String, List<Location>> savedTeleports = new HashMap<>();
	private final Map<String, List<String>> savedTeleportsNames = new HashMap<>();

	private final String key_cfg = "stmf";

	@Override
	public void onEnable() {
		getServer()
			.getPluginManager()
			.registerEvents(this, this);

		saveDefaultConfig();

		if (getConfig().contains(key_cfg))
			loadSavedTeleports();
	}

	@Override
	public void onDisable() {
		if (!savedTeleports.isEmpty() && !savedTeleportsNames.isEmpty())
			saveTeleportsToConfig();
	}

	public void loadSavedTeleports() {
		Objects.requireNonNull(
				getConfig()
					.getConfigurationSection(key_cfg)
			)
			.getKeys(false)
			.forEach(
				key -> {
					@SuppressWarnings("unchecked")
					List<Location> locations = (List<Location>) getConfig().get(key_cfg + "." + key);
					savedTeleports.put(key, locations);

					@SuppressWarnings("unchecked")
					List<String> names = (List<String>) getConfig().get(key_cfg + "_names." + key);
					savedTeleportsNames.put(key, names);
				}
			);
	}

	public void saveTeleportsToConfig() {
		for (Map.Entry<String, List<Location>> element : savedTeleports.entrySet())
			getConfig().set(key_cfg + "." + element.getKey(), element.getValue());

		for (Map.Entry<String, List<String>> element : savedTeleportsNames.entrySet())
			getConfig().set(key_cfg + "_names." + element.getKey(), element.getValue());

		saveConfig();
	}

	public void addTeleportToList(@NotNull Player player, @NotNull String name, @NotNull Location teleportLocation) {
		String uid = player.getUniqueId().toString();

		if (savedTeleports.get(uid) == null || savedTeleportsNames.get(uid) == null) {
			savedTeleports.put(uid, new ArrayList<>());
			savedTeleportsNames.put(uid, new ArrayList<>());

		}
		savedTeleports.get(uid).add(teleportLocation);
		savedTeleportsNames.get(uid).add(name);
		player.sendMessage(createText(String.format("Success! Teleport [%s] has been saved.",name)));
	}

	private Component createText(String text) {
		return Component
			.text(text)
			.color(TextColor.color(0, 255, 255));

	}

	private Component teleportText(Player player, String name, Location location) {
		return Component.text("[" + name + "]")
			.color(TextColor.color(0, 255, 255))
			.decorate(TextDecoration.BOLD)
			.decorate(TextDecoration.ITALIC)
			.hoverEvent(
				HoverEvent.hoverEvent(
					HoverEvent.Action.SHOW_TEXT,
					Component.text("Teleport there")
				)
			)
			.clickEvent(
				ClickEvent.clickEvent(
					ClickEvent.Action.RUN_COMMAND,
					String.format(
						"/tp %s %s %s %s",
						player.getName(),
						location.getX(),
						location.getY(),
						location.getZ()
					)
				)
			);
	}

	public void sendPlayerListOfTeleports(@NotNull Player player) {
		String uid = player.getUniqueId().toString();
		List<Component> messages = new ArrayList<>();

		List<String> playerTeleportsNames = savedTeleportsNames.get(uid);
		List<Location> playerTeleportsLocations = savedTeleports.get(uid);
		if (playerTeleportsNames == null || playerTeleportsLocations == null) {
			player.sendMessage(createText("You don't have any teleports yet"));
			return;
		}

		messages.add(createText("-------------"));
		messages.add(createText("# Teleports #"));
		messages.add(createText("-------------"));


		for (int i = 0; i < playerTeleportsNames.size(); i++) {
			messages.add(teleportText(player, playerTeleportsNames.get(i), playerTeleportsLocations.get(i)));
		}
		messages.add(createText("-------------"));

		for (Component m : messages)
			player.sendMessage(m);

	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (sender instanceof Player player) {
			switch (command.getLabel()) {
				case "settp":
					addTeleportToList(player, args[0], player.getLocation());
					break;
				case "tpl":
					sendPlayerListOfTeleports(player);
					break;
				case "deltp":
					break;
			}
		} else sender.sendMessage("Only players can use those commands!");
		return false;
	}

}
