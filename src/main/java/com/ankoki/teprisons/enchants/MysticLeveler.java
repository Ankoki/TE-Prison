package com.ankoki.teprisons.enchants;

import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.EffFunctionCall;
import ch.njol.skript.lang.function.FunctionReference;
import ch.njol.skript.variables.Variables;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MysticLeveler extends EnchantHandler {

	private final TokenEnchantAPI api;
	private static final List<String> BLOCKED_WORLDS = new ArrayList<>(), UPGRADABLE_ENCHANTS = new ArrayList<>();
	private final Random random = new Random();
	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private boolean debug;
	private int upperBound;

	private static MysticLeveler instance;

	/**
	 * Gets the instance of KeyFinder, used for calculating block break events
	 * without heavy calls.
	 * @return the instance.
	 */
	public static MysticLeveler getInstance() {
		return instance;
	}

	public MysticLeveler(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
		instance = this;
		UPGRADABLE_ENCHANTS.add("FortuneTeller");
		UPGRADABLE_ENCHANTS.add("JackHammer");
		UPGRADABLE_ENCHANTS.add("TokenGreed");
		UPGRADABLE_ENCHANTS.add("KeyFinder");
		UPGRADABLE_ENCHANTS.add("Efficiency");
		this.api = api;
		this.loadConfig();
	}

	@Override
	public String getName() {
		return "MysticLeveler";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.debug = this.getConfig().getBoolean("dev-debug");
		Object object = this.getConfig().get("Enchants.MysticLeveler.upper-bound");
		try {
			this.upperBound = (int) object;
		} catch (NullPointerException | ClassCastException ex) { this.console.sendMessage("§cTE-Prison | this.upperBound not able to be cast from: " + object + " : loadConfig : MysticLeveler"); }
		List<String> list = this.getConfig().getStringList("Enchants.MysticLeveler.blocked-worlds");
		BLOCKED_WORLDS.clear();
		BLOCKED_WORLDS.addAll(list);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public void onBlockBreak(BlockBreakEvent event) {
		CompletableFuture.runAsync(() -> {
			Player player = event.getPlayer();
			int level = Misc.getEnchantmentLevel(player, "MysticLeveler");
			if (level <= 0)
				return;
			if (Misc.isBlocked(player)) {
				if (debug)
					this.console.sendMessage("§eTE-Prison | Misc.isBlocked(player) == true : BlockBreakEvent : MysticLeveler");
				return;
			}
			if (BLOCKED_WORLDS.contains(player.getWorld().getName())) {
				if (debug)
					this.console.sendMessage("§eTE-Prison | this.blockedWorlds.contains(player.getWorld()) == true : BlockBreakEvent : MysticLeveler");
				return;
			}
			if (this.upperBound <= 1)
				this.upperBound = 1000000;
			if (debug)
				this.console.sendMessage("§eTE-Prison | this.upperbound = " + this.upperBound + " : BlockBreakEvent : MysticLeveler");
			int random = this.random.nextInt(1, this.upperBound);
			if (random <= (1 + (level * 1.5))) {
				if (debug)
					this.console.sendMessage("§eTE-Prison | random <= 1 + (level * 2) == true : BlockBreakEvent : MysticLeveler");
				Collections.shuffle(UPGRADABLE_ENCHANTS);
				String enchant = UPGRADABLE_ENCHANTS.get(0);
				CEHandler ench = api.getEnchantment(enchant);
				int max = ench.getMaxLevel();
				int l = ench.getCELevel(player);
				int updated = max + l;
				if (updated > max) {
					if (debug)
						this.console.sendMessage("§eTE-Prison | updated [" + updated + "] > max [" + max + "] == true : BlockBreakEvent : MysticLeveler");
				} else {
					UUID uuid = player.getUniqueId();
					Variables.setVariable("byeol::prisons::" + uuid + "::enchants::" + enchant, updated, event, false);
					FunctionReference<?> reference = new SkriptParser("pickaxe_refreshPickaxe(player)", SkriptParser.ALL_FLAGS)
							.parseFunction((Class<?>[]) null);
					if (reference == null) {
						this.console.sendMessage("§cTE-Prison | reference == null : BlockBreakEvent : MysticLeveler");
						return;
					}
					new EffFunctionCall(reference).run(event);
				}
			}
		});
	}

}

