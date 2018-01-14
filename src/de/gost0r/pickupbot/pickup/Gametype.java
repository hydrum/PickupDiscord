package de.gost0r.pickupbot.pickup;

public class Gametype {
	
	private String name;
	private boolean active;
	
	public Gametype(String name, boolean active) {
		
		this.setName(name);
		this.setActive(active);
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
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Gametype) {
			Gametype gt = (Gametype) o;
			return gt.name == this.name;
		}
		return false;
	}
}
