package dev.mlnr.spidey.handlers.command;

import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import dev.mlnr.spidey.utils.KSoftAPIHelper;
import dev.mlnr.spidey.utils.StringUtils;
import dev.mlnr.spidey.utils.Utils;
import io.github.classgraph.ClassGraph;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static dev.mlnr.spidey.handlers.command.CooldownHandler.cooldown;
import static dev.mlnr.spidey.handlers.command.CooldownHandler.isOnCooldown;

public class CommandHandler
{
	private static final Map<String, Command> COMMANDS = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
	private static final ClassGraph CLASS_GRAPH = new ClassGraph().acceptPackages("dev.mlnr.spidey.commands");
	private static final String NO_PERMS = "Action can't be completed because you don't have **%s** permission";

	private CommandHandler() {}

	static
	{
		try (final var result = CLASS_GRAPH.scan())
		{
			for (final var cls : result.getAllClasses())
			{
				final var cmd = (Command) cls.loadClass().getDeclaredConstructor().newInstance();
				COMMANDS.put(cmd.getInvoke(), cmd);
				for (final var alias : cmd.getAliases())
					COMMANDS.put(alias, cmd);
			}
		}
		catch (final Exception e)
		{
			LOGGER.error("There was an error while registering the commands!", e);
		}
	}

	public static void handle(final GuildMessageReceivedEvent event, final String prefix)
	{
		final var message = event.getMessage();
		final var content = message.getContentRaw().substring(prefix.length());
		if (content.isEmpty())
		{
			Utils.returnError("Please specify a command", message);
			return;
		}
		final var command = (content.contains(" ") ? content.substring(0, content.indexOf(' ')) : content).toLowerCase();
		final var cmd = COMMANDS.get(command);
		if (cmd == null)
		{
			final var similar = StringUtils.getSimilarCommand(command);
			Utils.returnError("**" + command + "** isn't a valid command. " + (similar == null ? "Check `" + prefix + "help` for a list of commands." : "Did you perhaps mean **" + similar + "**?"), message, false);
			return;
		}
		final var requiredPermission = cmd.getRequiredPermission();
		final var member = message.getMember();
		final var userId = member.getIdLong();
		if (!member.hasPermission(requiredPermission))
		{
			Utils.returnError(String.format(NO_PERMS, requiredPermission.getName()), message);
			return;
		}
		final var guildId = message.getGuild().getIdLong();
		if (isOnCooldown(userId, cmd))
		{
			Utils.returnError("The command is on cooldown", message);
			return;
		}

		// API DEPENDANT COMMANDS HANDLING
		final var channel = message.getTextChannel();
		final var category = cmd.getCategory();
		final var nsfw = category == Category.NSFW;
		if ((category == Category.FUN && cmd.getCooldown() > 0) || nsfw) // if a command has a cooldown, i can assume it requires an api
		{
			if (nsfw && !channel.isNSFW())
			{
				Utils.returnError("You can use nsfw commands only in nsfw channels", message);
				return;
			}
			Utils.sendMessage(channel, KSoftAPIHelper.getImage(cmd.getInvoke(), member, nsfw));
			cooldown(guildId, userId, cmd);
			return;
		}
		//

		final var maxArgs = cmd.getMaxArgs();
		final var tmp = content.split("\\s+", maxArgs > 0 ? maxArgs + 1 : 0);
		final var args = Arrays.copyOfRange(tmp, 1, tmp.length);
		cmd.execute(args, new CommandContext(event));
		cooldown(guildId, userId, cmd);
	}
	
	public static Map<String, Command> getCommands()
	{
		return COMMANDS;
	}
}