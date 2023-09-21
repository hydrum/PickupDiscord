package de.gost0r.pickupbot.database.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "stats")
public class Stats {

    @Id
    @ManyToOne
    @JoinColumn(name = "pim", nullable = false)
    private PlayerInMatch playerInMatch;

    @Column(name = "ip")
    private String ip;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @JoinColumn(name = "score_1")
    private Score scoreFirstHalf;

    @ManyToOne
    @JoinColumn(name = "score_2")
    private Score scoreSecondHalf;
}
