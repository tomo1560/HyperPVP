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
package us.hyperpvp.game;

import org.bukkit.ChatColor;

public class GameSpawns {

	private long X;
	private long Y;
	private long Z;
	private ChatColor color;

	public GameSpawns(ChatColor color, long x, long y, long z) {
		this.color = color;
		this.X = x;
		this.Y = y;
		this.Z = z;
	}
	
	public long getX() {
		return this.X;
	}
	
	public long getY() {
		return this.Y;
	}
	
	public long getZ() {
		return this.Z;
	}

	public ChatColor getColor() {
		return color;
	}
	
}
