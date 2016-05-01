package de.expeehaa.xphabitat.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.expeehaa.xphabitat.XPHabitat;

public class CommandXPH implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("xph")){
			if(args.length == 0) return true;
			
			if(args[0] == "cfg"){
				reloadConfig(sender);
			}
			
			if(args[0] == "get"){
				getPlayersStoredXP(sender, args);
			}
			
			return true;
		}
		
		return false;
	}
	
	void reloadConfig(CommandSender sender){
		if(sender.hasPermission("xphabitat.reloadConfig")){
			XPHabitat.instance.reloadCfg();
			sender.sendMessage(XPHabitat.prefix + ChatColor.GREEN + "Config successfully reloaded!");
		}
	}
	
	void getPlayersStoredXP(CommandSender sender, String[] args){
		if(args.length < 1) return;
		UUID uuid = null;
		String name = "";
		Player p = XPHabitat.instance.getServer().getPlayer(args[1]);
		if(p == null) {
			@SuppressWarnings("deprecation")
			OfflinePlayer offp = XPHabitat.instance.getServer().getOfflinePlayer(args[1]);
			
			if(offp == null){
				sender.sendMessage(XPHabitat.prefix + ChatColor.RED + "Player " + args[1] + " has no XPHabitat!");
				return;
			}
			
			uuid = offp.getUniqueId();
			name = offp.getName();
		}
		else{
			uuid = p.getUniqueId();
			name = p.getName();
		}
		
		sender.sendMessage(XPHabitat.prefix + ChatColor.GREEN + "Player " + name + " has " + XPHabitat.storedxp.get(uuid) + "XP stored.");
	}
}
