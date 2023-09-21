package de.gost0r.pickupbot.database.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "banlist")
public class PlayerBan {

    @Id
    @Column(name = "ID")
    private int id;

    @ManyToOne
    @JoinColumns(value = {
            @JoinColumn(name = "player_userid", referencedColumnName = "userId"),
            @JoinColumn(name = "player_urtauth", referencedColumnName = "urtauth")})
    private Player player;

    @Column(name = "reason")
    private String reason;

    @Column(name = "pardon")
    private String pardon;

    @Column(name = "start")
    private int startTime;

    @Column(name = "end")
    private int endTime;

    @Column(name = "forgiven")
    private boolean forgiven;
}
