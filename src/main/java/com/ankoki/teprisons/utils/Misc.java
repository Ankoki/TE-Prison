package com.ankoki.teprisons.utils;

import com.ankoki.teprisons.enchants.FortuneTeller;
import com.ankoki.teprisons.enchants.KeyFinder;
import com.ankoki.teprisons.enchants.MysticLeveler;
import com.ankoki.teprisons.enchants.TokenGreed;
import com.vk2gpz.tokenenchant.api.CEHandler;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import tech.mcprison.prison.cryptomorin.xseries.XMaterial;
import tech.mcprison.prison.spigot.sellall.SellAllUtil;

import java.util.*;

public class Misc {

	private static final List<Player> blocked = new ArrayList<>();
	private static final TokenEnchantAPI api = TokenEnchantAPI.getInstance();
	private static final List<String> mines = new ArrayList<>();

	static {
		mines.addAll(Arrays.asList("abcdefghijklmnopqrstuvwxyz".split("")));
		mines.add("void");
		mines.add("p10");
		mines.add("p25");
		mines.add("p50");
		mines.add("p100");
		mines.add("p150");
	}

	/**
	 * Checks if a given string is a registered mine.
	 *
	 * @param name the name.
	 * @return true if a mine.
	 */
	public static boolean isMine(String name) {
		return mines.contains(name);
	}

	/**
	 * Blocks a player from using automated block breaking tasks.
	 *
	 * @param player the player to block.
	 */
	public static void block(Player player) {
		if (!blocked.contains(player))
			blocked.add(player);
	}

	/**
	 * Allows a player to use automated block break task.
	 *
	 * @param player the player to unblock.
	 */
	public static void unblock(Player player) {
		blocked.remove(player);
	}

	/**
	 * Whether a player is blocked from using automated block break tasks.
	 *
	 * @param player the player to check.
	 * @return true if blocked.
	 */
	public static boolean isBlocked(Player player) {
		return blocked.contains(player);
	}

