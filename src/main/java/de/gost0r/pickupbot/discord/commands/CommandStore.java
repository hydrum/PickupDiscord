package de.gost0r.pickupbot.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CommandStore {
    public static Map<String, Consumer<SlashCommandInteractionEvent>> commandMap = new HashMap<>();
}
