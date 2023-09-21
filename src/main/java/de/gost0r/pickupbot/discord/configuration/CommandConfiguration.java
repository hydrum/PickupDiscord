package de.gost0r.pickupbot.discord.configuration;

import de.gost0r.pickupbot.discord.commands.CommandStore;
import de.gost0r.pickupbot.discord.commands.DiscordCommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class CommandConfiguration {

    @Autowired
    public CommandConfiguration(JDA jda, List<DiscordCommand> commands) {
        CommandListUpdateAction commandList = jda.updateCommands();
        for (DiscordCommand command : commands) {
            SlashCommandData commandData = command.registerCommand();
            CommandStore.commandMap.putIfAbsent(commandData.getName(), command::onCommand);

            commandList = commandList.addCommands(commandData);
        }
        commandList.complete();

        // delete on demand
        List<Command> existingCommands = jda.retrieveCommands().complete();
        for (Command cmd : existingCommands) {
            log.info("Existing commands: {}", cmd.getName());
            if (CommandStore.commandMap.containsKey(cmd.getName())) {
                //cmd.delete().queue();
            }
        }
    }
}
