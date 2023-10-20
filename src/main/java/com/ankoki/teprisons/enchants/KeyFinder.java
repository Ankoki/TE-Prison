package com.ankoki.teprisons.enchants;

import com.ankoki.teprisons.utils.Misc;
import com.vk2gpz.tokenenchant.api.EnchantHandler;
import com.vk2gpz.tokenenchant.api.InvalidTokenEnchantException;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import com.vk2gpz.tokenenchant.event.EventPriorityHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class KeyFinder extends EnchantHandler {

	private final TokenEnchantAPI api;
	private static final List<String> BLOCKED_WORLDS = new ArrayList<>(), COMMANDS = new ArrayList<>();
	private final Random random = new Random();
	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private boolean debug;
	private int upperBound;

	private static KeyFinder instance;

	/**
	 * Gets the instance of KeyFinder, used for calculating block break events
	 * without heavy calls.
	 * @return the instance.
	 */
	public static KeyFinder getInstance() {
		return instance;
	}

	public KeyFinder(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
		instance = this;
		this.api = api;
		this.loadConfig();
	}

	@Override
	public String getName() {
		return "KeyFinder";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.debug = this.getConfig().getBoolean("dev-debug");
		Object object = this.getConfig().get("Enchants.KeyFinder.upper-bound");
		try {
			this.upperBound = (int) object;
		} catch (NullPointerException | ClassCastException ex) { this.console.sendMessage("§cTE-Prison | this.upperBound not able to be cast from: " + object + " : loadConfig : KeyFinder"); }
		COMMANDS.clear();
		COMMANDS.addAll(this.getConfig().getStringList("Enchants.KeyFinder.commands"));
		List<String> list = this.getConfig().getStringList("Enchants.KeyFinder.blocked-worlds");
		BLOCKED_WORLDS.clear();
		BLOCKED_WORLDS.addAll(list);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public void onBlockBreak(BlockBreakEvent event) {
		CompletableFuture.runAsync(() -> {
			Player player = event.getPlayer();
			int level = Misc.getEnchantmentLevel(player, "KeyFinder");
			if (level <= 0)
				return;
			if (Misc.isBlocked(player)) {
				if (debug)
					this.console.sendMessage("§eTE-Prison | Misc.isBlocked(player) == true : BlockBreakEvent : KeyFinder");
				return;
			}
			if (BLOCKED_WORLDS.contains(player.getWorld().getName())) {
				if (debug)
					this.console.sendMessage("§eTE-Prison | this.blockedWorlds.contains(player.getWorld()) == true : BlockBreakEvent : KeyFinder");
				return;
			}
			if (this.upperBound <= 1)
				this.upperBound = 20000;
			if (debug)
				this.console.sendMessage("§eTE-Prison | this.upperbound = " + this.upperBound + " : BlockBreakEvent : KeyFinder");
			int random = this.random.nextInt(1, this.upperBound);
			if (random <= (1 + (level * 1.5))) {
				if (debug)
					this.console.sendMessage("§eTE-Prison | random <= 1 + (level * 2) == true : BlockBreakEvent : KeyFinder");
				Bukkit.getScheduler().runTask(TokenEnchantAPI.getInstance(), () -> {
					for (String command : COMMANDS) {
						String execute = command.replace("{PLAYER}", player.getName());
						if (debug)
							this.console.sendMessage("§eTE-Prison | command " + execute + " executed : BlockBreakEvent : KeyFinder");
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), execute);
					}
				});
			}
		});
	}

}

