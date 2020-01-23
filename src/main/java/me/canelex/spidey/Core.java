package me.canelex.spidey;

import me.canelex.jda.api.JDA;
import me.canelex.jda.api.JDABuilder;
import me.canelex.jda.api.OnlineStatus;
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
	private static JDA jda;

	public static void main(final String[] args)
	{
		try
		{
			jda = new JDABuilder(Secrets.TOKEN)
					.addEventListeners(new Events())
					.setStatus(OnlineStatus.DO_NOT_DISTURB)
					.build()
					.awaitReady();
		}
		catch (final Exception e)
		{
			LOG.error("There was an error while building JDA!", e);
		}
        Utils.startup();
	}

	public static JDA getJDA()
	{
		return jda;
	}

	public static Map<String, ICommand> getCommands()
	{
		return commands;
	}
}