	/**
	 * Attempts to parse a string into an integer.
	 *
	 * @param string the string to parse.
	 * @return the parsed integer, or -1 if not able to do so.
	 */
	public static int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException ex) { return -1; }
	}

	/**
	 * Parses a string list into a list of worlds.
	 *
	 * @param strings the strings to transform.
	 * @return the worlds.
	 */
	public static List<World> parseWorlds(Collection<String> strings) {
		List<World> worlds = new ArrayList<>();
		for (String name : strings) {
			World world = Bukkit.getWorld(name);
			if (world != null)
				worlds.add(world);
			else
				Bukkit.getConsoleSender().sendMessage("§cTE-Prison | World named '" + name + "' was not found. Skipping.");
		}
		return worlds;
	}

	/**
	 * Gets a list of blocks from one location to another.
	 *
	 * @param pointOne the first point.
	 * @param pointTwo the second point.
	 * @return the blocks between the two points.
	 */
	public static List<Block> getBlocks(Location pointOne, Location pointTwo) {
		List<Block> blocks = new ArrayList<>();
		int maxX = Math.max(pointOne.getBlockX(), pointTwo.getBlockX());
		int minX = Math.min(pointOne.getBlockX(), pointTwo.getBlockX());
		int maxY = Math.max(pointOne.getBlockY(), pointTwo.getBlockY());
		int minY = Math.min(pointOne.getBlockY(), pointTwo.getBlockY());
		int maxZ = Math.max(pointOne.getBlockZ(), pointTwo.getBlockZ());
		int minZ = Math.min(pointOne.getBlockZ(), pointTwo.getBlockZ());
		World world = pointOne.getWorld();
		for (int x = minX; x <= maxX; x++)
			for (int z = minZ; z <= maxZ; z++)
				for (int y = minY; y <= maxY; y++)
					blocks.add(world.getBlockAt(x, y, z));
		return blocks;
	}

	/**
	 * Executes all enchants.
	 * Used by Void and JackHammer.
	 *
	 * @param player the player.
	 * @param blocks the blocks.
	 */
	public static void applyEnchants(Player player, List<Block> blocks) {
		BlockBreakEvent event;
		for (Block block : blocks) {
			event = new BlockBreakEvent(block, player);
			FortuneTeller.getInstance().onBlockBreak(event);
			KeyFinder.getInstance().onBlockBreak(event);
			TokenGreed.getInstance().onBlockBreak(event);
			MysticLeveler.getInstance().onBlockBreak(event);
		}
	}

	/**
	 * Gets blocks which would be obtained through a laser.
	 *
	 * @param start the block that proceed this enchants.
	 * @param pointOne the smallest point of this mine.
	 * @param pointTwo the largest point of this mine.
	 * @return the blocks to be affected.
	 */
	public static List<Block> getLaserBlocks(Location start, Location pointOne, Location pointTwo) {
		List<Block> blocks = new ArrayList<>();
		int maxX = Math.max(pointOne.getBlockX(), pointTwo.getBlockX());
		int minX = Math.min(pointOne.getBlockX(), pointTwo.getBlockX());
		int maxY = Math.max(pointOne.getBlockY(), pointTwo.getBlockY());
		int minY = Math.min(pointOne.getBlockY(), pointTwo.getBlockY());
		int maxZ = Math.max(pointOne.getBlockZ(), pointTwo.getBlockZ());
		int minZ = Math.min(pointOne.getBlockZ(), pointTwo.getBlockZ());
		int startX = start.getBlockX();
		int startY = start.getBlockY();
		int startZ = start.getBlockZ();
		World world = pointOne.getWorld();
		for (int x = minX; x <= maxX; x++)
			blocks.add(world.getBlockAt(x, startY, startZ));
		for (int y = minY; y <= maxY; y++)
			blocks.add(world.getBlockAt(startX, y, startZ));
		for (int z = minZ; z <= maxZ; z++)
			blocks.add(world.getBlockAt(startX, startY, z));
		return blocks;
	}

	/**
	 * Gets the prison value of a list of blocks.
	 *
	 * @param player the player to check for.
	 * @param blocks the blocks to check.
	 * @return the amount these blocks are worth.
	 */
	public static double getValue(Player player, List<Block> blocks) {
		HashMap<XMaterial, Integer> map = new HashMap<>();
		for (Block block : blocks) {
			XMaterial material = XMaterial.matchXMaterial(block.getType());
			if (map.containsKey(material)) {
				int amount = map.get(material);
				map.put(material, amount + 1);
			} else
				map.put(material, 1);
		}
		return SellAllUtil.get().getSellMoney(player, map);
	}

	/**
	 * Gets a hollow cube. Used for spawning particles around a block outline.
	 *
	 * @param location the location of the block.
	 * @param step the distance of the particles. Should default at 0.1.
	 * @return a list of the locations to spawn.
	 */
	public static List<Location> getHollowCube(Location location, double step) {
		List<Location> result = new ArrayList<>();
		World world = location.getWorld();
		double[] xArr = {location.getBlockX(), location.getBlockX() + 1};
		double[] yArr = {location.getBlockY(), location.getBlockY() + 1};
		double[] zArr = {location.getBlockZ(), location.getBlockZ() + 1};
		for (double x = xArr[0]; x < xArr[1]; x += step)
			for (double y : yArr)
				for (double z : zArr)
					result.add(new Location(world, x, y, z));
		for (double y = yArr[0]; y < yArr[1]; y += step)
			for (double x : xArr)
				for (double z : zArr)
					result.add(new Location(world, x, y, z));
		for (double z = zArr[0]; z < zArr[1]; z += step)
			for (double x : xArr)
				for (double y : yArr)
					result.add(new Location(world, x, y, z));
		return result;
	}

	public static List<Location> getHollowCube(Location pointOne, Location pointTwo, double step) {
		List<Location> result = new ArrayList<>();
		World world = pointOne.getWorld();
		double[] xArr = {pointOne.getBlockX(), pointTwo.getBlockX()};
		double[] yArr = {pointOne.getBlockY(), pointTwo.getBlockY()};
		double[] zArr = {pointOne.getBlockZ(), pointTwo.getBlockZ()};
		for (double x = xArr[0]; x < xArr[1]; x += step)
			for (double y : yArr)
				for (double z : zArr)
					result.add(new Location(world, x, y, z));
		for (double y = yArr[0]; y < yArr[1]; y += step)
			for (double x : xArr)
				for (double z : zArr)
					result.add(new Location(world, x, y, z));
		for (double z = zArr[0]; z < zArr[1]; z += step)
			for (double x : xArr)
				for (double y : yArr)
					result.add(new Location(world, x, y, z));
		return result;
	}

	/**
	 * Gets the level of the given enchantment for the given player.
	 *
	 * @param player the player.
	 * @param enchant the string.
	 * @return the enchantment level.
	 */
	public static int getEnchantmentLevel(Player player, String enchant) {
		CEHandler handler = api.getEnchantment(enchant);
		return handler.getCELevel(player);
	}

}
