package de.gost0r.pickupbot.database.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "report")
public class PlayerReport {

    @Id
    @Column(name = "ID")
    private int id;

    @ManyToOne
    @JoinColumns(value = {
            @JoinColumn(name = "player_userid", referencedColumnName = "userId"),
            @JoinColumn(name = "player_urtauth", referencedColumnName = "urtauth")})
    private Player player;

    @ManyToOne
    @JoinColumns(value = {
            @JoinColumn(name = "reporter_userid", referencedColumnName = "userId"),
            @JoinColumn(name = "reporter_urtauth", referencedColumnName = "urtauth")})
    private Player reporter;

    @Column(name = "reason")
    private String reason;

    @ManyToOne
    @JoinColumn(name = "match", nullable = false)
    private Match match;
}
