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
import org.bukkit.inventory.ItemStack;

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
public class WhackAMole extends Game{
	//Initialize the Timer API
	private TimerAPI timer = new TimerAPI(60000);
	//Slots for skull spawning
	private List<Integer> slots = new ArrayList<>();
	//Current player points (If you are making multiplayer boardgames make this with HashMaps/ArraysLists)
	private int points = 0;
	//Player (If you are making multiplayer boardgames make this with HashMaps/ArraysLists)
	private UUID player;
	
	//Constructor game, don't use it for Game API methods, because this method go to initialize on load the game and play it
	public WhackAMole() {
		//Define skull spawns slots
		slots.add(10);
		slots.add(12);
		slots.add(14);
		slots.add(16);
	}
	
	//Handle the game start
	@Override
	public void onGameStart() {
		//Add black glass in not spawn slots
		for (int i = 0; i < getGUISize(); i++) {
			if(!slots.contains(i) && i!=22){
				sendToAllClients(getCover(), i);
			}
		}

		//Define the previous see "player" var
		for(Entry<UUID, Inventory> p : players.entrySet()){
			player = p.getKey();
		}
		
		//Start the timer
		timer.start();
	}
	
	//Handle click event
	@SuppressWarnings("deprecation")
	@Override
	public void onPlayerClickOnGUI(InventoryClickEvent e) {
		e.setCancelled(true);
		e.setCursor(new ItemStack(Material.AIR));
		//Check if is a game skull
		if(e.getCurrentItem()!=null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getCurrentItem().getItemMeta().getDisplayName().equals(getStringNode("smash")) && 
				slots.contains(e.getSlot())){
			//Increment points
			points += e.getCurrentItem().getAmount();
			//Play the sound according the skull
			if(e.getCurrentItem().getType()==Material.COOKIE){
				SoundHelper.ENTITY_ARROW_HIT_PLAYER.play((Player) e.getWhoClicked(), 1, 1);
			}else if(e.getCurrentItem().getType()==Material.SKULL_ITEM){
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
				case 3:
					SoundHelper.ENTITY_ZOMBIE_INFECT.play((Player) e.getWhoClicked(), 1, 1);
					break;
				case 4:
					SoundHelper.ENTITY_CREEPER_DEATH.play((Player) e.getWhoClicked(), 1, 1);
					break;
			}
			}
			removeItem(player, e.getSlot());
			//Increment local stat for send to DB when the game end
			incrementStat(player, "mobsSmashed", 1);
			incrementStat(player, "coins", e.getCurrentItem().getAmount());
		}
	}
	
	//Handle loop
	@Override
	public void loop() {
		//Set the timer item provided by TimerAPI
		sendToAllClients(timer.getItemTimer(), 22);
		
		//If random remove all skulls
		if(new Random().nextInt(10)==0){
			for(int slot : slots){
				removeItem(player, slot);
			}
		}
		
		//If random spawn new skull
		if(new Random().nextInt(5)==0){
			//Remove all skulls
			for(int slot : slots){
				removeItem(player, slot);
			}
			
			//Set random spawn and put the skull
			
			int[] r = new int[] {0,1,2,4};
			
			int points = new Random().nextInt(5);
			
			if(new Random().nextInt(4)==0){
				points+=new Random().nextInt(3);
			}
			
			boolean halloween = Helper.getMonth()==9;
			boolean xmas = Helper.getMonth()==11;
			if(xmas){
				sendToAllClients(createItem(Material.COOKIE, 0, points, getStringNode("smash")), slots.get(new Random().nextInt(slots.size())));
			}else if(halloween){
				//This is sooooo spoky...
				sendToAllClients(Helper.headizer("MHF_Pumpkin", createItem(Material.SKULL_ITEM, 3, points, getStringNode("smash"))), slots.get(new Random().nextInt(slots.size())));
			}else{
				sendToAllClients(createItem(Material.SKULL_ITEM, r[new Random().nextInt(r.length)], points, getStringNode("smash")), slots.get(new Random().nextInt(slots.size())));
			}
			
		}
		
		//If timer is finished end the game
		if(timer.isFinished()){
			end();
		}
		
		//Display the points
		sendToAllClients(createItem(Material.GOLD_NUGGET, 0, Math.min(points, 64), getStringNode("points", "{points}:"+points)), 4);
	}
	
	public void end(){
		//End the game, true for register stats, false for end without register stats (This is for miss points if you cancel the game)
		endGame(true);
		//Execute the final command, this is the rewards, you can set it in configuration
		executeFinalCommand(Bukkit.getPlayer(player));
		//If is active broadcast send message to all players with player points
		if(getConfigNode("broadcast", Boolean.class)){
			sendChatBroadcast(getStringNode("broadcastmsg", "{player}:"+Bukkit.getPlayer(player).getName(), "{points}:"+points));
		}else{
			sendChatMessageAll(getStringNode("broadcastmsg", "{player}:"+Bukkit.getPlayer(player).getName(), "{points}:"+points));
		}
		//Spawn 5 fireworks 1 each 5 ticks 
		Helper.spawnFirework(Bukkit.getPlayer(player).getLocation(), 5);
	}
	
	//The raw name game, this can be changed in configuration "displayName"
	@Override
	public String getName() {
		return "WhackAMole";
	}

	//Minimum players can join in the game
	@Override
	public int getMinPlayers() {
		return 1;
	}

	//Maximum players can join in the game (1 for singleplayer games)
	@Override
	public int getMaxPlayers() {
		return 1;
	}

	//Game icon in game list
	@Override
	public ItemStack getItemIcon() {
		return new ItemStack(Material.DIAMOND_PICKAXE);
	}

	//Game color for GUI, this can be changed in configuration "displayName"
	@Override
	public ChatColor getMinigameNameColor() {
		return ChatColor.DARK_GREEN;
	}
	
	//Set GUI size default: BIG_GUI (54)
	@Override
	public int getGUISize() {
		return SMALL_GUI;
	}
	
	//Register configuration nodes, add here and then when the game load it go to be added in config.yml
	@Override
	public HashMap<String, Object> registerConfigNodes(HashMap<String, Object> nodes) {
		nodes.put("smash", "&aSmash!");
		nodes.put("broadcastmsg", "&6{player} wins {points} points in WhackAMole");
		nodes.put("broadcast", true);
		nodes.put("points", "&6{points} points!");
		return nodes;
	}
	
	//Register database nodes, add here and then when the game load it go to be added in db
	@Override
	public LinkedHashMap<String, String> registerDBStats(LinkedHashMap<String, String> stats) {
		//rawName - dbType
		stats.put("mobsSmashed", "INT(10)");
		stats.put("coins", "INT(10)");
		return stats;
	}
	
	@Override
	public String getGameInstructions() {
		return "Smash the most possible skulls#in 60 seconds";
	}
}
