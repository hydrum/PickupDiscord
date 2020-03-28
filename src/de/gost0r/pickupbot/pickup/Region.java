package de.gost0r.pickupbot.pickup;

public enum Region {
	EU {
		@Override 
		public String toString() { return "EU"; }
	},
	
	NA {
		@Override 
		public String toString() { return "NA"; }
	},
	
	SA {
		@Override 
		public String toString() { return "SA"; }
	},
	
	AU {
		@Override 
		public String toString() { return "AU"; }
	},
	
	ASIA {
		@Override 
		public String toString() { return "ASIA"; }
	},
	
	AFRICA {
		@Override 
		public String toString() { return "AFRICA"; }
	},
	
	WORLD {
		@Override 
		public String toString() { return "WORLD"; }
	}
}
