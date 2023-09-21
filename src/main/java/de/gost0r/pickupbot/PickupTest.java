package de.gost0r.pickupbot;

import de.gost0r.pickupbot.pickup.PickupLogic;

public class PickupTest {

    public static void main(String[] args) throws Exception {
//		String config = new String(Files.readAllBytes(Paths.get("config.json")));
//		JSONObject cfg = new JSONObject(config);
//		DiscordBot.setToken(cfg.getString("token"));
//		
//		Database db = new Database(null);
//		
//		Player.db = db;
//		
//		Player p = Player.get("zmb");
////		String msg = Config.pkup_getelo;
////		msg = msg.replace(".urtauth.", p.getUrtauth());
////		msg = msg.replace(".elo.", String.valueOf(p.getElo()));
////		msg = msg.replace(".wdl.", String.valueOf(Math.round(db.getWDLForPlayer(p).calcWinRatio() * 100d)));
////		msg = msg.replace(".position.", String.valueOf(db.getRankForPlayer(p)));
////		msg = msg.replace(".rank.", p.getRank().getEmoji());
//		System.out.println(p.isBanned());

        String duration = "10y";
        long time = PickupLogic.parseDurationFromString(duration);
        System.out.println(Long.MAX_VALUE + "\n" + time);
        System.out.print(duration + " => " + PickupLogic.parseStringFromDuration(time));
    }

}
