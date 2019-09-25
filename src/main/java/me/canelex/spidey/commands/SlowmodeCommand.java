package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.PermissionError;
import me.canelex.spidey.utils.Utils;

@SuppressWarnings("unused")
public class SlowmodeCommand implements ICommand
{
	@Override
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var member = e.getMember();
		final var channel = e.getChannel();
		if (member != null && !Utils.hasPerm(member, Permission.MANAGE_CHANNEL))
			Utils.sendMessage(channel, PermissionError.getErrorMessage("MANAGE_CHANNEL"), false);
		else
		{
			var seconds = 0;
			final var par = e.getMessage().getContentRaw().substring(11);
			if (par.equals("off") || par.equals("false"))
				seconds = 0;
			else
			{
				try
				{
					seconds = Math.max(0, Math.min(Integer.parseInt(par), 21600));
				}
				catch (final NumberFormatException ignored)
				{
					Utils.sendMessage(channel, ":no_entry: Couldn't parse argument.", false);
					return;
				}
			}
			channel.getManager().setSlowmode(seconds).queue();
		}
	}

	@Override
	public final String getDescription() { return "Sets the slowmode of the channel. Limit: `21600s` - `6h`. Example - `s!slowmode <seconds | off>`"; }
	@Override
	public final boolean isAdmin() { return true; }
	@Override
	public final String getInvoke() { return "slowmode"; }
	@Override
	public final Category getCategory() { return Category.MODERATION; }
	@Override
	public final String getUsage() { return "s!slowmode <seconds/off>"; }
}