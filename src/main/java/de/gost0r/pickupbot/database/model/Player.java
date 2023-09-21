package de.gost0r.pickupbot.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(Player.PlayerId.class)
@Table(name = "player")
public class Player {

    @Id
    @Column(name = "userid")
    private String userId;

    @Id
    @Column(name = "urtauth")
    private String urtauth;

    @Column(name = "elo")
    private int elo;

    @Column(name = "elochange")
    private int eloChange = 0;

    @Column(name = "active")
    private String active;

    @Column(name = "country")
    private String country;

    @Column(name = "enforce_ac")
    private String enforceAc;

    @Column(name = "coins")
    private int coins = 0;

    @Column(name = "eloboost")
    private int eloBoost = 0;

    @Column(name = "mapvote")
    private int mapVote = 0;

    @Column(name = "mapban")
    private int mapBan = 0;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerId implements Serializable {
        private String userId;

        private String urtauth;
    }
}
