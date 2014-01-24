/*******************************************************************************
 * Copyright 2014 Alex Miller
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package us.hyperpvp;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import us.hyperpvp.commands.AuthCommand;
import us.hyperpvp.commands.CycleCommand;
import us.hyperpvp.commands.JoinCommand;
import us.hyperpvp.commands.MatchInfoCommand;
import us.hyperpvp.commands.PinCommand;
import us.hyperpvp.commands.ReportCommand;
import us.hyperpvp.commands.ScoreCommand;
import us.hyperpvp.commands.SpectateCommand;
import us.hyperpvp.commands.TeamChatCommand;
import us.hyperpvp.game.Game;
import us.hyperpvp.game.map.GameMap;
import us.hyperpvp.game.map.team.Detonator;
import us.hyperpvp.game.map.team.TeamMap;
import us.hyperpvp.game.session.Session;
import us.hyperpvp.listeners.BlockListener;
import us.hyperpvp.listeners.CreatureListener;
import us.hyperpvp.listeners.EntityListener;
import us.hyperpvp.listeners.InventoryListener;
import us.hyperpvp.listeners.MiscListener;
import us.hyperpvp.listeners.PlayerListener;
import us.hyperpvp.misc.Configuration;
import us.hyperpvp.misc.CycleUtil;
import us.hyperpvp.storage.Storage;
import us.hyperpvp.thread.Announcer;
import us.hyperpvp.thread.FightThread;
import us.hyperpvp.thread.misc.IThread;
import us.hyperpvp.thread.misc.ThreadType;

public class HyperPVP extends JavaPlugin {

	public static HyperPVP plugin;
	public static Storage storage;
	public static Configuration configuration;
	public static Boolean isCycling;
	public static TeamMap winningTeam;
	public static Integer minutesLeft;
	public static String timeString = "";
	public static Game game;
	public static Random random;
	public static Boolean cannotJoin;
	public static GameMap previousWorld;
	public static Boolean needsCycleThread;
	public static Boolean needsGameThread;
	public static Boolean needsMatchCheck;
	public static Boolean hasMatchBeenAnnounced;
	public static Boolean checkFirework;
	public static Map<Location, Color> fireworkLocation;
	public static WorldsHolder groupManager;
	public static int games = 0;

	public static Map<Detonator, Session> detonators;
	public static Map<Player, ChatColor> teamCycle;
	public static ConcurrentMap<ThreadType, IThread> threads;
	public static ConcurrentHashMap<String, Player> spectators;
	public static ConcurrentHashMap<String, Session> gameSessions;
	public static Player winningPlayer;
	public static boolean needsRestart;
	public static int callId;
	
	@Override
	public void onEnable() {

		plugin = this;
		isCycling = false;
		needsCycleThread = false;
		needsGameThread = false;
		needsMatchCheck = false;
		checkFirework = false;
		hasMatchBeenAnnounced = false;
		minutesLeft = 30;
		groupManager = getWorldsHolder();
		fireworkLocation = new HashMap<Location, Color>();
		teamCycle = new HashMap<Player, ChatColor>();
		threads = new ConcurrentHashMap<ThreadType, IThread>();
		spectators = new ConcurrentHashMap<String, Player>();
		gameSessions = new ConcurrentHashMap<String, Session>();
		detonators = new HashMap<Detonator, Session>();
		random = new Random();

		this.getLogger().info("Loading configuration.");
		this.initalizeConfiguration();

		this.getLogger().info("Loading MySQL database.");
		this.initalizeMySQL(true);

		this.getLogger().info("Loading the 'Game' instance.");
		game = new Game(this);

		this.getLogger().info("Loading the listeners.");
		this.handlers();
		this.commands();

		this.getLogger().info("Starting fight thread.");

		//getMap().load();
		HyperPVP.threads.put(ThreadType.FIGHT, new FightThread());
		HyperPVP.threads.get(ThreadType.FIGHT).start();

		this.getLogger().info("Starting announce thread");

		HyperPVP.threads.put(ThreadType.ANNOUNCE, new Announcer());
		HyperPVP.threads.get(ThreadType.ANNOUNCE).start();

		this.getLogger().info("Performing task..");
		callId = getServer().getScheduler().scheduleSyncRepeatingTask(this, CycleUtil.getCheckTask(), 0, 10);
		
		spectators.clear();
		gameSessions.clear();
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.setGameMode(GameMode.CREATIVE);
			spectators.put(p.getName(), p);
		}

		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	
		
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cycle wasteland");
	}

	@Override
	public void onDisable() {
		try {

			Bukkit.getScheduler().cancelTask(callId);

			getMap().unload();
			CycleUtil.cycleNext(false, null, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initalizeConfiguration() {

		try {
			configuration = new Configuration(true, this, "config.yml");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void initalizeMySQL(boolean clear) {

		try {
			storage = new Storage(configuration.getConfig().getString("MySQL.Hostname"), configuration.getConfig().getString("MySQL.Username"), configuration.getConfig().getString("MySQL.Password"), configuration.getConfig().getString("MySQL.Database"));

			if (clear) {
				storage.executeQuery("DELETE FROM servers_users");
			}

			//WebRequest.sendRequest("nousersonline", configuration.getConfig().getString("Server").toLowerCase());
			//storage.executeQuery("DELETE FROM servers_users WHERE id = '" + configuration.getConfig().getString("Server") + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void handlers() {
		this.getServer().getPluginManager().registerEvents(new MiscListener(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		this.getServer().getPluginManager().registerEvents(new EntityListener(), this);
		this.getServer().getPluginManager().registerEvents(new CreatureListener(), this);
		this.getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		this.getServer().getPluginManager().registerEvents(new BlockListener(), this);
	}

	public void commands() {
		this.getCommand("join").setExecutor(new JoinCommand());
		this.getCommand("spectate").setExecutor(new SpectateCommand());
		this.getCommand("matchinfo").setExecutor(new MatchInfoCommand());
		this.getCommand("pin").setExecutor(new PinCommand());
		this.getCommand("register").setExecutor(new AuthCommand());
		this.getCommand("cycle").setExecutor(new CycleCommand());
		this.getCommand("score").setExecutor(new ScoreCommand());
		this.getCommand("t").setExecutor(new TeamChatCommand());
		this.getCommand("report").setExecutor(new ReportCommand());
	}

	public static Session getSession(Player player) {
		return HyperPVP.getGameSessions().get(player.getName());
	}

	public static void setListName(ChatColor color, Player player) {

		if (player.getName().length() >= 14) {
			player.setPlayerListName(color + player.getName().substring(0, 14));
		}else {
			player.setPlayerListName(color + player.getName());
		}
	}

	public static WorldsHolder getGroupManager() {
		return groupManager;
	}

	public static WorldsHolder getWorldsHolder() {

		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("GroupManager");

		if (p != null) {
			if (!Bukkit.getServer().getPluginManager().isPluginEnabled(p)) {
				Bukkit.getServer().getPluginManager().enablePlugin(p);
			}
		}

		GroupManager gm = (GroupManager) p;
		WorldsHolder wd = gm.getWorldsHolder();

		return wd;
	}

	public static HyperPVP getJavaPlugin() {
		return plugin;
	}

	public static Game getGame() {
		return game;
	}

	public static Storage getStorage() {
		return storage;
	}

	/*public static World getDefaultWorld() {
		return Bukkit.getWorld("world");
	}

	public static Location getWorldSpawn() {
		return new Location(getDefaultWorld(), 31, 54, -24);
	}*/

	public static World getGameWorld() {
		return getGame().getMapManager().getCurrentWorld();
	}

	public static GameMap getMap() {
		return getGame().getMapManager().getGameMap();
	}

	public static ConcurrentHashMap<String, Player> getSpectators() {
		return spectators;
	}

	public static ConcurrentHashMap<String, Session> getGameSessions() {
		return gameSessions;
	}

	public static Random getRandom() {
		return random;
	}

	public static Map<ThreadType, IThread> getThreads() {
		return threads;
	}

	public static Boolean getCannotJoin() {
		return cannotJoin;
	}

	public static void setCannotJoin(Boolean cannotJoin) {
		HyperPVP.cannotJoin = cannotJoin;
	}

	public static Boolean isCycling() {
		return isCycling;
	}

	public static void setCycling(Boolean deny) {
		HyperPVP.isCycling = deny;
	}

	public static TeamMap getWinningTeam() {
		return winningTeam;
	}

	public static void setWinningTeam(TeamMap winningTeam) {
		HyperPVP.winningTeam = winningTeam;
	}

	public static Integer getMinutesLeft() {
		return minutesLeft;
	}

	public static Configuration getConfiguration() {
		return configuration;
	}


	public static void setMinutesLeft(Integer minutesLeft) {
		HyperPVP.minutesLeft = minutesLeft;
	}

	public static String capitalize(String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}

	public static int getTime() {

		/*if (getMap().getType() == GameType.DTC) {
			return 30;
		}

		if (getMap().getType() == GameType.DTM) {
			return 20;
		}

		if (getMap().getType() == GameType.TDM) {
			return 15;
		}

		if (getMap().getType() == GameType.FFA) {
			return 10;
		 */
		return getMap().getTime();
	}

	public static GameMap getPreviousWorld() {
		return previousWorld;
	}

	public static void setPreviousWorld(GameMap previousWorld) {
		HyperPVP.previousWorld = previousWorld;
	}

	public static Boolean getNeedsCycleThread() {
		return needsCycleThread;
	}

	public static void setNeedsCycleThread(Boolean needsCycleThread) {
		HyperPVP.needsCycleThread = needsCycleThread;
	}

	public static Boolean getNeedsGameThread() {
		return needsGameThread;
	}

	public static void setNeedsGameThread(Boolean needsGameThread) {
		HyperPVP.needsGameThread = needsGameThread;
	}

	public static Map<Player, ChatColor> getTeamCycle() {
		return teamCycle;
	}

	public static Boolean hasMatchBeenAnnounced() {
		return hasMatchBeenAnnounced;
	}

	public static void setMatchBeenAnnounced(Boolean hasMatchBeenAnnounced) {
		HyperPVP.hasMatchBeenAnnounced = hasMatchBeenAnnounced;
	}

	public static Boolean getNeedsMatchCheck() {
		return needsMatchCheck;
	}

	public static void setNeedsMatchCheck(Boolean needsMatchCheck) {
		HyperPVP.needsMatchCheck = needsMatchCheck;
	}

	public static String getTimeString() {
		return timeString;
	}

	public static void setTimeString(String timeString) {
		HyperPVP.timeString = timeString;
	}


}
