package com.ankoki.teprisons.api;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.vk2gpz.tokenenchant.api.InvalidTokenEnchantException;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import org.jetbrains.annotations.Nullable;

// UNUSED
public abstract class WorldGuardEnchant extends CustomEnchant {

	private RegionContainer container;
	private boolean disabled;

	/**
	 * Creates a new custom enchant that relies on world guard.
	 *
	 * @param api     You should take in a TokenEnchantAPI parameter and pass it here.
	 * @param name    the name of your enchant.
	 * @param version the version of your enchant.
	 * @throws InvalidTokenEnchantException Unsure.
	 */
	public WorldGuardEnchant(TokenEnchantAPI api, String name, String version) throws InvalidTokenEnchantException {
		super(api, name, version);
		try {
			this.container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		} catch (Exception ex) {
			this.console.sendMessage("§cTE-Prison | WorldGuard not found. Enchant '" + name + "' will not work.");
			this.disabled = true;
		}
	}

	/**
	 * Checks if WorldGuard was able to be found.
	 *
	 * @return false if found, true if not.
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Gets the WorldGuard Region container to use in this enchant.
	 *
	 * @return the container.
	 */
	@Nullable
	public RegionContainer getContainer() {
		return container;
	}

}
