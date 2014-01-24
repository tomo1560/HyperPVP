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
package us.hyperpvp.game.map;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.kitteh.tag.TagAPI;

import us.hyperpvp.HyperPVP;
import us.hyperpvp.game.GameSpawns;
import us.hyperpvp.game.GameType;
import us.hyperpvp.game.map.region.Region;
import us.hyperpvp.game.map.region.RegionType;
import us.hyperpvp.game.map.team.TeamMap;
import us.hyperpvp.game.session.Session;
import us.hyperpvp.misc.CycleUtil;

public class GameMap {

	private World world;
	private String worldName;
	private String mapName;
	private GameType type;
	private List<GameSpawns> spawnCoords;
	private List<Region> mapRegions;
	private List<TeamMap> teams;
	private Map<String, TeamMap> playerTeams;

	private double X;
	private double Y;
	private double Z;

	private List<ItemStack> items;
	private long mapid;
	private int time;
	/*private Scoreboard scoreboard;
	private Objective objective;*/
	private String author;
	private List<String> features;
	private int maxPlayers;
	//private Map<TeamMap, List<Region>> monuments;
	//private Map<GameObjects, Region> objectives;

	public GameMap(FileConfiguration conf, int mapId, GameType type, int time, World world, String mapName, String author, String worldName, Location spawn, List<GameSpawns> spawnCoords, List<TeamMap> teams, int maxPerTeam, List<ItemStack> items, List<String> specialfeatures) throws IOException {

		this.mapid = mapId;
		this.world = world;
		this.time = time;
		this.worldName = worldName;
		this.type = type;
		this.mapName = mapName;
		this.author = author;
		this.X = spawn.getX();
		this.Y = spawn.getY();
		this.Z = spawn.getZ();

		this.spawnCoords = spawnCoords;
		this.mapRegions = new ArrayList<Region>();
		this.playerTeams = new HashMap<String, TeamMap>();
		//this.monuments = new HashMap<TeamMap, List<Region>>();
		this.teams = teams;
		this.items = items;
		this.maxPlayers = maxPerTeam;
		this.features = specialfeatures;

		try {

			List<Integer> regions = new ArrayList<Integer>();

			for (int i = 0; i < 20; i++) {

				if (conf.contains("Settings.Regions." + i)) {
					regions.add(i);
				}

			}

			for (int id : regions) {

				List<ChatColor> whitelist = new ArrayList<ChatColor>();

				if (!conf.contains("Settings.Regions." + id + ".TeamWhitelist")) {
					System.out.println("amg!! :o " + mapName);
				}
				
				for (String color : conf.getStringList("Settings.Regions." + id + ".TeamWhitelist")) {

					if (color.length() == 0 || color == null) {
						continue;
					}

					whitelist.add(ChatColor.valueOf(color));
				}

				List<Material> blocks = new ArrayList<Material>();

				for (String material : conf.getStringList("Settings.Regions." + id + ".Blocks")) {

					if (material.length() == 0 || material == null) {
						continue;
					}

					blocks.add(Material.valueOf(material));
				}

				int maxX = conf.getInt("Settings.Regions." + id + ".MaxX");
				int maxY = conf.getInt("Settings.Regions." + id + ".MaxY");
				int maxZ = conf.getInt("Settings.Regions." + id + ".MaxZ");

				int minX = conf.getInt("Settings.Regions." + id + ".MinX");
				int minY = conf.getInt("Settings.Regions." + id + ".MinY");
				int minZ = conf.getInt("Settings.Regions." + id + ".MinZ");

				String regionType = conf.getString("Settings.Regions." + id + ".Type");
				String regionAlert = conf.getString("Settings.Regions." + id + ".Alert");

				Region region = new Region(this.world, RegionType.toValue(regionType), whitelist, blocks, regionAlert, maxX, maxY, maxZ, minX, minY, minZ);
				this.mapRegions.add(region);

				/*if (region.getType() == RegionType.DTM && this.type == GameType.DTM) {

					TeamMap map = Helpers.getTeam(region.getTeamWhitelist().get(0));

					if (!this.monuments.containsKey(map)) {
						this.monuments.put(map, new ArrayList<Region>());
						this.monuments.get(map).add(region);
					} else {
						this.monuments.get(map).add(region);
					}

				}*/
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dispose(boolean unload) {

		this.playerTeams.clear();

		for (TeamMap team : this.teams) {
			team.resetKills();
		}

		if (unload) {
			this.unload();
		}
	}

	public boolean isLoaded() {
		for (World w: Bukkit.getServer().getWorlds()) {
			if (w.getName().equals(this.worldName)) {
				return w.getPlayers().size() == 0;
			}
		}
		return false;
	}

	public void load() {
		WorldCreator creator = WorldCreator.name(this.worldName);
		creator.environment(Environment.NORMAL);
		creator.generator("CleanroomGenerator:.");
		this.world = creator.createWorld();
		world.setAutoSave(false);

		//new Location(this.world, this.X, this.Y, this.Z);

	}

	public void unload() {

		/*this.objective.unregister();
		this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);*/

		try {
			Bukkit.unloadWorld(this.worldName, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Bukkit.unloadWorld(this.world, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.world = null;
	}

	@SuppressWarnings("deprecation")
	public void joinGame(Player player, String name) {

		int rank = 1;

		try {
			rank = HyperPVP.getStorage().readInt32("SELECT rank FROM users WHERE username = '" + player.getName() + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}


		if (this.type == GameType.FFA) {

			List<Session> first = this.getTeamMembers(this.teams.get(0).getColor());

			if (first.size() >= this.maxPlayers && rank == 1) {

				System.out.println(this.maxPlayers);
				player.sendMessage(ChatColor.RED + "Teams full - " + ChatColor.GOLD + "hyperpvp.us/shop" + ChatColor.GREEN + " to join full teams or view other servers at " + ChatColor.GREEN + "hyperpvp.us/servers");
				return;
			}

		} else {

			List<Session> first = this.getTeamMembers(this.teams.get(0).getColor());
			List<Session> second = this.getTeamMembers(this.teams.get(1).getColor());

			if ((first.size() + second.size()) >= this.maxPlayers && rank == 1) {

				System.out.println(this.maxPlayers);
				player.sendMessage(ChatColor.RED + "Teams full - " + ChatColor.GOLD + "hyperpvp.us/shop" + ChatColor.GREEN + " to join full teams or view other servers at " + ChatColor.GREEN + "hyperpvp.us/servers");
				return;
			}
		}

		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}

		if (HyperPVP.hasMatchBeenAnnounced()) {
			player.setGameMode(GameMode.SURVIVAL);
		} else {
			player.setGameMode(GameMode.CREATIVE);
		}

		player.getInventory().clear();
		player.updateInventory();

		Session session = new Session(player);

		HyperPVP.getSpectators().remove(player.getName());
		CycleUtil.addGameSession(player, session);

		if (this.type == GameType.FFA) {
			session.setTeam(this.teams.get(0));
		} else {

			if (name != null) {

				TeamMap team = this.getTeamByName(name);
				session.setTeam(team);

			} else {

				List<Session> first = this.getTeamMembers(this.teams.get(0).getColor());
				List<Session> second = this.getTeamMembers(this.teams.get(1).getColor());

				if (first.size() == second.size()) {
					session.setTeam(this.teams.get(0));
				} else {

					if (first.size() > second.size()) {
						session.setTeam(this.teams.get(1));

					} else
						session.setTeam(this.teams.get(0));
				}
			}
		}

		/*if (HyperPVP.hasMatchBeenAnnounced()) {
			player.sendMessage(this.matchInfoToString(player));
		}*/

		player.sendMessage(ChatColor.GRAY + "You joined the " + session.getTeam().getColor() + HyperPVP.capitalize(session.getTeam().getColor().name().toLowerCase().replace("_", " ").replace("dark ", "")) + " Team");

		HyperPVP.setListName(session.getTeam().getColor(), player);

		TagAPI.refreshPlayer(player);

		player.setFallDistance(0F);

		if (HyperPVP.hasMatchBeenAnnounced()) {

			CycleUtil.hidePlayerWhereAppropriate(player, true);
			player.teleport(this.getRandomSpawn(player));
		}

		if (!HyperPVP.hasMatchBeenAnnounced()) {
			player.getInventory().clear();
			player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
			player.updateInventory();
			player.closeInventory();
		}

		try {
			HyperPVP.getStorage().executeQuery("UPDATE servers SET team_one = '" + this.getTeamMembers(this.teams.get(0).getColor()).size() + "' WHERE bungee_name = '" + HyperPVP.getConfiguration().getConfig().getString("Server").toLowerCase() + "'");

			if (this.type != GameType.FFA) {
				HyperPVP.getStorage().executeQuery("UPDATE servers SET team_two = '" + this.getTeamMembers(this.teams.get(1).getColor()).size() + "' WHERE bungee_name = '" + HyperPVP.getConfiguration().getConfig().getString("Server").toLowerCase() + "'");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println(session.toValue());
	}

	public String matchInfoToString(CommandSender player) {

		StringBuilder builder = new StringBuilder();

		player.sendMessage(ChatColor.RED + "------------------ " + ChatColor.AQUA + "Match Info" + ChatColor.RED + " ------------------");

		if (HyperPVP.getMap().getType() == GameType.TDM ||
				HyperPVP.getMap().getType() == GameType.DTM ||
				HyperPVP.getMap().getType() == GameType.DTC ||
				HyperPVP.getMap().getType() == GameType.RTC) {

			player.sendMessage(ChatColor.DARK_AQUA + "Time: " + ChatColor.AQUA + HyperPVP.getTimeString());

			TeamMap one = this.teams.get(0);
			TeamMap two = this.teams.get(1);

			player.sendMessage(one.getColor() + HyperPVP.capitalize(one.getColor().name().toLowerCase().replace("_", " ").replace("dark ", "")) + ChatColor.GRAY + " kills: " + ChatColor.WHITE + one.getKills() + " | " + two.getColor() + HyperPVP.capitalize(two.getColor().name().toLowerCase().replace("_", " ").replace("dark ", "")) + ChatColor.GRAY + " kills: " + ChatColor.WHITE + two.getKills() + " | " + ChatColor.AQUA + "Observers" + ChatColor.GRAY + ": " + ChatColor.WHITE + HyperPVP.getSpectators().size());

			player.sendMessage("");

			String goal = "";

			if (HyperPVP.getMap().getType() == GameType.TDM) {
				goal = ChatColor.AQUA + "(Team Death Match) Get your team the most kills.";
			}

			if (HyperPVP.getMap().getType() == GameType.DTM) {
				goal = ChatColor.AQUA + "(Destroy The Monument) Destory all of other teams obsidian.";
			}

			if (HyperPVP.getMap().getType() == GameType.DTC) {
				goal = ChatColor.AQUA + "(Destroy The Core) Leak their obisdian core.";
			}

			if (HyperPVP.getMap().getType() == GameType.RTC) {
				goal = ChatColor.AQUA + "(Race To Core) Be the first team to destroy the core in the middle.";
			}

			player.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Goal: " + ChatColor.RESET + goal);

		}

		if (HyperPVP.getMap().getType() == GameType.FFA) {

			player.sendMessage(ChatColor.DARK_AQUA + "Time: " + ChatColor.AQUA + HyperPVP.getTimeString());
			player.sendMessage("");
			player.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Goal: " + ChatColor.RESET + ChatColor.AQUA + "Kill everyone, get the highest kill score.");
			player.sendMessage("");

			List<Session> topPlayers = HyperPVP.getMap().getTop();

			if (topPlayers.size() != 0) {

				int i = 1;

				for (Session set : topPlayers) {

					if (i > 10 && set.getKills() != 0) {
						continue;
					}

					player.sendMessage(i + ". " + ChatColor.GOLD + set.getPlayer().getName() + ChatColor.WHITE + " with " + set.getKills() + "!");

					i++;
				}
			}

		}


		return builder.toString();
	}

	@SuppressWarnings("deprecation")
	public void leaveGame(Player player, boolean normalLeave) {

		player.setGameMode(GameMode.CREATIVE);
		//player.setAllowFlight(true);

		player.setHealth(20.0);
		player.setFoodLevel(20);
		player.setLevel(0);

		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}

		//Session session = HyperPVP.getSession(player);
		//session.setInterruptThread(true);

		if (normalLeave) {
			player.sendMessage(ChatColor.AQUA + "You are now spectating!");
			HyperPVP.setListName(ChatColor.AQUA, player);
			player.getInventory().clear();
			player.updateInventory();
		}

		CycleUtil.addSpectator(player, normalLeave);
		HyperPVP.getGameSessions().remove(player.getName());

		CycleUtil.hidePlayerWhereAppropriate(player, false);
		player.getInventory().setHelmet(null);
		player.getInventory().setBoots(null);	
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);

		try {
			HyperPVP.getStorage().executeQuery("UPDATE servers SET team_one = '" + this.getTeamMembers(this.teams.get(0).getColor()).size() + "' WHERE bungee_name = '" + HyperPVP.getConfiguration().getConfig().getString("Server").toLowerCase() + "'");

			if (this.type != GameType.FFA) {
				HyperPVP.getStorage().executeQuery("UPDATE servers SET team_two = '" + this.getTeamMembers(this.teams.get(1).getColor()).size() + "' WHERE bungee_name = '" + HyperPVP.getConfiguration().getConfig().getString("Server").toLowerCase() + "'");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	public Location getRandomSpawn(Player player) {
		ChatColor color = HyperPVP.getSession(player).getTeam().getColor();
		return this.getRandomSpawn(color);
	}

	public Location getRandomSpawn(ChatColor color) {

		try {
			List<GameSpawns> coords = this.getTeamCoords(color);

			GameSpawns coordSpawn = null;

			if (coords.size() == 1) {
				coordSpawn = coords.get(0);
			} else {
				coordSpawn = coords.get(HyperPVP.getRandom().nextInt(coords.size()));
			}

			Location gameSpawn = new Location(this.world, coordSpawn.getX(), coordSpawn.getY(), coordSpawn.getZ());
			return gameSpawn;
		} catch (Exception e) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload");
		}
		return null;
	}	

	public List<Region> getRegions(RegionType type) {

		List<Region> regions = new ArrayList<Region>();
		for (Region region : this.mapRegions) {
			if (region.getType() == type) {
				regions.add(region);
			}
		}

		return regions;

	}

	public List<GameSpawns> getTeamCoords(ChatColor color) {

		List<GameSpawns> teams = new ArrayList<GameSpawns>();
		for (GameSpawns team : this.spawnCoords) {
			if (team.getColor() == color) {
				teams.add(team);
			}
		}
		return teams;
	}

	public List<Session> getTeamMembers(ChatColor color) {

		List<Session> sessions = new ArrayList<Session>();

		for (Session session : HyperPVP.getGameSessions().values()) {
			if (session.getTeam() != null && session.getTeam().getColor() == color) {
				sessions.add(session);
			}
		}

		return sessions;
	}

	public TeamMap getTeamWinning() {

		int first = this.getTeamStats(0);
		int second = this.getTeamStats(1);

		if (first == 0 && second == 0) {
			return null;
		}

		if (first > second) {
			return this.teams.get(0);
		} 

		if (first < second) {
			return this.teams.get(1);
		} 

		return null;
	}

	public List<Session> getTop() {

		List<Session> map = new ArrayList<Session>();

		for (Session session : HyperPVP.getGameSessions().values()) {

			map.add(session);
		}

		Comparator<Session> comparator = new Comparator<Session>() {

			public int compare(Session c1, Session c2) {
				if (c1.getKills() < c2.getKills()) {
					return 1;
				}
				if (c1.getKills() > c2.getKills()) {
					return -1;
				}
				return -1;
			}
		};

		Collections.sort(map, comparator);

		return map;
	}

	public TeamMap getTeamByName(String name) {

		for (TeamMap team : teams) {
			if (team.getColor().name().toLowerCase().replace("_", " ").replace("dark ", "").startsWith(name)) {
				return team;
			}
		}

		return null;

	}

	public int getTeamStats(int index) {
		return this.teams.get(index).getKills();
	}

	public int getCounter() {
		return HyperPVP.getSpectators().size();
	}

	public World getWorld() {

		return world;
	}

	public String getMapName() {
		return mapName;
	}

	public List<GameSpawns> getCoords() {
		return spawnCoords;
	}

	public Location getSpawn() {
		return new Location(this.world, this.X, this.Y, this.Z);
	}

	public void setSpawn(Location spawn) {
	}

	public GameType getType() {
		return type;
	}

	public List<TeamMap> getTeams() {
		return teams;
	}

	public String getWorldName() {
		return this.worldName;
	}

	public List<ItemStack> getItems() {
		return items;
	}

	public long getMap() {
		return this.mapid;
	}

	public int getTime() {
		return time;
	}

	public String getAuthor() {
		return author;
	}

	public List<String> getFeatures() {
		return features;
	}

	public int getMaxPerTeam() {
		return maxPlayers;
	}


	/*public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void setScoreboard(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

	public Objective getObjective() {
		return objective;
	}

	public void setObjective(Objective objective) {
		this.objective = objective;
	}*/
}
