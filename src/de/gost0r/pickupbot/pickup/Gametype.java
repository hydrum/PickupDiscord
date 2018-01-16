package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.List;

public class Gametype {
	
	private String name;
	private boolean active;
	
	private int teamSize;

	private List<String> config;
	
	public Gametype(String name, int teamSize, boolean active) {
		
		this.setName(name);
		this.setActive(active);

		this.setTeamSize(teamSize);
		
		config = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getActive() {
		return active;
	}

	public void setTeamSize(int teamSize) {
		this.teamSize = teamSize;
	}

	public int getTeamSize() {
		return teamSize;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<String> getConfig() {
		return config;
	}

	public void addConfig(String configString) {
		if (!config.contains(configString))	{
			this.config.add(configString);
		}
	}

	public void removeConfig(String configString) {
		config.remove(configString);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Gametype) {
			Gametype gt = (Gametype) o;
			return gt.name == this.name;
		}
		return false;
	}
}
