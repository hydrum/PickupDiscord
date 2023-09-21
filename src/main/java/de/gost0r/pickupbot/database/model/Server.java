package de.gost0r.pickupbot.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "server")
public class Server {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "ip")
    private String ip;

    @Column(name = "port")
    private int port;

    @Column(name = "rcon")
    private String rcon;

    @Column(name = "password")
    private String password;

    @Column(name = "active")
    private String active;

    @Column(name = "region")
    private String region;
}
