package com.ankoki.teprisons.enchants;

import com.ankoki.teprisons.utils.Misc;
import com.vk2gpz.tokenenchant.api.CEHandler;
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

public class KeyFinder extends EnchantHandler {

	private final TokenEnchantAPI api;
	private final List<String> blockedWorlds = new ArrayList<>(), commands = new ArrayList<>();
	private final Random random = new Random();
	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private boolean debug;
	private int upperBound;

	public KeyFinder(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
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
		this.commands.clear();
		this.commands.addAll(this.getConfig().getStringList("Enchants.KeyFinder.commands"));
		List<String> list = this.getConfig().getStringList("Enchants.KeyFinder.blocked-worlds");
		this.blockedWorlds.clear();
		this.blockedWorlds.addAll(list);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (Misc.isBlocked(player)) {
			if (debug)
				this.console.sendMessage("§eTE-Prison | Misc.isBlocked(player) == true : BlockBreakEvent : KeyFinder");
			return;
		}
		if (this.blockedWorlds.contains(player.getWorld().getName())) {
			if (debug)
				this.console.sendMessage("§eTE-Prison | this.blockedWorlds.contains(player.getWorld()) == true : BlockBreakEvent : KeyFinder");
			return;
		}
		CEHandler handler = api.getEnchantment("KeyFinder");
		int level = handler.getCELevel(player);
		if (this.upperBound <= 1)
			this.upperBound = 20000;
		if (debug)
			this.console.sendMessage("§eTE-Prison | this.upperbound = " + this.upperBound + " : BlockBreakEvent : KeyFinder");
		int random = this.random.nextInt(1, this.upperBound);
		if (random <= (1 + (level * 2))) {
			if (debug)
				this.console.sendMessage("§eTE-Prison | random <= 1 + (level * 2) == true : BlockBreakEvent : KeyFinder");
			for (String command : this.commands) {
				String execute = command.replace("{PLAYER}", player.getName());
				if (debug)
					this.console.sendMessage("§eTE-Prison | command " + execute + " executed : BlockBreakEvent : KeyFinder");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), execute);
			}
		}
	}

}

