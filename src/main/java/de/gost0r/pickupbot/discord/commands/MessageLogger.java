package de.gost0r.pickupbot.discord.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class MessageLogger extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            log.info("[DM] {}: {}", event.getAuthor().getName(), event.getMessage().getContentDisplay());
        } else {
            log.info("[{}] #{} {}: {}",
                    event.getGuild().getName(),
                    event.getChannel().getName(),
                    Objects.requireNonNull(event.getMember()).getEffectiveName(),
                    event.getMessage().getContentDisplay());
        }
    }
}
