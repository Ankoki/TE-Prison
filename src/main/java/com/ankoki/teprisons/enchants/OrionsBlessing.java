package com.ankoki.teprisons.enchants;

import com.vk2gpz.tokenenchant.api.EnchantHandler;
import com.vk2gpz.tokenenchant.api.InvalidTokenEnchantException;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;

public class OrionsBlessing extends EnchantHandler {

	// We do nothing here. MidasTouch handles it for us.

	public OrionsBlessing(TokenEnchantAPI tokenEnchantAPI) throws InvalidTokenEnchantException {
		super(tokenEnchantAPI);
		this.loadConfig();
	}

	@Override
	public String getName() {
		return "OrionsBlessing";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

}
