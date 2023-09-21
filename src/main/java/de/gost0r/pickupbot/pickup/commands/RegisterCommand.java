package de.gost0r.pickupbot.pickup.commands;

import de.gost0r.pickupbot.discord.commands.DiscordCommand;
import de.gost0r.pickupbot.pickup.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class RegisterCommand implements DiscordCommand {

    private final PlayerService service;

    public RegisterCommand(PlayerService service) {
        this.service = service;
    }

    public SlashCommandData registerCommand() {
        return Commands.slash("register", "Register your discord user with a urtauth")
                .setGuildOnly(true)
                .addOption(OptionType.STRING, "urtauth", "Your urtauth name", true);
    }

    public void onCommand(SlashCommandInteractionEvent event) {
        String urtauth = Objects.requireNonNull(event.getOption("urtauth")).getAsString();
        log.debug("{} tried to register {}",
                Objects.requireNonNull(event.getMember()).getEffectiveName(),
                urtauth
        );
        service.registerPlayer(event.getMember().getUser(), urtauth, event.deferReply());
    }
}
