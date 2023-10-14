package com.ankoki.teprisons.enchants;

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
import tech.mcprison.prison.cryptomorin.xseries.XMaterial;
import tech.mcprison.prison.ranks.PrisonRanks;
import tech.mcprison.prison.ranks.data.RankPlayer;
import tech.mcprison.prison.spigot.game.SpigotPlayer;
import tech.mcprison.prison.spigot.sellall.SellAllUtil;

import java.util.*;

public class FortuneTeller extends EnchantHandler {

	private final TokenEnchantAPI api;
	private final List<String> blockedWorlds = new ArrayList<>();
	private final ConsoleCommandSender console = Bukkit.getConsoleSender();
	private boolean debug;

	public FortuneTeller(TokenEnchantAPI api) throws InvalidTokenEnchantException {
		super(api);
		this.api = api;
		this.loadConfig();
	}

	@Override
	public String getName() {
		return "FortuneTeller";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.debug = this.getConfig().getBoolean("dev-debug");
		List<String> list = this.getConfig().getStringList("Enchants.Fortune.blocked-worlds");
		this.blockedWorlds.clear();
		this.blockedWorlds.addAll(list);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (this.blockedWorlds.contains(player.getWorld().getName())) {
			if (debug)
				this.console.sendMessage("§eTE-Prison | this.blockedWorlds.contains(player.getWorld()) == true : BlockBreakEvent : Fortune");
			return;
		}
		CEHandler handler = api.getEnchantment("FortuneTeller");
		int level = handler.getCELevel(player);
		XMaterial material = XMaterial.matchXMaterial(event.getBlock().getType());
		HashMap<XMaterial, Integer> map = new HashMap<>();
		map.put(material, 1);
		double amount = SellAllUtil.get().getSellMoney(player, map);
		if (debug)
			this.console.sendMessage("§eTE-Prison | SellAllUtil.get().getSellMoney(player, map) = " + amount + ": BlockBreakEvent : Fortune");
		amount *= 1 + (level / 10D);
		if (debug)
			this.console.sendMessage("§eTE-Prison | amount *= 1 + (level / 10D) = " + amount + ": BlockBreakEvent : Fortune");
		SpigotPlayer sPlayer = new SpigotPlayer(player);
		RankPlayer rPlayer = PrisonRanks.getInstance().getPlayerManager().getPlayer(sPlayer.getUUID(), sPlayer.getName());
		rPlayer.addBalance(amount);
	}

}

