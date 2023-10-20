package com.ankoki.teprisons.enchants;

import com.ankoki.teprisons.utils.Misc;
import com.vk2gpz.tokenenchant.api.EnchantHandler;
import com.vk2gpz.tokenenchant.api.InvalidTokenEnchantException;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import com.vk2gpz.tokenenchant.event.EventPriorityHandler;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;

public class FortuneTeller extends EnchantHandler {

	private static final List<String> BLOCKED_WORLDS = new ArrayList<>();
	private static FortuneTeller instance;
	private final TokenEnchantAPI api;
	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private Economy economy;
	private boolean debug;
	private boolean disabled = false;

	public FortuneTeller(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
		instance = this;
		this.api = api;
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
			console.sendMessage("§cTE-Prison | Vault was not found. FortuneTeller will not work.");
			disabled = true;
		} else {
			RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
			if (rsp == null) {
				console.sendMessage("§cTE-Prison | RegisteredServiceProvider<Economy> was not found. FortuneTeller will not work.");
				disabled = true;
			} else {
				this.economy = rsp.getProvider();
				this.loadConfig();
			}
		}
	}

	/**
	 * Gets the instance of FortuneTeller, used for calculating block break events
	 * without heavy calls.
	 *
	 * @return the instance.
	 */
	public static FortuneTeller getInstance() {
		return instance;
	}

	@Override
	public String getName() {
		return "FortuneTeller";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.debug = this.getConfig().getBoolean("dev-debug");
		List<String> list = this.getConfig().getStringList("Enchants.Fortune.blocked-worlds");
		BLOCKED_WORLDS.clear();
		BLOCKED_WORLDS.addAll(list);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public void onBlockBreak(BlockBreakEvent event) {
		if (this.disabled) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | this.disabled == true : BlockBreakEvent : FortuneTeller");
			return;
		}
		Player player = event.getPlayer();
		int level = Misc.getEnchantmentLevel(player, "FortuneTeller");
		if (level <= 0)
			return;
		if (BLOCKED_WORLDS.contains(player.getWorld().getName())) {
			if (debug)
				this.console.sendMessage("§eTE-Prison | this.blockedWorlds.contains(player.getWorld()) == true : BlockBreakEvent : FortuneTeller");
			return;
		}
		double amount = Misc.getValue(player, List.of(event.getBlock()));
		if (debug)
			this.console.sendMessage("§eTE-Prison | Misc.getValue(player, List.of(event.getBlock()) = " + amount + ": BlockBreakEvent : FortuneTeller");
		amount *= 1 + (level / 10D);
		if (debug)
			this.console.sendMessage("§eTE-Prison | amount *= 1 + (level / 10D) = " + amount + ": BlockBreakEvent : FortuneTeller");
		this.economy.depositPlayer(player, amount);
	}

}

