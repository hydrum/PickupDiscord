package de.gost0r.pickupbot.pickup;

import java.util.HashMap;
import java.util.Map;

public class GameMap {
	
	public String name;
	public Map<Gametype, Boolean> gametypeList = new HashMap<Gametype, Boolean>();

	public GameMap(String name) {
		this.name = name;
	}
	
	public void setGametype(Gametype gametype, boolean active) {
		gametypeList.put(gametype, active);	
	}

	public boolean isActiveForGametype(Gametype gametype) {
		if (gametypeList.containsKey(gametype)) {
			return gametypeList.get(gametype);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
