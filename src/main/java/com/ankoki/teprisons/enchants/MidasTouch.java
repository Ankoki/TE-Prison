package com.ankoki.teprisons.enchants;

import com.ankoki.teprisons.utils.Misc;
import com.vk2gpz.tokenenchant.api.EnchantHandler;
import com.vk2gpz.tokenenchant.api.InvalidTokenEnchantException;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import com.vk2gpz.tokenenchant.event.EventPriorityHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MidasTouch extends EnchantHandler {

	private final Random random = new Random();
	private final List<String> commands = new ArrayList<>();
	private int chance;
	private int commandAmount;
	private final List<String> blockedWorlds = new ArrayList<>();
	private final List<Block> midasBlocks = new ArrayList<>();

	public MidasTouch(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
	}

	@Override
	public String getName() {
		return "MidasTouch";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.commands.clear();
		this.commands.addAll(this.getConfig().getStringList("Enchants.MidasTouch.commands"));
		this.blockedWorlds.clear();
		this.blockedWorlds.addAll(this.getConfig().getStringList("Enchants.MidasTouch.blocked-worlds"));
		this.chance = this.getConfig().getInt("Enchants.MidasTouch.chance");
		this.commandAmount = this.getConfig().getInt("Enchants.MidasTouch.command-amount");
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		int level = Misc.getEnchantmentLevel(player, "MidasTouch");
		if (level <= 0)
			return;
		Block block = event.getBlock();
		if (this.blockedWorlds.contains(player.getWorld().getName()))
			return;
		if (this.midasBlocks.contains(block)) {
			this.midasBlocks.remove(block);
			this.runCommands(player);
			return;
		}
		int chance = this.random.nextInt(1, this.chance);
		if (chance >= 3 + (level * 2)) {
			if (Misc.getEnchantmentLevel(player, "OrionsBlessing") == 1) {
				this.runCommands(player);
				return;
			}
			event.setCancelled(true);
			block.setType(Material.SPONGE);
			this.midasBlocks.add(block);
		}
	}

	/**
	 * Runs the midas commands for the given player.
	 *
	 * @param player the player.
	 */
	private void runCommands(Player player) {
		Collections.shuffle(this.commands);
		for (int i = 0; i < Math.min(this.commands.size(), this.commandAmount); i++) {
			String command = this.commands.get(i);
			if (command.startsWith("[CONSOLE] "))
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
						.replaceFirst("[CONSOLE] ", "")
						.replace("{PLAYER}", player.getName()));
			else
				player.performCommand(command
						.replace("{PLAYER}", player.getName()));
		}
	}

}
