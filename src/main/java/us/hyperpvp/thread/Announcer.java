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
package us.hyperpvp.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import us.hyperpvp.HyperPVP;
import us.hyperpvp.thread.misc.IThread;

public class Announcer extends IThread {

	private String prefix;
	private List<String> broadcasts;
	private int seconds;
	public Announcer() {
		this.prefix = ChatColor.GRAY + "[" + ChatColor.AQUA + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + ""  + "TIP" + ChatColor.GRAY + "] ";
		this.broadcasts = new ArrayList<String>();
		
		List<String> temp = new ArrayList<String>();
		temp = HyperPVP.getConfiguration().getConfig().getStringList("Broadcast.Messages");
		seconds = HyperPVP.getConfiguration().getConfig().getInt("Broadcast.Interval");
		for (String str : temp) {
			
			String input = str;
			
			for (ChatColor color : ChatColor.values()) {
				input = input.replace("<" + color.name() + ">", color.toString());
			}
			
			this.broadcasts.add(input);
		}
	}
	
	public void dispose() {
		this.broadcasts.clear();
		
	}
	
	@Override
	public void run() {
		
		for (String msg : this.broadcasts) {
			
			if (this.isCancelled()) {
				return;
			}
			
			for (Player player : Bukkit.getOnlinePlayers()) {
				String toSay = msg.replace("{name}", player.getName());
				player.sendMessage(this.prefix + toSay);
			
			}
			
			try {
				TimeUnit.SECONDS.sleep(seconds);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
		
		if (this.isCancelled()) {
			return;
		}
		
		this.run();
		
	}
	
}
