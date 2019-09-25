package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.PermissionError;
import me.canelex.spidey.utils.Utils;

@SuppressWarnings("unused")
public class SayCommand implements ICommand
{
	@Override
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var member = e.getMember();
		final var message = e.getMessage();
		final var channel = e.getChannel();
		final var channels = message.getMentionedChannels();
		final var target = channels.isEmpty() ? channel : channels.get(0);
		if (member != null && !Utils.hasPerm(member, Permission.BAN_MEMBERS))
			Utils.sendMessage(channel, PermissionError.getErrorMessage("BAN_MEMBERS"), false);
		else
		{
			Utils.deleteMessage(message);
			var toSay = message.getContentRaw().substring(6);
			toSay = toSay.substring(0, toSay.lastIndexOf(' '));
			Utils.sendMessage(target, toSay, false);
		}
	}

	@Override
	public final String getDescription() { return "Spidey will say something for you (in specified channel)"; }
	@Override
	public final boolean isAdmin() { return true; }
	@Override
	public final String getInvoke() { return "say"; }
	@Override
	public final Category getCategory() { return Category.UTILITY; }
	@Override
	public final String getUsage() { return "s!say <toSay>"; }
}