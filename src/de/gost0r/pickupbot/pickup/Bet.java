package de.gost0r.pickupbot.pickup;

import org.json.JSONObject;

public class Bet {
    public int matchid;
    public Player player;
    public String color;
    public int amount;
    public float odds;
    public boolean open;
    public boolean won;

    public static PickupLogic logic;
    public static JSONObject coinEmoji = new JSONObject().put("name", "pugcoin").put("id", "1079910771342979092");
    public static JSONObject blueCoinEmoji = new JSONObject().put("name", "pugcoin_blue").put("id", "1079910838330208339");
    public static JSONObject purpleCoinEmoji = new JSONObject().put("name", "pugcoin_purple").put("id", "1079910887541973042");

    public Bet(int matchid, Player p, String color, int amount, float odds){
        this.matchid = matchid;
        this.player = p;
        this.color = color;
        this.amount = amount;
        this.odds = odds;
        this.open = true;
    }

    public void place(Match match){
        boolean allIn = amount == player.getCoins();
        player.spendCoins(amount);
        JSONObject emoji = getCoinEmoji(amount);
        String msg = Config.bets_place;
        msg = msg.replace(".player.", player.getDiscordUser().getMentionString());
        msg = msg.replace(".amount.", String.valueOf(amount));
        msg = msg.replace(".emojiname.", emoji.getString("name"));
        msg = msg.replace(".emojiid.", emoji.getString("id"));

        if (allIn){
            msg +=" **ALL IN!!**";
            logic.bot.sendMsg(logic.getChannelByType(PickupChannelType.PUBLIC), msg);
            return;
        }
        logic.bot.sendMsg(match.threadChannels, msg);
    }

    public void enterResult(boolean result){
        won = result;
        open = false;

        if (won){
            int wonAmount = Math.round(amount * odds);
            player.addCoins(wonAmount);
        }
        player.saveWallet();
        logic.db.createBet(this);
    }

    public void refund(Match match){
        player.addCoins(amount);
        open = false;
        String msg = Config.bets_refund;
        JSONObject emoji = getCoinEmoji(amount);
        msg = msg.replace(".player.", player.getDiscordUser().getMentionString());
        msg = msg.replace(".amount.", String.valueOf(amount));
        msg = msg.replace(".emojiname.", emoji.getString("name"));
        msg = msg.replace(".emojiid.", emoji.getString("id"));
        logic.bot.sendMsg(match.threadChannels, msg);
    }

    public static JSONObject getCoinEmoji(int amount){
        if (amount <= 9999){
            return coinEmoji;
        }
        else if (amount <= 99999){
            return blueCoinEmoji;
        }
        return purpleCoinEmoji;
    }
}
