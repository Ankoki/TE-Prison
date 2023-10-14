package com.ankoki.teprisons.enchants;

import com.ankoki.teprisons.utils.Misc;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.vk2gpz.tokenenchant.api.CEHandler;
import com.vk2gpz.tokenenchant.api.EnchantHandler;
import com.vk2gpz.tokenenchant.api.InvalidTokenEnchantException;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import com.vk2gpz.tokenenchant.event.EventPriorityHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JackHammer extends EnchantHandler {

	private final TokenEnchantAPI api;
	private final Random random = new Random();
	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private RegionContainer container;

	// Configuration
	private boolean debug;
	private final List<String> blockedWorlds = new ArrayList<>();

	public JackHammer(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
		this.api = api;
		try {
			this.container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		} catch (Exception ex) { console.sendMessage("§cTE-Prison | WorldGuard not found. JackHammer will not work, and is made for A-Z mines."); }
		this.loadConfig();
	}

	@Override
	public String getName() {
		return "JackHammer";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.debug = this.getConfig().getBoolean("dev-debug");
		List<String> list = this.getConfig().getStringList("Enchants.JackHammer.blocked-worlds");
		this.blockedWorlds.clear();
		this.blockedWorlds.addAll(list);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	private void onBlockBreak(BlockBreakEvent event) {
		if (this.container == null) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | this.container == null: BlockBreakEvent : JackHammer");
			return;
		}
		Player player = event.getPlayer();
		if (Misc.isBlocked(player)) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | Misc.isBlocked(event.getPlayer()) == true : BlockBreakEvent : JackHammer");
			return;
		}
		Block block = event.getBlock();
		World world = block.getWorld();
		if (this.blockedWorlds.contains(world.getName())) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | this.blockedWorlds.contains(world) = true : BlockBreakEvent : JackHammer");
			return;
		}
		CEHandler handler = api.getEnchantment("JackHammer");
		int level = handler.getCELevel(player);
		if (level <= 0) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | handler.getCELevel(event.getPlayer()) <= 0 : BlockBreakEvent : JackHammer");
			return;
		}
		RegionQuery query = this.container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));
		if (set == null) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | query.getApplicableRegions(BukkitAdapter.adapt(block.getLocation())) = null : BlockBreakEvent : JackHammer");
			return;
		}
		for (ProtectedRegion region : set.getRegions()) {
			if (!"abcdefghijklmnopqrstuvwxyz".contains(region.getId())) {
				if (this.debug)
					this.console.sendMessage("§eTE-Prison | \"abcdefghijklmnopqrstuvwxyz\".contains(region.getId()) = false : BlockBreakEvent : JackHammer");
				continue;
			}
			int random = this.random.nextInt(1, 10000);
			if (random <= 1 + (level * 2)) {
				Location pointOne = BukkitAdapter.adapt(world, region.getMinimumPoint());
				Location pointTwo = BukkitAdapter.adapt(world, region.getMaximumPoint());
				pointOne.setY(block.getY());
				pointTwo.setY(block.getY());
				Misc.block(player);
				for (Block b : Misc.getBlocks(pointOne, pointTwo)) {
					if (player.isOnline() && b.getType() != Material.AIR)
						player.breakBlock(b);
				}
				Misc.unblock(player);
			}
			break;
		}
	}

}