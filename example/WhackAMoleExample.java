package mx.com.rodel.games.defaultgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import mx.com.rodel.games.Game;
import mx.com.rodel.utils.Helper;
import mx.com.rodel.utils.SoundHelper;
import mx.com.rodel.utils.TimerAPI;

/**
 * This file is part of Mini Board Games Spigot Plugin and only can be used for learn about MBG API
 * 
 * You can't sell/redistribute it without the author permission 
 * 
 * @author rodel77
 */
public class WhackAMoleExample extends Game{
	//Initialize Timer
	private TimerAPI timer = new TimerAPI(60000);
	//Slots for mobs spawn
	private List<Integer> slots = new ArrayList<>();
	//Current player points (You can make it like local variable because is singleplayer game)
	private int points = 0;
	//Current player playing (You can make it like local variable because is singleplayer game)
	private UUID player;
	
	//Initialize constructor, use it for non-inventory/player/bukkit actions
	public WhackAMoleExample() {
		//Define slots for mobs spawn
		slots.add(10);
		slots.add(12);
		slots.add(14);
		slots.add(16);
	}

	//Handle game start event
	@Override
	public void onGameStart() {
		//Set black glass in not special slots
		for (int i = 0; i < getGUISize(); i++) {
			//Slot 22 is for the clock
			if(!slots.contains(i) && i!=22){
				sendToAllClients(getCover(), i);
			}
		}
		
		//The the player variable
		for(Entry<UUID, Inventory> p : players.entrySet()){
			player = p.getKey();
		}
		
		//Start the timer
		timer.start();
	}
	
	//Handle click event
	@Override
	public void onPlayerClickOnGUI(InventoryClickEvent e) {
		//Check if is mob
		if(e.getCurrentItem().getType()==Material.SKULL_ITEM && e.getCurrentItem().getItemMeta().getDisplayName().equals(getStringNode("smash")) && slots.contains(e.getSlot())){
			//Add points
			points += 5;
			//Play sound according the head
			switch (e.getCurrentItem().getDurability()) {
				case 0:
					SoundHelper.ENTITY_SKELETON_DEATH.play((Player) e.getWhoClicked(), 1, 1);
					break;
				case 1:
					SoundHelper.ENTITY_SKELETON_DEATH.play((Player) e.getWhoClicked(), 1, 1);
					break;
				case 2:
					SoundHelper.ENTITY_ZOMBIE_DEATH.play((Player) e.getWhoClicked(), 1, 1);
					break;
				case 4:
					SoundHelper.ENTITY_CREEPER_DEATH.play((Player) e.getWhoClicked(), 1, 1);
					break;
			}
			//Remove item
			removeItem(player, e.getSlot());
			//Increment stat for database
			incrementStat(player, "mobsKilled", 1);
			incrementStat(player, "coins", 5);
		}
		e.setCancelled(true);
	}
	
	@Override
	public void loop() {
		//Set the clock
		sendToAllClients(timer.getItemTimer(), 22);
		
		//Clear all spawn slots
		if(new Random().nextInt(10)==0){
			for(int slot : slots){
				removeItem(player, slot);
			}
		}
		
		//Spawn new head
		if(new Random().nextInt(5)==0){
			//Remove all slots
			for(int slot : slots){
				removeItem(player, slot);
			}
			
			//Generate/Put head
			
			int[] r = new int[] {0,1,2,4};
			
			sendToAllClients(createItem(Material.SKULL_ITEM, r[new Random().nextInt(r.length)], 1, getStringNode("smash")), slots.get(new Random().nextInt(slots.size())));
		}
		
		//End if the timer end
		if(timer.isFinished()){
			end();
		}
		
		//Update coins counter
		sendToAllClients(createItem(Material.GOLD_NUGGET, 0, points>64 ? 64 : points, getStringNode("points", "{points}:"+points)), 4);
	}
	
	//End the game
	public void end(){
		//Call end
		endGame(true);
		//The the points message
		if(getConfigNode("broadcast", Boolean.class)){
			sendChatBroadcast(getStringNode("broadcastmsg", "{player}:"+Bukkit.getPlayer(player).getName(), "{points}:"+points));
		}else{
			sendChatMessageAll(getStringNode("broadcastmsg", "{player}:"+Bukkit.getPlayer(player).getName(), "{points}:"+points));
		}
		//Spawn fireworks
		Helper.spawnFirework(Bukkit.getPlayer(player).getLocation(), 5);
	}
	
	//Setup name
	@Override
	public String getName() {
		return "WhackAMole";
	}
	
	//Setup minimum players
	@Override
	public int getMinPlayers() {
		return 1;
	}
	
	//Setup maximum players (1 for singleplayer)
	@Override
	public int getMaxPlayers() {
		return 1;
	}

	//Setup icon
	@Override
	public Material getIcon() {
		return Material.DIAMOND_PICKAXE;
	}

	//Setup minigamecolor
	@Override
	public ChatColor getMinigameNameColor() {
		return ChatColor.DARK_GREEN;
	}
	
	//Setup gui size to small
	@Override
	public int getGUISize() {
		return SMALL_GUI;
	}
	
	//Setup nodes
	@Override
	public HashMap<String, Object> registerConfigNodes(HashMap<String, Object> nodes) {
		nodes.put("smash", "&aSmash!");
		nodes.put("broadcastmsg", "&6{player} wins {points} points in WhackAMole");
		nodes.put("broadcast", true);
		nodes.put("points", "&6{points} points!");
		return nodes;
	}
	
	//Setup nodes
	@Override
	public LinkedHashMap<String, String> registerDBStats(LinkedHashMap<String, String> stats) {
		stats.put("mobsKilled", "INT(10)");
		stats.put("coins", "INT(10)");
		return stats;
	}
}
