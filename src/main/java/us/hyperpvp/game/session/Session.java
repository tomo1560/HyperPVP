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
package us.hyperpvp.game.session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import us.hyperpvp.HyperPVP;
import us.hyperpvp.game.GameType;
import us.hyperpvp.game.map.team.TeamMap;
import us.hyperpvp.misc.OverideValue;

public class Session extends OverideValue {

	private Player player;
	private TeamMap team;
	private Session lastDamagedBy;
	private boolean brokeMonument;
	private int kills;
	private List<Block> monumentBlockCount;

	private Integer userId;

	public Session(Player player) {
		
		try {
			this.userId = HyperPVP.getStorage().readInt32("SELECT id FROM users WHERE username = '" + player.getName() + "'");
		} catch (SQLException e) {
			this.userId = 0;
		}
		
		this.player = player;
		this.lastDamagedBy = null;
		this.brokeMonument = false;
		this.kills = 0;
		this.monumentBlockCount = new ArrayList<Block>();
	}

	public void leaveGame(boolean normalLeave) {

		HyperPVP.getMap().leaveGame(player, normalLeave);
		this.setDestroyer(false);
		this.resetKills();
		this.resetMonumentBlock();
		this.userId = 0;
		this.lastDamagedBy = null;
		this.monumentBlockCount.clear();
	}

