package de.gost0r.pickupbot.pickup;

public class Gametype {
	
	private String name;
	private boolean active;
	
	private String config;
	private int half;
	
	public Gametype(String name, boolean active) {
		
		this.setName(name);
		this.setActive(active);
		
		config = "";
		half = 1;
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

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public int getHalf() {
		return half;
	}

	public void setHalf(int half) {
		this.half = half;
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
