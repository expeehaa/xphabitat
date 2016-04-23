package de.expeehaa.xphabitat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
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
	
	public static HashMap<UUID, Float> storedxp = new HashMap<UUID, Float>();
	
	
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
		SQLConnecter.update("CREATE TABLE IF NOT EXISTS `storedxp` ( `uuid` VARCHAR(100) NOT NULL PRIMARY KEY, `xp` FLOAT(255,10) NOT NULL )");
		
		Plugin plugin = this.getServer().getPluginManager().getPlugin("Craftconomy3");
		if(plugin != null){
			craftconomy = (Common) ((Loader) plugin).getCommon();
		}
		this.getLogger().info("Craftconomy3 " + (plugin != null ? "" : "not") + " found!");
		
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			
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
		
		this.getConfig().addDefault("habitat.storeTax", 0.6);
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
		
		HashMap<UUID, Float> storemap = new HashMap<UUID, Float>();

		try {
			int col1 = rs.findColumn("uuid");
			int col2 = rs.findColumn("xp");
			while(rs.next()){
				String uuidstring = rs.getString(col1);
				int xp = rs.getInt(col2);
				storemap.put(UUID.fromString(uuidstring), Float.valueOf(xp));
			}
		} catch (SQLException e) {
			this.getLogger().info("SQLException: " + e.getMessage());
		}
		
		this.getLogger().info("got new SQL data with " + storemap.size() + " objects");
		
		String map = "";
		for (Entry<UUID, Float> entry : storemap.entrySet()) {
			map += "\n" + entry.getKey().toString() + " | " + entry.getValue().toString();
		}
		
		this.getLogger().info(map);
		
		storedxp = storemap;
	}
}