	public void updateStatistics(ScoreType type, Session from) {

		if (this.userId == 0) {
			return;
		}
		
		try {
			java.sql.PreparedStatement statement = HyperPVP.getStorage().queryParams("INSERT INTO `users_statistics` (`from_id`, `to_id`, `type`, `time`, `map`, `mode`) VALUES (?, ?, ?, ?, ?, ?)"); {
				statement.setInt(1, this.getUserId());
				statement.setInt(2, from.getUserId());
				statement.setString(3, type.toString());
				statement.setLong(4, (System.currentTimeMillis() / 1000L));
				statement.setString(5, HyperPVP.getMap().getMapName());
				statement.setString(6, HyperPVP.getMap().getType().getType());
				statement.execute();
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updatetStats(ScoreType type) {

		if (this.userId == 0) {
			return;
		}
		
		try {
			java.sql.PreparedStatement statement = HyperPVP.getStorage().queryParams("INSERT INTO `users_statistics` (`from_id`, `to_id`, `type`, `time`, `map_id`) VALUES (?, ?, ?, ?, ?)"); {
				statement.setInt(1, this.getUserId());
				statement.setInt(2, 0);
				statement.setString(3, type.toString());
				statement.setLong(4, (System.currentTimeMillis() / 1000L));
				statement.setString(5, HyperPVP.getMap().getMapName());
				statement.setString(6, HyperPVP.getMap().getType().getType());
				statement.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int getUserId() {
		return this.userId;
	}

	public String getItemHand(Player killed) {

		if (player.getItemInHand().getType() == Material.WOOD_HOE && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().contains("Paintball Gun")) {
			return "paintball gun";
		} else if (player.getItemInHand().getType() == null) {
			return "fist";
		} else if (player.getItemInHand().getType() == Material.AIR) {
			return "fist";
		} else {
			return player.getItemInHand().getType().name().replace("_", " ").toLowerCase();
		}
	}

	public String getItemHand() {

		if (player.getItemInHand().getType() == null) {
			return "fist";
		} else if (player.getItemInHand().getType() == Material.AIR) {
			return "fist";
		} else {
			return player.getItemInHand().getType().name().replace("_", " ").toLowerCase();
		}
	}

	public String getDeathMessage(PlayerDeathEvent event, boolean pastTense) {
		
		if (player.getLastDamageCause().getCause() == DamageCause.DROWNING) {
			return " drownd";	
		}

		if (player.getLastDamageCause().getCause() == DamageCause.ENTITY_ATTACK) {
			return pastTense ? " were ambushed by mobs" : " was ambushed by mobs";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.ENTITY_EXPLOSION) {
			return " 'sploded";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.FALL) {
			return " hit the ground too hard (" + (int)player.getFallDistance() + " blocks)";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.FALLING_BLOCK) {
			return " suffocated";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.FIRE) {
			return " burnt alive";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.LAVA) {
			return " burnt to a crisp";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.MAGIC) {
			return " died from magic";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.MELTING) {
			return " melted";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.POISON) {
			return " failed the battle of wits";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.STARVATION) {
			return " died from poverty";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.SUFFOCATION) {
			return " suffocated";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.SUICIDE) {
			return " killed himself";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.THORNS) {
			return " didn't see the cactus";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.VOID) {
			return " fell out of the world";
		}

		if (player.getLastDamageCause().getCause() == DamageCause.BLOCK_EXPLOSION) {
			
			String death = " exploded";
			
			/*for (Entry<Detonator, Session> set : HyperPVP.detonators.entrySet()) {
				
				Session killer = set.getValue();
				Session killed = HyperPVP.getSession(event.getEntity().getPlayer());
				
				if (set.getValue().getPlayer() == event.getEntity().getPlayer()) {
					return death;
				}
				
				Location playerLocation = event.getEntity().getLocation();
				Location tntLocation = set.getKey().getExplode();
				
				double distance = playerLocation.distance(tntLocation);
				
				if (distance <= 3) {
					death = " exploded from " + set.getValue().getPlayer().getName() + "'s TNT"; 
				}
				
				if (HyperPVP.getMap().getType() != GameType.FFA) {
					set.getValue().getTeam().killIncrease();
				} else {
					set.getValue().killIncrease();
				}
				
				try {
					// update killed
					int deaths = HyperPVP.getStorage().readInt32("SELECT deaths FROM users WHERE username = '" + killed.getPlayer().getName() + "'");
					HyperPVP.getStorage().executeQuery("UPDATE users SET deaths = '" + (deaths + 1) + "' WHERE username = '" + killed.getPlayer().getName() + "'");

					// update killer
					int kills = HyperPVP.getStorage().readInt32("SELECT kills FROM users WHERE username = '" + killer.getPlayer().getName() + "'");
					HyperPVP.getStorage().executeQuery("UPDATE users SET kills = '" + (kills + 1) + "' WHERE username = '" + killer.getPlayer().getName() + "'");
				
				} catch (SQLException e) {
					e.printStackTrace();
				}

				killed.updateStatistics(ScoreType.DEATH, killer);
				killer.updateStatistics(ScoreType.KILL, killed);
				
			}*/
			
			return death;
		}
		
		if (player.getLastDamageCause().getCause() == DamageCause.WITHER) {
			return pastTense ? " were killed by a wither" : " was killed by a wither";
		} 
		
		
		return pastTense ? " died" : " has died";
		
	}

	public Player getPlayer() {
		return player;

	}

	public TeamMap getTeam() {
		return team;
	}

	public void setTeam(TeamMap team) {
		this.team = team;
	}

	public void setDestroyer(boolean flag) {
		this.brokeMonument = flag;
	}

	public boolean isDestroyer() {
		return this.brokeMonument;
	}

	public Session getLastDamagedBy() {
		return lastDamagedBy;
	}

	public void setLastDamagedBy(Session lastDamagedBy) {
		this.lastDamagedBy = lastDamagedBy;
	}

	public void killIncrease() {
		kills = kills + 1;
	}

	public void killDecrease() {
		//this.kills = this.kills - 1;
	}

	public void resetKills() {
		kills = 0;
	}

	public Integer getKills() {
		return kills;
	}

	public void monumentBlockIncrease(Block block) {
		monumentBlockCount.add(block);
	}

	public void resetMonumentBlock() {
		monumentBlockCount.clear();
	}

	public List<Block> getMonumentBlocks() {
		return this.monumentBlockCount;
	}
	
}
