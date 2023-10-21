package com.ankoki.teprisons.enchants;

import com.ankoki.teprisons.utils.Misc;
import com.fastasyncworldedit.core.Fawe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.vk2gpz.tokenenchant.api.EnchantHandler;
import com.vk2gpz.tokenenchant.api.InvalidTokenEnchantException;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import com.vk2gpz.tokenenchant.event.EventPriorityHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import tech.mcprison.prison.Prison;
import tech.mcprison.prison.mines.PrisonMines;
import tech.mcprison.prison.mines.data.Mine;
import tech.mcprison.prison.spigot.game.SpigotPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Void extends EnchantHandler {

	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private final Random random = new Random();
	private final List<String> blockedWorlds = new ArrayList<>();
	private RegionContainer container;
	private boolean debug;

	public Void(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
		try {
			this.container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		} catch (Exception ex) { console.sendMessage("§cTE-Prison | WorldGuard not found. Void will not work, and is made for A-Z mines."); }
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
		Player player = event.getPlayer();
		int level = Misc.getEnchantmentLevel(player, "Void");
		if (level <= 0)
			return;
		if (this.container == null) {
			if (this.debug)
				this.console.sendMessage("§eTE-Prison | this.container == null: BlockBreakEvent : Void");
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
			if (!"abcdefghijklmnopqrstuvwxyz".contains(region.getId())) {
				if (this.debug)
					this.console.sendMessage("§eTE-Prison | \"abcdefghijklmnopqrstuvwxyz\".contains(region.getId()) = false : BlockBreakEvent : Void");
				continue;
			}
			int random = this.random.nextInt(0, 10000);
			if (random <= 100) {
				Location pointOne = BukkitAdapter.adapt(world, region.getMinimumPoint());
				Location pointTwo = BukkitAdapter.adapt(world, region.getMaximumPoint());
				Misc.block(player);
				List<Block> blocks = Misc.getBlocks(pointOne, pointTwo);
				BlockBreakEvent e;
				for (Block b : blocks) {
					e = new BlockBreakEvent(b, player);
					FortuneTeller.getInstance().onBlockBreak(e);
					KeyFinder.getInstance().onBlockBreak(e);
					TokenGreed.getInstance().onBlockBreak(e);
					MysticLeveler.getInstance().onBlockBreak(event);
				}
				try (EditSession editSession = Fawe.instance().getWorldEdit().newEditSession(BukkitAdapter.adapt(world))) {
					CuboidRegion reg = new CuboidRegion(BukkitAdapter.adapt(world),
							BlockVector3.at(pointOne.getX(), pointOne.getY(), pointOne.getZ()),
							BlockVector3.at(pointTwo.getX(), pointTwo.getY(), pointTwo.getZ()));
					editSession.setBlocks((Region) reg, BlockTypes.AIR);
					editSession.flushQueue();
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
				mine.submitMineSweeperTask();
			}
			break;
		}
	}

}
