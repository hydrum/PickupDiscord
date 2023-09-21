package de.gost0r.pickupbot.database.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "match")
public class Match {

    @Id
    @Column(name = "ID")
    private int id;

    @ManyToOne
    @JoinColumn(name = "server")
    private Server server;

    @Column(name = "state")
    private String status;

    @ManyToOne
    @JoinColumn(name = "gametype", insertable = false, updatable = false)
    private GameType gameType;

    @ManyToOne
    @JoinColumns(value = {
            @JoinColumn(name = "gametype", referencedColumnName = "gameType"),
            @JoinColumn(name = "map", referencedColumnName = "map")})
    private GameMap map;

    @Column(name = "starttime")
    private int startTime;

    @Column(name = "elo_red")
    private int eloRed;

    @Column(name = "elo_blue")
    private int eloBlue;

    @Column(name = "score_red")
    private int scoreRed;

    @Column(name = "score_blue")
    private int scoreBlue;
}
