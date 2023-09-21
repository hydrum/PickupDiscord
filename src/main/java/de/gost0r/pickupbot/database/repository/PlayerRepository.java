package de.gost0r.pickupbot.database.repository;

import de.gost0r.pickupbot.database.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Player.PlayerId> {
    Optional<Player> findByUrtauth(String urtauth);

    Optional<Player> findByUserId(String userId);

    boolean existsByUserIdOrUrtauth(String userId, String urtauth);

    @Query("SELECT AVG(elo) FROM Player WHERE active='true'")
    double getAverageElo();
}
