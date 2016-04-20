package de.expeehaa.xphabitat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.greatmancode.craftconomy3.Common;
import com.greatmancode.craftconomy3.tools.interfaces.Loader;

public class XPHabitat extends JavaPlugin {

	public static XPHabitat instance;
	
	public static final String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "XPH" + ChatColor.GRAY + "]§r ";
	
	public static Common craftconomy;
	
	public static HashMap<UUID, Integer> storedxp = new HashMap<UUID, Integer>();
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		instance = this;
		
		reloadCfg();
		
		setSQLConnecterData();
		SQLConnecter.connect();
		if(!SQLConnecter.isConnected()){
			this.getServer().getPluginManager().disablePlugin(this);
			this.getLogger().info(prefix + "XPHabitat is disabled now!");
			return;
		}
		SQLConnecter.update("create table if not exists storedxp (uuid varchar(100) primary key unique, xp int(255))");
		
		Plugin plugin = this.getServer().getPluginManager().getPlugin("Craftconomy3");
		if(plugin != null){
			craftconomy = (Common) ((Loader) plugin).getCommon();
		}
		this.getLogger().info("Craftconomy3 " + (plugin != null ? "" : "not") + " found!");
		
		
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			
			public void run() {
				XPHabitat.instance.retrieveSQLData();
			}
		}, 0, 6000);
		
		this.getServer().getPluginManager().registerEvents(new XPHabitatListener(), this);
	}
	
	@Override
	public void onDisable() {
		SQLConnecter.disconnect();
	}
	
	void reloadCfg(){
		this.reloadConfig();
		
		this.getConfig().addDefault("habitat.storeTax", "0.6");
		this.getConfig().addDefault("habitat.costPerHabitat", "5000");
		this.getConfig().addDefault("db.host", "");
		this.getConfig().addDefault("db.port", "3306");
		this.getConfig().addDefault("db.database", "");
		this.getConfig().addDefault("db.username", "");
		this.getConfig().addDefault("db.password", "");
		
		this.getConfig().options().copyDefaults(true);
		
		this.saveConfig();
		this.reloadConfig();
		
		this.getLogger().info("Config successfully reloaded!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("xphcfg")){
			if(sender.hasPermission("xphabitat.reloadConfig")){
				this.reloadCfg();
				sender.sendMessage(prefix + ChatColor.GREEN + "Config successfully reloaded!");
				
				return true;
			}
		}
		
		return false;
	}
	
	public void setSQLConnecterData(){
		
		
		SQLConnecter.host = this.getConfig().getString("db.host");
		SQLConnecter.port = this.getConfig().getString("db.port");
		SQLConnecter.database = this.getConfig().getString("db.database");
		SQLConnecter.username = this.getConfig().getString("db.username");
		SQLConnecter.password = this.getConfig().getString("db.password");
	}
	
	public void retrieveSQLData(){
		ResultSet rs = SQLConnecter.getResult("SELECT * FROM `storedxp`");
		
		if(rs == null){
			this.getLogger().info("Could not retrieve new SQL data!");
			return;
		}
		
		List<UUID> uuidlist = new ArrayList<UUID>();
		List<Integer> storedXPList = new ArrayList<Integer>();
		HashMap<UUID, Integer> storemap = new HashMap<UUID, Integer>();

		int i = 1;
		try {
			while(rs.next()){
				String s;
				s = rs.getObject(0, String.class);
				uuidlist.add(UUID.fromString(s));
				s = rs.getObject(1, String.class);
				storedXPList.add(Integer.parseInt(s));
				i++;
			}
		} catch (SQLException e) {
			this.getLogger().info("SQLException: " + e.getMessage());
		}
		
		for (i = 0; i < uuidlist.size(); i++) {
			storemap.put(uuidlist.get(i), storedXPList.get(i));
		}
		
		storedxp = storemap;
	}
}