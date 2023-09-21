package de.gost0r.pickupbot.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "gametype")
public class GameType {

    @Id
    @Column(name = "gametype")
    private String gameType;

    @Column(name = "teamsize")
    private int teamSize;

    @Column(name = "active")
    private String active;
}
