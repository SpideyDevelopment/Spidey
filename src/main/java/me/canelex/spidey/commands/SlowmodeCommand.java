package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Message;
import me.canelex.jda.api.entities.TextChannel;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.PermissionError;
import me.canelex.spidey.utils.Utils;

@SuppressWarnings("unused")
public class SlowmodeCommand implements ICommand
{
	@Override
	public final void action(final String[] args, final Message message)
	{
		final var channel = message.getChannel();
		final var requiredPermission = getRequiredPermission();
		if (!Utils.hasPerm(message.getMember(), requiredPermission))
			Utils.sendMessage(channel, PermissionError.getErrorMessage(requiredPermission), false);
		else
		{
			var seconds = 0;
			final var par = message.getContentRaw().substring(11);
			if (!(par.equalsIgnoreCase("off") || par.equalsIgnoreCase("false")))
			{
				try
				{
					seconds = Math.max(0, Math.min(Integer.parseInt(par), TextChannel.MAX_SLOWMODE));
				}
				catch (final NumberFormatException ignored)
				{
					Utils.sendMessage(channel, ":no_entry: Couldn't parse argument.", false);
					return;
				}
			}
			message.getTextChannel().getManager().setSlowmode(seconds).queue();
		}
	}

	@Override
	public final String getDescription()
	{
		final var maxSlowmode = TextChannel.MAX_SLOWMODE;
		final var hours = maxSlowmode / 3600;
		return "Sets the slowmode of the channel. Limit: `" + maxSlowmode + "s` - `" + hours + "h`. Example - `s!slowmode <seconds | off>`";
	}
	@Override
	public final Permission getRequiredPermission() { return Permission.MANAGE_CHANNEL; }
	@Override
	public final String getInvoke() { return "slowmode"; }
	@Override
	public final Category getCategory() { return Category.MODERATION; }
	@Override
	public final String getUsage() { return "s!slowmode <seconds/off>"; }
}