package de.gost0r.pickupbot.discord.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandHandler extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!CommandStore.commandMap.containsKey(event.getName())) {
            log.warn("Command {} received, but no Handler is registered", event.getName());
            return;
        }
        CommandStore.commandMap.get(event.getName()).accept(event);
    }
}
