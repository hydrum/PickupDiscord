package de.gost0r.pickupbot.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "season")
public class Season {

    @Id
    @Column(name = "number")
    private int id;

    @Column(name = "startdate")
    private int startTime;

    @Column(name = "enddate")
    private int endTime;
}
