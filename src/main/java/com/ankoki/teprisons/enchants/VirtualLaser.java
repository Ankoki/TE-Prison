package com.ankoki.teprisons.enchants;

import com.ankoki.teprisons.utils.Misc;
import com.destroystokyo.paper.ParticleBuilder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.vk2gpz.tokenenchant.api.EnchantHandler;
import com.vk2gpz.tokenenchant.api.InvalidTokenEnchantException;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import com.vk2gpz.tokenenchant.event.EventPriorityHandler;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class VirtualLaser extends EnchantHandler {

	private static VirtualLaser instance;
	private final List<String> BLOCKED_WORLDS = new ArrayList<>();
	private final Random random = new Random();
	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private Economy economy;
	private Color colour;
	private float size;
	private boolean debug;
	private RegionContainer container;
	private boolean disabled;

	public VirtualLaser(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
		instance = this;
		try {
			this.container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		} catch (Exception ex) { console.sendMessage("§cTE-Prison | WorldGuard not found. VirtualLaser will not work, and is made for A-Z mines."); }
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
			console.sendMessage("§cTE-Prison | Vault was not found. VirtualLaser will not work.");
			disabled = true;
		} else {
			RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
			if (rsp == null) {
				console.sendMessage("§cTE-Prison | RegisteredServiceProvider<Economy> was not found. VirtualLaser will not work.");
				disabled = true;
			} else {
				this.economy = rsp.getProvider();
				this.loadConfig();
			}
		}
	}

	/**
	 * Gets the instance of TokenGreed, used for calculating block break events
	 * without heavy calls.
	 *
	 * @return the instance.
	 */
	public static VirtualLaser getInstance() {
		return instance;
	}

	@Override
	public String getName() {
		return "VirtualLaser";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.debug = this.getConfig().getBoolean("dev-debug");
		List<String> list = this.getConfig().getStringList("Enchants.VirtualLaser.blocked-worlds");
		this.BLOCKED_WORLDS.clear();
		this.BLOCKED_WORLDS.addAll(list);
		String colour = this.getConfig().getString("Enchants.VirtualLaser.colour");
		String[] split = colour.split(", ");
		List<Integer> ints = new ArrayList<>();
		for (String unparsed : split) {
			int i = Misc.parseInt(unparsed);
			if (i < 0)
				this.console.sendMessage("§cTE-Prison | Config value of 'colour' [" + unparsed + "] is not a recognised integer. Please use the format colour: \"106, 255, 0\"");
			else
				ints.add(i);
		}
		if (ints.size() != 3)
			this.colour = Color.fromRGB(106, 255, 0);
		else
			this.colour = Color.fromRGB(ints.get(0), ints.get(1), ints.get(2));
		this.size = (float) this.getConfig().getDouble("Enchants.VirtualLaser.size");
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		int level = Misc.getEnchantmentLevel(player, "VirtualLaser");
		if (level <= 0)
			return;
		if (this.disabled) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | this.disabled == true : BlockBreakEvent : VirtualLaser");
			return;
		}
		Block block = event.getBlock();
		World world = event.getBlock().getWorld();
		if (this.container == null) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | this.container == null: BlockBreakEvent : VirtualLaser");
			return;
		}
		if (BLOCKED_WORLDS.contains(player.getWorld().getName())) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | this.blockedWorlds.contains(player.getWorld()) == true : BlockBreakEvent : VirtualLaser");
			return;
		}
		if (Misc.isBlocked(player)) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | Misc.isBlocked(player) == true : BlockBreakEvent : VirtualLaser");
			return;
		}
		RegionQuery query = this.container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));
		if (set == null) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | query.getApplicableRegions(BukkitAdapter.adapt(block.getLocation())) = null : BlockBreakEvent : VirtualLaser");
			return;
		}
		for (ProtectedRegion region : set.getRegions()) {
			if (!"abcdefghijklmnopqrstuvwxyz".contains(region.getId())) {
				if (this.debug)
					this.console.sendMessage("§eTE-Prison | \"abcdefghijklmnopqrstuvwxyz\".contains(region.getId()) = false : BlockBreakEvent : VirtualLaser");
				continue;
			}
			int random = this.random.nextInt(1, 12000);
			if (random <= 1 + (level * 2)) {
				if (this.debug)
					this.console.sendMessage("§eTE-Prison | random <= 1 + (level * 2) == true : BlockBreakEvent : VirtualLaser");
				Location pointOne = BukkitAdapter.adapt(world, region.getMinimumPoint());
				Location pointTwo = BukkitAdapter.adapt(world, region.getMaximumPoint());
				List<Block> blocks = Misc.getLaserBlocks(block.getLocation(), pointOne, pointTwo);
				Misc.applyEnchants(player, blocks);
				this.economy.depositPlayer(player, Misc.getValue(player, blocks));
				CompletableFuture.runAsync(() -> {
					List<Location> particles = new ArrayList<>();
					for (Block b : blocks) {
						if (b.getType() != Material.AIR) {
							if (this.debug)
								this.console.sendMessage("§eTE-Prison | Location[x:" + b.getX() + "|y:" + b.getY() + "|z:" + b.getZ() + "] : BlockBreakEvent : VirtualLaser");
							particles.addAll(Misc.getHollowCube(b.getLocation(), 0.5));
						}
					}
					ParticleBuilder builder = new ParticleBuilder(Particle.REDSTONE);
					builder.color(this.colour, this.size);
					builder.receivers(player);
					for (Location location : particles)
						builder.location(location).spawn();
					if (this.debug)
						this.console.sendMessage("§eTE-Prison | All applied.");
				});
			} else if (this.debug)
				this.console.sendMessage("§eTE-Prison | random <= 1 + (level * 2) == false : BlockBreakEvent : VirtualLaser");
		}
	}

}

