package de.expeehaa.xphabitat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.greatmancode.craftconomy3.Cause;
import com.greatmancode.craftconomy3.account.Account;

public class XPHabitatListener implements Listener {
	
	@EventHandler
	public void onSignWritten(SignChangeEvent e){
		if(e.getLine(0).equals("[XPH]") && e.getLine(1).equals("Habitat sign")){
			if(e.getPlayer().hasPermission("xphabitat.createHabitatSigns")){
				e.getPlayer().sendMessage(XPHabitat.prefix + ChatColor.GREEN + "You successfully created a habitat sign!");
			}
			else{
				e.getPlayer().sendMessage(XPHabitat.prefix + "§4You do not have permission to create habitat signs!");
				e.getBlock().breakNaturally();
			}
		}
		if(e.getBlock().getState() instanceof Sign){
			XPHabitat.instance.getLogger().info("Block at " + e.getBlock().getLocation().toString() + " is a sign!");
		}
	}
	
	@EventHandler
	public void onRightClickSign(PlayerInteractEvent e){
		if(e.getClickedBlock() == null)return;
		
		if(e.getClickedBlock().getType().equals(Material.SIGN_POST) || e.getClickedBlock().getType().equals(Material.WALL_SIGN)){
			
			Sign sign = (Sign) e.getClickedBlock().getState();
			
			
			if(sign.getLine(0).equals("[XPH]") && sign.getLine(1).equals("Habitat sign")){

				if(e.getPlayer().isSneaking()){
					return;
				}
				
				/*
				if(e.getPlayer().getName().equals("katket")){
					e.getPlayer().kickPlayer("katket");
					return;
				}
				*/
				
				e.setCancelled(true);
				
				Player p = e.getPlayer();
				
				if(!XPHabitat.storedxp.containsKey(p.getUniqueId())){
					e.getPlayer().sendMessage("You have no habitat");
					
					//use craftconomy3, if it's enabled
					if(XPHabitat.craftconomy != null){
						
						Account playeraccount = XPHabitat.craftconomy.getAccountManager().getAccount(p.getName(), true);
						String defaultcurrency = XPHabitat.craftconomy.getCurrencyManager().getDefaultCurrency().getName();
						String worldname = p.getWorld().getName();
						
						//check if the player has enough money
						if(playeraccount.hasEnough(XPHabitat.instance.getConfig().getDouble("habitat.costPerHabitat"), worldname, defaultcurrency)){
							playeraccount.withdraw(XPHabitat.instance.getConfig().getDouble("habitat.costPerHabitat"), worldname, defaultcurrency, Cause.PAYMENT, "XPHabitat bought");
							
							//update database and hashmap
							SQLConnecter.update("insert into 'storedxp' ('uuid','xp') values ('" + p.getUniqueId() + "', 0)");
							XPHabitat.instance.retrieveSQLData();
						}
						else {
							p.sendMessage(XPHabitat.prefix + "§4You do not have enough money to buy a XPHabitat!");
							return;
						}
					}
					else{
						//update database and hashmap
						SQLConnecter.update("INSERT INTO `storedxp`(`uuid`, `xp`) VALUES ('" + p.getUniqueId() + "', 0)");
						XPHabitat.instance.retrieveSQLData();
						
						p.sendMessage("You got an habitat");
					}
				}
				
				//store xp in habitat
				if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
					float xp = 0;
					
					if(p.getExp() > 0) {
						int maxXP = maxXPinBar(p.getLevel()+1);
						
						xp = maxXP * p.getExp();						
						p.sendMessage(maxXP + "; " + xp + "; " + p.getExpToLevel() + "; " + p.getExp());
						
						p.giveExp((int) -xp);
					}
					else if(p.getLevel() > 0){
						xp = maxXPinBar(p.getLevel());
						p.setLevel(p.getLevel() - 1);
					}
					else{
						p.sendMessage(XPHabitat.prefix + "§4You do not have enough levels!");
						return;
					}
					
					float taxxp = (float) (xp * XPHabitat.instance.getConfig().getDouble("habitat.storeTax"));
					
					float newStoredXP = taxxp + XPHabitat.storedxp.get(p.getUniqueId());
					
					
					//update database and hashmap
					SQLConnecter.update("UPDATE `storedxp` SET `uuid`='" + p.getUniqueId().toString() + "', `xp`=" + newStoredXP + " WHERE `uuid`='" + p.getUniqueId().toString() + "'");
					XPHabitat.instance.retrieveSQLData();
					
					p.sendMessage(XPHabitat.prefix + ChatColor.GREEN + "You stored " + taxxp + "XP in your deposit. You now have " + newStoredXP + "XP inside.");
					return;
				}
				//retrieve xp from habitat
				else if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
					
					float storedXp = XPHabitat.storedxp.get(p.getUniqueId()) == null ? 0 : XPHabitat.storedxp.get(p.getUniqueId());
					float drainedXp = 0;
					
					if(storedXp > 0){
						
						if(storedXp >= (1-p.getExp()) * maxXPinBar(p.getLevel() + 1)){
							drainedXp = ((1-p.getExp()) * maxXPinBar(p.getLevel() + 1));
							p.giveExp((int)drainedXp);
							storedXp -= drainedXp;
						}
						else {
							drainedXp = storedXp;
							p.giveExp((int)drainedXp);
							storedXp = 0;
						}
						
						//update database and hashmap
						SQLConnecter.update("UPDATE `storedxp` SET `uuid`='" + p.getUniqueId().toString() + "', `xp`=" + storedXp + " WHERE `uuid`='" + p.getUniqueId().toString() + "'");
						XPHabitat.instance.retrieveSQLData();
						
						p.sendMessage(XPHabitat.prefix + ChatColor.GREEN + "You drained " + drainedXp + "XP from your deposit. You now have " + storedXp + "XP inside.");
						return;
					}
					else{
						p.sendMessage(XPHabitat.prefix + "§4You do not have enough XP in your deposit!");
						return;
					}
				}
			}
		}
	}
	

	public int maxXPinBar(int level){
		return level > 30 ? 103 + (level - 30) * 9 : (level > 15 ? 32 + (level - 15) * 5 : 2 * level + 5);
	}
}
