package de.gost0r.pickupbot.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "score")
public class Score {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "kills")
    private int kills = 0;

    @Column(name = "deaths")
    private int deaths = 0;

    @Column(name = "assists")
    private int assists = 0;

    @Column(name = "caps")
    private int caps = 0;

    @Column(name = "returns")
    private int returns = 0;

    @Column(name = "fckills")
    private int flagCarrierKills = 0;

    @Column(name = "stopcaps")
    private int stopCaps = 0;

    @Column(name = "protflag")
    private int protFlag = 0;
}
