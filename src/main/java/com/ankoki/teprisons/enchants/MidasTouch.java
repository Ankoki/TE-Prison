package com.ankoki.teprisons.enchants;

import com.vk2gpz.tokenenchant.api.EnchantHandler;
import com.vk2gpz.tokenenchant.api.InvalidTokenEnchantException;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import com.vk2gpz.tokenenchant.event.EventPriorityHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public class MidasTouch extends EnchantHandler {

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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public void onBlockBreak(BlockBreakEvent event) {

	}

}
