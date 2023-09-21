package de.gost0r.pickupbot.pickup.commands;

import de.gost0r.pickupbot.discord.commands.DiscordCommand;
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

    public SlashCommandData registerCommand() {
        return Commands.slash("register", "Register your discord user with a urtauth")
                .setGuildOnly(true)
                .addOption(OptionType.STRING, "urtauth", "Your urtauth name", true);
    }

    public void onCommand(SlashCommandInteractionEvent event) {
        String urtauth = Objects.requireNonNull(event.getOption("urtauth")).getAsString();
        log.info("{} tried to register {}",
                Objects.requireNonNull(event.getMember()).getEffectiveName(),
                urtauth
        );
        event.reply("User tried to register " + urtauth).queue();
    }
}
