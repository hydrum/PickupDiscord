package de.gost0r.pickupbot.database.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("de.gost0r.pickupbot.database")
public class JpaConfiguration {
}
