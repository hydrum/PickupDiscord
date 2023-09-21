package de.gost0r.pickupbot.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Data
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

    @Column(name = "eloChange")
    private int eloChange;

    @Column(name = "active")
    private String active;

    @Column(name = "country")
    private String country;

    @Column(name = "enforce_ac")
    private String enforceAc;

    @Column(name = "coins")
    private int coins;

    @Column(name = "eloboost")
    private int eloBoost;

    @Column(name = "mapvote")
    private int mapVote;

    @Column(name = "mapban")
    private int mapBan;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerId implements Serializable {
        private String userId;

        private String urtauth;
    }
}
