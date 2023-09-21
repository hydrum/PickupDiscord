package de.gost0r.pickupbot.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface DiscordCommand {

    SlashCommandData registerCommand();

    void onCommand(SlashCommandInteractionEvent event);

}
