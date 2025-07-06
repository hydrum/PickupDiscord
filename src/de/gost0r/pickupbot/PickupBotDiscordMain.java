package de.gost0r.pickupbot;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.ftwgl.FtwglAPI;
import de.gost0r.pickupbot.pickup.Country;
import de.gost0r.pickupbot.pickup.PickupBot;
import io.github.cdimascio.dotenv.Dotenv;
import io.sentry.Sentry;
import org.json.JSONException;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.*;

public class PickupBotDiscordMain {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static String env;
    private static Dotenv dotenv;

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        try {
            if (args.length > 0 && args[0].equals("dev")) {
                dotenv = Dotenv.configure().filename(".dev.env").load();
                env = "dev";
            } else {
                dotenv = Dotenv.configure().filename(".prod.env").load();
                env = "prod";
            }

            setupLogger();

            Country.initCountryCodes();

            DiscordBot.setToken(dotenv.get("DISCORD_TOKEN"));
            DiscordBot.setApplicationId(dotenv.get("DISCORD_APPLICATION_ID"));
            FtwglAPI.setupCredentials(dotenv.get("FTW_URL"), dotenv.get("FTW_KEY"));
            PickupBot bot = new PickupBot();
            bot.init(env);


            while (true) {
                Thread.sleep(5000);
            }

        } catch (IOException | JSONException | SecurityException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "Exception: ", e);
            Sentry.captureException(e);
        }
    }

    public static void setupLogger() throws SecurityException, IOException {

        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s - %2$s(): %5$s%6$s%n");
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.WARNING);

        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.WARNING);
        logger.addHandler(handler);

        FileHandler logfile = new FileHandler("bot.log");
        logfile.setFormatter(new SimpleFormatter());
        logfile.setLevel(Level.WARNING);
        logger.addHandler(logfile);

        Sentry.init(dotenv.get("SENTRY_DSN") + "?environment=" + env);

        LOGGER.severe("Bot started.");
        Sentry.captureMessage("Bot started");
    }
}
