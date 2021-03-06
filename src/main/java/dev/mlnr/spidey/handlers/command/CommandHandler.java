package dev.mlnr.spidey.handlers.command;

import dev.mlnr.spidey.cache.Cache;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import io.github.classgraph.ClassGraph;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static dev.mlnr.spidey.handlers.command.CooldownHandler.cooldown;
import static dev.mlnr.spidey.handlers.command.CooldownHandler.isOnCooldown;

public class CommandHandler {
	private static final Map<String, Command> COMMANDS = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	private CommandHandler() {}

	public static void handle(SlashCommandEvent event, Cache cache) {
		var commandName = event.getName();
		var command = COMMANDS.get(commandName);
		var member = event.getMember();
		var requiredPermission = command.getRequiredPermission();
		var guildId = event.getGuild().getIdLong();
		var i18n = cache.getGuildSettingsCache().getMiscSettings(guildId).getI18n();

		if (!member.hasPermission(requiredPermission)) {
			event.reply(i18n.get("command_failures.no_perms", requiredPermission.getName())).setEphemeral(true).queue();
			return;
		}
		var userId = member.getIdLong();
		if (isOnCooldown(userId, command)) {
			event.reply(i18n.get("command_failures.cooldown")).setEphemeral(true).queue();
			return;
		}
		var vip = cache.getGuildSettingsCache().getGeneralSettings(guildId).isVip();
		var executed = command.execute(new CommandContext(event, command.shouldHideResponse(), i18n, cache));
		if (executed) {
			cooldown(userId, command, vip);
		}
	}

	public static void loadCommands(JDA jda) {
		var commandsUpdate = jda.updateCommands();

		try (var result = new ClassGraph().acceptPackages("dev.mlnr.spidey.commands").scan()) {
			var hideOption = new OptionData(OptionType.BOOLEAN, "hide", "Whether to hide the response");

			for (var cls : result.getAllClasses()) {
				var command = (Command) cls.loadClass().getDeclaredConstructor().newInstance();
				var commandName = command.getInvoke();
				COMMANDS.put(commandName, command);

				var commandData = new CommandData(commandName, command.getDescription());
				commandData.addOptions(command.getOptions());
				commandData.addOptions(hideOption);
				commandsUpdate.addCommands(commandData);
			}
			commandsUpdate.queue();
		}
		catch (Exception e) {
			logger.error("There was an error while registering the commands, exiting", e);
			System.exit(1);
		}
	}

	public static Map<String, Command> getCommands() {
		return COMMANDS;
	}
}