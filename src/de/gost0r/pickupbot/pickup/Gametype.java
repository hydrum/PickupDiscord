package de.gost0r.pickupbot.pickup;

public class Gametype {
	
	public static Gametype CTF = new Gametype("CTF");
	public static Gametype TS = new Gametype("TS");

	private String name;
	private boolean active = true;
	
	public Gametype(String name) {
		
		this.setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
