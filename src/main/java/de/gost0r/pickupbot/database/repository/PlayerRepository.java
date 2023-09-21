package de.gost0r.pickupbot.database.repository;

import de.gost0r.pickupbot.database.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Player.PlayerId> {
}
