package de.gost0r.pickupbot.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Data
@IdClass(Channel.ChannelId.class)
@Table(name = "channels")
public class Channel {

    @Id
    @Column(name = "channel")
    private String channel;

    @Id
    @Column(name = "type")
    private String type;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelId implements Serializable {
        private String channel;

        private String type;
    }
}
