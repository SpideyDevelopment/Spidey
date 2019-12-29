package me.canelex.spidey;

import me.canelex.jda.api.JDABuilder;
import me.canelex.jda.api.OnlineStatus;
import me.canelex.jda.api.entities.Activity;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Core
{
	protected static final Map<String, ICommand> commands = new HashMap<>();
	private static final Logger LOG = LoggerFactory.getLogger(Core.class);

	public static void main(final String[] args)
	{
		try
		{
			final var jda = new JDABuilder(Secrets.TOKEN)
                    .addEventListeners(new Events())
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.listening("your commands"));
			for (var i = 0; i < 10; i++)
			{
			    jda.useSharding(i, 10).build().awaitReady();
            }
		}
		catch (final Exception e)
		{
			LOG.error("There was an error while building JDA!", e);
		}
        Utils.registerCommands();
	}

	public static Map<String, ICommand> getCommands()
	{
		return commands;
	}
}