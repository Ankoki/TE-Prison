package com.ankoki.teprisons.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Misc {

	private static final List<Player> blocked = new ArrayList<>();

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
	 * @param locationOne the first point.
	 * @param locationTwo the second point.
	 * @return the blocks between the two points.
	 */
	public static List<Block> getBlocks(Location locationOne, Location locationTwo) {
		List<Block> blocks = new ArrayList<>();
		int topBlockX = (Math.max(locationOne.getBlockX(), locationTwo.getBlockX()));
		int bottomBlockX = (Math.min(locationOne.getBlockX(), locationTwo.getBlockX()));
		int topBlockY = (Math.max(locationOne.getBlockY(), locationTwo.getBlockY()));
		int bottomBlockY = (Math.min(locationOne.getBlockY(), locationTwo.getBlockY()));
		int topBlockZ = (Math.max(locationOne.getBlockZ(), locationTwo.getBlockZ()));
		int bottomBlockZ = (Math.min(locationOne.getBlockZ(), locationTwo.getBlockZ()));
		for (int x = bottomBlockX; x <= topBlockX; x++)
			for (int z = bottomBlockZ; z <= topBlockZ; z++)
				for (int y = bottomBlockY; y <= topBlockY; y++)
					blocks.add(locationOne.getWorld().getBlockAt(x, y, z));
		return blocks;
	}

}
