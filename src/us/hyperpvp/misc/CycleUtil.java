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
package us.hyperpvp.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.kitteh.tag.TagAPI;

import us.hyperpvp.HyperPVP;
import us.hyperpvp.game.GameType;
import us.hyperpvp.game.map.GameMap;
import us.hyperpvp.game.map.team.TeamMap;
import us.hyperpvp.game.map.team.TeamColor;
import us.hyperpvp.game.session.Session;
import us.hyperpvp.thread.CycleThread;
import us.hyperpvp.thread.FightThread;
import us.hyperpvp.thread.misc.IThread;
import us.hyperpvp.thread.misc.ThreadType;

public class CycleUtil {

	@SuppressWarnings("deprecation")
	public static void resetSpectatorInventory(Player player) {

		player.getInventory().clear();

		/*ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setTitle(ChatColor.RESET + "" + ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to Hyper PVP");
		bookMeta.setAuthor("Quackdot");
		List<String> pages = new ArrayList<String>();
		pages.add("Welcome to HyperPVP.\n\nRight you are in the spawn hub.\n\nTo join a game type /join\n\nTo watch others type /spectate");
		pages.add("Please note you can see your kills at\n\nhttp://hyperpvp.us/profile/" + player.getName());
		pages.add("Hope you have fun! :)");
		bookMeta.setPages(pages);
		book.setItemMeta(bookMeta);
		player.getInventory().addItem(book);*/

		/*ItemStack clock = new ItemStack(Material.WATCH);
		ItemMeta clockMeta = clock.getItemMeta();
		clockMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "The Magic Clock");//
		clockMeta.setLore(Arrays.asList(new String[] {ChatColor.RESET + "" + ChatColor.YELLOW + "Hides all other spectators."}));
		clock.setItemMeta(clockMeta);

		player.getInventory().addItem(clock);*/

		player.getInventory().setItem(0, new ItemStack(Material.COMPASS, 1));

		ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "" + ChatColor.BOLD + "Team Selection");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.RESET + "" + ChatColor.DARK_PURPLE + "Use this star to easily select your team!");
		meta.setLore(lore);
		item.setItemMeta(meta);
		player.getInventory().setItem(1, item);

		item = new ItemStack(Material.ENCHANTED_BOOK);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "What is HyperPVP?");
		lore.clear();
		lore.add(ChatColor.RESET + "" + ChatColor.DARK_PURPLE + "Right click this book to further explain HyperPVP!");
		meta.setLore(lore);
		item.setItemMeta(meta);
		player.getInventory().setItem(2, item);


		player.updateInventory(); 


	}

	@SuppressWarnings("deprecation")
	public static void resetInventory(Player player) {

		player.getInventory().clear();
		player.updateInventory();

		for (ItemStack stack : HyperPVP.getMap().getItems()) {
			player.getInventory().addItem(stack);
		}

		Session session = HyperPVP.getSession(player);

		ItemStack lhelmet = new ItemStack(Material.LEATHER_HELMET, 1);
		LeatherArmorMeta lam = (LeatherArmorMeta)lhelmet.getItemMeta();
		lam.setColor(TeamColor.get(session.getTeam().getColor()));
		lhelmet.setItemMeta(lam);

		player.getInventory().setHelmet(lhelmet);

		lhelmet = new ItemStack(Material.LEATHER_BOOTS, 1);
		lam = (LeatherArmorMeta)lhelmet.getItemMeta();
		lam.setColor(TeamColor.get(session.getTeam().getColor()));
		lhelmet.setItemMeta(lam);

		player.getInventory().setBoots(lhelmet);

		lhelmet = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		lam = (LeatherArmorMeta)lhelmet.getItemMeta();
		lam.setColor(TeamColor.get(session.getTeam().getColor()));
		lhelmet.setItemMeta(lam);

		player.getInventory().setChestplate(lhelmet);

		lhelmet = new ItemStack(Material.IRON_LEGGINGS, 1);
		/*lam = (LeatherArmorMeta)lhelmet.getItemMeta();
			lam.setColor(TeamColor.get(session.getTeam().getColor()));
			lhelmet.setItemMeta(lam);*/

		player.getInventory().setLeggings(lhelmet);



		player.updateInventory();
	}

	public static void checkWorlds() {

		if (HyperPVP.checkFirework) {

			for (Entry<Location, Color> set : HyperPVP.fireworkLocation.entrySet()) {

				Firework firework = set.getKey().getWorld().spawn(set.getKey(), Firework.class);
				FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();

				int type = HyperPVP.getRandom().nextInt(3);

				if (type == 0) {
					data.addEffects(FireworkEffect.builder().withColor(set.getValue()).with(Type.BALL_LARGE).build());
				}

				if (type == 1) {
					data.addEffects(FireworkEffect.builder().withColor(set.getValue()).with(Type.CREEPER).build());
				}

				if (type == 2) {
					data.addEffects(FireworkEffect.builder().withColor(set.getValue()).with(Type.STAR).build());
				}

				data.setPower(2);
				firework.setFireworkMeta(data);
			}

			HyperPVP.checkFirework = false;
			HyperPVP.fireworkLocation.clear();
		}

		if (!HyperPVP.isCycling) {
			for (Player player : Bukkit.getOnlinePlayers()) {

				if (player.getWorld() != HyperPVP.getMap().getWorld()/* && player.getWorld() != HyperPVP.getDefaultWorld()*/)  {

					HyperPVP.setListName(ChatColor.AQUA, player);
					resetSpectatorInventory(player);
					player.teleport(HyperPVP.getMap().getSpawn());
				}
			}

		}

		if (HyperPVP.isCycling) {
			for (Item entity : HyperPVP.getMap().getWorld().getEntitiesByClass(Item.class)) {
				entity.remove();
			}
		}

		if (HyperPVP.needsCycleThread) {
			HyperPVP.needsCycleThread = false;
			cycleNext(true, null, null);
		}

		if (HyperPVP.needsGameThread) {
			HyperPVP.needsGameThread = false;

			HyperPVP.getTeamCycle().clear();

			for (Player player : Bukkit.getOnlinePlayers()) {
				TagAPI.refreshPlayer(player);
				//TagAPI.refreshPlayer(player);
			}

			for (Monster entity : HyperPVP.getMap().getWorld().getEntitiesByClass(Monster.class)) {
				entity.remove();
			}

			if (HyperPVP.getPreviousWorld() != null ) {
				if (HyperPVP.getPreviousWorld().isLoaded()) {
					HyperPVP.getPreviousWorld().dispose(true);
				}
			}

			HyperPVP.threads.put(ThreadType.FIGHT, new FightThread());
			HyperPVP.threads.get(ThreadType.FIGHT).start();
		}

		if (HyperPVP.needsMatchCheck) {
			HyperPVP.needsMatchCheck = false;

			for (Player player : Bukkit.getOnlinePlayers()) {
				hidePlayerWhereAppropriate(player, true);
			}

		}

		/*if (HyperPVP.needsRestart) {
			try {
				for (Player user : Bukkit.getOnlinePlayers()) {
						user.sendMessage(ChatColor.RED + "Hyper PVP will up again shortly!");

						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream dos = new DataOutputStream(baos);
						dos.writeUTF("Connect");
						dos.writeUTF("lobby");
						user.sendPluginMessage(HyperPVP.getJavaPlugin(), "BungeeCord", baos.toByteArray());
						baos.close();
						dos.close();
					}

					TimeUnit.SECONDS.sleep(5);

					Bukkit.shutdown();

			} catch (Exception e) {
				e.printStackTrace();
			}


		}*/

		/*getDefaultWorld().setTime(200);
		getDefaultWorld().setStorm(false);
		getDefaultWorld().setThundering(false);
		getDefaultWorld().setWeatherDuration(999999);*/

	}

	public static void hidePlayerWhereAppropriate(Player client, boolean newPlayer) {

		for (Player user : Bukkit.getOnlinePlayers()) {

			if (HyperPVP.getSpectators().containsKey(user.getName()) && HyperPVP.getGameSessions().containsKey(client.getName())) {
				client.hidePlayer(user);
				user.showPlayer(client);
			} else if (HyperPVP.getGameSessions().containsKey(user.getName()) && HyperPVP.getSpectators().containsKey(client.getName())) {
				user.hidePlayer(client);
				client.showPlayer(user);
			} else {
				client.showPlayer(user);
				user.showPlayer(client);
			}
		}

		if (HyperPVP.getGameSessions().containsKey(client.getName()) && newPlayer) {
			resetInventory(client);
			client.teleport(HyperPVP.getMap().getRandomSpawn(client));
			client.setGameMode(GameMode.SURVIVAL);
			client.sendMessage(HyperPVP.getMap().matchInfoToString(client));
			//client.setAllowFlight(false);
		}
	}

	public static void addSpectator(Player player, boolean normalLeave) {
		if (!HyperPVP.spectators.containsKey(player.getName())) {
			HyperPVP.spectators.put(player.getName(), player);
		} else {
			HyperPVP.spectators.remove(player.getName());
			HyperPVP.spectators.put(player.getName(), player);
		}

		if (normalLeave) {
			resetSpectatorInventory(player);
		}
	}

	public static void addGameSession(Player player, Session session) {
		if (!HyperPVP.gameSessions.containsKey(player.getName())) {
			HyperPVP.gameSessions.put(player.getName(), session);
		} else {
			HyperPVP.gameSessions.remove(player.getName());
			HyperPVP.gameSessions.put(player.getName(), session);
		}

		resetSpectatorInventory(player);
	}

	public static void cycleNext(boolean thread, TeamMap team, String mapName) {

		if (thread) {

			for (Entry<ThreadType, IThread> set : HyperPVP.threads.entrySet()) {

				if (set.getKey() != ThreadType.ANNOUNCE)
					set.getValue().setCancelled(true);
			}


			//List<Player> teamMembers = new ArrayList<Player>();
			//Player personWhoWon = null;

			if (HyperPVP.getMap().getType() != GameType.FFA) {// && Paintball.isNotMapType(GameType.YOLO)) {

				HyperPVP.winningTeam = HyperPVP.getMap().getTeamWinning();
				HyperPVP.winningPlayer = null;

				if (team != null) {
					HyperPVP.winningTeam = team;
				}

				if (HyperPVP.winningTeam != null) {

					Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + " # # # # # # # # # # # #\n" +
							ChatColor.DARK_PURPLE + "# #    " + ChatColor.GOLD + "Game Over!" + ChatColor.DARK_PURPLE + "    # #\n" +
							ChatColor.DARK_PURPLE + "# # " + HyperPVP.winningTeam.getColor() + HyperPVP.capitalize(HyperPVP.winningTeam.getColor().name().toLowerCase().replace("_", " ").replace("dark ", "")) + " Team wins! " + ChatColor.DARK_PURPLE + "# #\n" +
							ChatColor.DARK_PURPLE + "# # # # # # # # # # # #\n");
				} else {

					Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + " # # # # # # # # # # # #\n" +
							ChatColor.DARK_PURPLE + "# #    " + ChatColor.GOLD + "Game Over!" + ChatColor.DARK_PURPLE + "    # #\n" +
							ChatColor.DARK_PURPLE + "# #    " + ChatColor.GOLD + "No one won!" + ChatColor.DARK_PURPLE + "  # #\n" +
							ChatColor.DARK_PURPLE + "# # # # # # # # # # # #\n");
				}


			} else {

				List<Session> topPlayers = HyperPVP.getMap().getTop();

				Bukkit.broadcastMessage(ChatColor.GRAY + "The top 10 so far players are..");

				int i = 1;

				for (Session set : topPlayers) {

					if (i > 10 && set.getKills() != 0) {
						continue;
					}

					Bukkit.broadcastMessage(i + ". " + ChatColor.GOLD + set.getPlayer().getName() + ChatColor.WHITE + " with " + set.getKills() + "!");

					i++;
				}

				if (topPlayers.size() != 0) {
					
					if (topPlayers.size() > 1) {

						Session inLead = topPlayers.get(0);
						Session inLeadTwo = topPlayers.get(1);

						if (inLead.getKills() == inLeadTwo.getKills()) {
							HyperPVP.winningPlayer = null;
						} else {

							HyperPVP.winningPlayer = topPlayers.get(0).getPlayer();

						}
					} else {
						HyperPVP.winningPlayer = topPlayers.get(0).getPlayer();
					}
				}
			}


			GameMap newMap = null;
			if (mapName == null) {
				newMap = HyperPVP.game.getMapManager().changeWorld();
			} else {
				newMap = HyperPVP.game.getMapManager().changeWorld(mapName);
			}

			Session[] sessions = HyperPVP.gameSessions.values().toArray(new Session[]{});

			for (Session game : sessions) {

				HyperPVP.teamCycle.put(game.getPlayer(), game.getTeam().getColor());

				game.getPlayer().closeInventory();
				game.leaveGame(false);

				//HyperTag.changePlayer(game.getPlayer());
				TagAPI.refreshPlayer(game.getPlayer());
			}

			HyperPVP.gameSessions.clear();

			Bukkit.getScheduler().cancelTask(HyperPVP.callId);

			HyperPVP.setCycling(true);
			HyperPVP.getThreads().put(ThreadType.CYCLE, new CycleThread(HyperPVP.winningTeam, HyperPVP.winningPlayer, newMap));
			HyperPVP.getThreads().get(ThreadType.CYCLE).start();

			HyperPVP.callId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(HyperPVP.getJavaPlugin(), getCheckTask(), 0, 10);

		} else {

			for (Entry<ThreadType, IThread> set : HyperPVP.threads.entrySet()) {
				set.getValue().setCancelled(true);
			}

			HyperPVP.threads.clear();
		}
	}

	public static Runnable getCheckTask() {
		return new Runnable() {
			public void run() {
				CycleUtil.checkWorlds();
			}
		};
	}
}
