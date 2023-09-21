package de.gost0r.pickupbot.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Data
@IdClass(GameMap.GameMapId.class)
@Table(name = "map")
public class GameMap {

    @Id
    @Column(name = "map")
    private String map;

    @Id
    @ManyToOne
    @JoinColumn(name = "gametype", nullable = false)
    private GameType gameType;

    @Column(name = "active")
    private String active;

    @Column(name = "banned_until")
    private int bannedUntil = 0;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameMapId implements Serializable {
        private String map;

        private String gameType;
    }
}
