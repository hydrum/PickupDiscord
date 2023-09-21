package de.gost0r.pickupbot.discord.configuration;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DiscordConfiguration {

    private final String token;

    @Autowired
    public DiscordConfiguration(@Value("${discord.token}") String token) {
        this.token = token;
    }

    @Bean
    public JDA jda(List<ListenerAdapter> listeners) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(this.token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new ReadyListener())
                .build();

        listeners.forEach(jda::addEventListener);

        jda.awaitReady();

        return jda;
    }

    private static class ReadyListener implements EventListener {

        @Override
        public void onEvent(@NotNull GenericEvent event) {
            if (event instanceof ReadyEvent)
                System.out.println("API is ready!");
        }
    }
}