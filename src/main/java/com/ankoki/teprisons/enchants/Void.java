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

public class Void extends EnchantHandler {

	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private final Random random = new Random();
	private final List<String> blockedWorlds = new ArrayList<>();
	private RegionContainer container;
	private boolean debug, disabled;
	private Economy economy;
	private TokenEnchantAPI api;

	public Void(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
		this.api = api;
		try {
			this.container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		} catch (Exception ex) {
			console.sendMessage("§cTE-Prison | WorldGuard not found. Void will not work, and is made for A-Z mines.");
		}
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
		this.loadConfig();
	}

	@Override
	public String getName() {
		return "Void";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.debug = this.getConfig().getBoolean("dev-debug");
		List<String> list = this.getConfig().getStringList("Enchants.Void.blocked-worlds");
		this.blockedWorlds.clear();
		this.blockedWorlds.addAll(list);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	private void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		int level = Misc.getEnchantmentLevel(player, "Void");
		if (level <= 0)
			return;
		if (this.container == null) {
			return;
		}
		if (Misc.isBlocked(player)) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | Misc.isBlocked(event.getPlayer()) == true : BlockBreakEvent : Void");
			return;
		}
		Block block = event.getBlock();
		World world = block.getWorld();
		if (this.blockedWorlds.contains(world.getName())) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | this.blockedWorlds.contains(world) = true : BlockBreakEvent : Void");
			return;
		}
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));
		if (set == null) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | query.getApplicableRegions(BukkitAdapter.adapt(block.getLocation())) = null : BlockBreakEvent : Void");
			return;
		}
		for (ProtectedRegion region : set.getRegions()) {
			if (!Misc.isMine(region.getId())) {
				if (this.debug)
					this.console.sendMessage("§eTE-Prison | \"abcdefghijklmnopqrstuvwxyz\".contains(region.getId()) = false : BlockBreakEvent : Void");
				continue;
			}
			int random = this.random.nextInt(0, 35000);
			if (random <= 50) {
				this.console.sendMessage("§eTE-Prison | Player[" + player.getName() + "] procced Void : BlockBreakEvent : Void");
				Location pointOne = BukkitAdapter.adapt(world, region.getMinimumPoint());
				Location pointTwo = BukkitAdapter.adapt(world, region.getMaximumPoint());
				double fortuneLevel = Misc.getEnchantmentLevel(player, "FortuneTeller");
				double tgLevel = Misc.getEnchantmentLevel(player, "TokenGreed");
				List<Block> blocks = Misc.getBlocks(pointOne, pointTwo);
				double amount = Misc.getValue(player, blocks);
				blocks.removeIf(b -> b.getType() == Material.AIR);
				if (fortuneLevel > 0)
					amount *= 1 + (fortuneLevel / 5D);
				double tgAmount = 1;
				if (tgLevel > 0)
					tgAmount = (3 + (tgLevel * 6)) * blocks.size();
				this.economy.depositPlayer(player, amount);
				this.api.addTokens(player, tgAmount);
				player.sendActionBar("§x§A§A§5§5§D§EYOU HAVE VIRTUALLY ABSORBED THE MINE.");
				CompletableFuture.runAsync(() -> {
					List<Location> particles = Misc.getHollowCube(pointOne, pointTwo, 1.0D);
					ParticleBuilder builder = new ParticleBuilder(Particle.REDSTONE);
					builder.color(org.bukkit.Color.fromRGB(106, 255, 0), 2F);
					builder.receivers(player);
					for (Location location : particles)
						builder.location(location).spawn();
				});
				/**
				 try (EditSession editSession = Fawe.instance().getWorldEdit().newEditSession(BukkitAdapter.adapt(world))) {
				 CuboidRegion reg = new CuboidRegion(BukkitAdapter.adapt(world),
				 BlockVector3.at(pointOne.getX(), pointOne.getY(), pointOne.getZ()),
				 BlockVector3.at(pointTwo.getX(), pointTwo.getY(), pointTwo.getZ()));
				 CompletableFuture.runAsync(() -> {
				 BlockBreakEvent e;
				 for (Block b : blocks) {
				 e = new BlockBreakEvent(b, player);
				 FortuneTeller.getInstance().onBlockBreak(e);
				 KeyFinder.getInstance().onBlockBreak(e);
				 TokenGreed.getInstance().onBlockBreak(e);
				 MysticLeveler.getInstance().onBlockBreak(e);
				 }
				 }).thenRun(() -> {
				 editSession.setBlocks((Region) reg, BlockTypes.AIR);
				 editSession.flushQueue();
				 });
				 }
				 Misc.unblock(player);
				 PrisonMines mines = (PrisonMines) Prison.get().getModuleManager().getModule(PrisonMines.MODULE_NAME);
				 SpigotPlayer sPlayer = new SpigotPlayer(player);
				 Mine mine = mines.findMineLocation(sPlayer);
				 if (mine == null) {
				 if (this.debug)
				 this.console.sendMessage("§eTE-Prison | mine == null : BlockBreakEvent : Void");
				 break;
				 }
				 mine.checkZeroBlockReset();
				 mine.submitMineSweeperTask();*/
			}
			break;
		}
	}

}