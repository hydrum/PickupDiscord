package de.gost0r.pickupbot.pickup;

import de.gost0r.pickupbot.discord.DiscordEmbed;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PrivateGroup {
    public Player captain;
    public Gametype gt;
    private ArrayList<Player> playerList = new ArrayList<>();
    public Instant timestamp;

    public PrivateGroup(Player captain, Gametype inputGt) {
        this.captain = captain;
        this.gt = new Gametype(inputGt.getName() + ' ' + captain.getUrtauth(), inputGt.getTeamSize(), true, true);
        updateTimestamp();
        addPlayer(captain);
    }

    public void addPlayer(Player p) {
        if (!playerList.contains(p)) {
            playerList.add(p);
        }
        updateTimestamp();
    }

    public void removePlayer(Player p) {
        playerList.remove(p);
        updateTimestamp();
    }

    public boolean playerInGroup(Player p) {
        return playerList.contains(p);
    }

    public DiscordEmbed getEmbed() {
        DiscordEmbed embed = new DiscordEmbed();
        embed.title = "Private " + gt.getName().split(" ")[0] +  " Group";
        embed.color = 7056881;

        long epochSeconds = timestamp.plus(Duration.ofHours(1)).getEpochSecond();
        embed.description = "This group will expire in <t:" + Long.toString(epochSeconds) +  ":R> if left inactive.";

        StringBuilder playerString = new StringBuilder();
        for (Player p : playerList) {
            playerString.append(p.getDiscordUser().getMentionString());
            playerString.append("\n");
        }

        embed.addField("Captain", captain.getDiscordUser().getMentionString(), true);
        embed.addField("Players (" + Integer.toString(playerList.size()) + ")", playerString.toString(), true);
        return embed;
    }

    public void updateTimestamp() {
        timestamp = Instant.now();
    }
}
