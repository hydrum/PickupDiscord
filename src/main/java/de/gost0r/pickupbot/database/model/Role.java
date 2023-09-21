package de.gost0r.pickupbot.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Data
@IdClass(Role.RoleId.class)
@Table(name = "roles")
public class Role {

    @Id
    @Column(name = "role")
    private String role;

    @Id
    @Column(name = "type")
    private String type;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleId implements Serializable {
        private String role;

        private String type;
    }
}
