package de.gost0r.pickupbot.pickup.service;

import de.gost0r.pickupbot.database.model.Player;
import de.gost0r.pickupbot.database.repository.PlayerRepository;
import de.gost0r.pickupbot.pickup.Region;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional
    public void registerPlayer(User user, String urtauth, ReplyCallbackAction reply) {
        if (playerRepository.existsByUserIdOrUrtauth(user.getId(), urtauth)) {
            reply.setContent("Already registered.").queue();
            return;
        }
        if (!urtauth.matches("^[a-z0-9]*$") || urtauth.length() == 32) {
            reply.setContent("Invalid urtauth.").queue();
            return;
        }

        Player player = playerRepository.findById(new Player.PlayerId(user.getId(), urtauth))
                .orElse(Player.builder()
                        .userId(user.getId())
                        .urtauth(urtauth)
                        .elo((int) Math.round(playerRepository.getAverageElo()))
                        .enforceAc("false")
                        .country(Region.WORLD.name())
                        .build());
        player.setActive(String.valueOf(true));

        playerRepository.save(player);
        reply.setContent("User successfully registered").queue();
    }
}
