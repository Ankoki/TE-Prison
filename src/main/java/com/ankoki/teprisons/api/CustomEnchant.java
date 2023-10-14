package com.ankoki.teprisons.api;

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

// UNUSED
public abstract class CustomEnchant extends EnchantHandler {

	private final String name;
	private final String version;
	private final TokenEnchantAPI api;
	public final ConsoleCommandSender console = Bukkit.getConsoleSender();

	/**
	 * Creates a new custom enchant.
	 *
	 * @param api You should take in a TokenEnchantAPI parameter and pass it here.
	 * @param name the name of your enchant.
	 * @param version the version of your enchant.
	 * @throws InvalidTokenEnchantException Unsure.
	 */
	public CustomEnchant(TokenEnchantAPI api, String name, String version) throws InvalidTokenEnchantException {
		super(api);
		this.api = api;
		this.name = name;
		this.version = version;
		this.loadConfig();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the Token Enchants API provided.
	 *
	 * @return the api.
	 */
	public TokenEnchantAPI getAPI() {
		return api;
	}

	/**
	 * Gets the level of this enchant for the given player.
	 *
	 * @param player the player.
	 * @return the level of the enchantment.
	 */
	public int getLevel(Player player) {
		CEHandler handler = api.getEnchantment(this.name);
		return handler.getCELevel(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public abstract void onBlockBreak(BlockBreakEvent event);

}
