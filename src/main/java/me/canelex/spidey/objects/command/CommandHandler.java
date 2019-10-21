package me.canelex.spidey.objects.command;

import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.Core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandHandler
{
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

	public static void handle(String content, final GuildMessageReceivedEvent e)
	{
		content = content.replace("s!", "");
		if (content.length() != 0)
		{
			final var command = content.contains(" ") ? content.substring(0, content.indexOf(' ')) : content;
			final var commands = Core.commands;
			if (commands.containsKey(command))
			{
				final var cmd = commands.get(command);
				final var args = content.split("\\s+", cmd.getMaxArgs());
				EXECUTOR.submit(() -> cmd.action(args, e.getMessage()));
			}
		}
	}
}