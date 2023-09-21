package de.gost0r.pickupbot.database.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "bets")
public class Bet {

    @Id
    @Column(name = "ID")
    private int id;

    @ManyToOne
    @JoinColumns(value = {
            @JoinColumn(name = "player_userid", referencedColumnName = "userId"),
            @JoinColumn(name = "player_urtauth", referencedColumnName = "urtauth")})
    private Player player;

    @ManyToOne
    @JoinColumn(name = "matchid", nullable = false)
    private Match match;

    @Column(name = "team")
    private int team;

    @Column(name = "won")
    private String won;

    @Column(name = "amount")
    private int amount;

    @Column(name = "odds")
    private double odds;
}
