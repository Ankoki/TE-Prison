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

public class TokenGreed extends EnchantHandler {

	private final TokenEnchantAPI api;
	private final List<String> blockedWorlds = new ArrayList<>();
	private final Random random = new Random();
	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private boolean debug;
	private double defaultTokens;
	private double tokenIncrease;

	public TokenGreed(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
		this.api = api;
		this.loadConfig();
	}

	@Override
	public String getName() {
		return "TokenGreed";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.debug = this.getConfig().getBoolean("dev-debug");
		this.defaultTokens = this.getConfig().getDouble("Enchants.TokenGreed.default-tokens");
		this.tokenIncrease = this.getConfig().getDouble("Enchants.TokenGreed.token-increase");
		List<String> list = this.getConfig().getStringList("Enchants.TokenGreed.blocked-worlds");
		this.blockedWorlds.clear();
		this.blockedWorlds.addAll(list);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (this.blockedWorlds.contains(player.getWorld().getName())) {
			if (debug)
				this.console.sendMessage("§eTE-Prison | this.blockedWorlds.contains(player.getWorld()) == true : BlockBreakEvent : TokenGreed");
			return;
		}
		CEHandler handler = api.getEnchantment("TokenGreed");
		int level = handler.getCELevel(player);
		if (level <= 1) {
			if (debug)
				this.console.sendMessage("§eTE-Prison | level <= 1 && defaultTokens == " + defaultTokens + " == true : BlockBreakEvent : TokenGreed");
			this.api.addTokens(player, defaultTokens);
		} else {
			int above = (int) (tokenIncrease + (level * 3));
			int below = (int) (tokenIncrease - 1);
			int random = this.random.nextInt(below, above + 1);
			if (debug)
				this.console.sendMessage("§eTE-Prison | random == " + random + " && random + (level * 2) == " + random + (level * 2) + " == true : BlockBreakEvent : TokenGreed");
			this.api.addTokens(player, random);
		}
	}

}

