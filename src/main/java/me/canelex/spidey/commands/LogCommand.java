package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.MySQL;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.PermissionError;
import me.canelex.spidey.utils.Utils;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class LogCommand implements ICommand
{
	@Override
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var member = e.getMember();
		final var guild = e.getGuild();
		final var idLong = guild.getIdLong();
		final var channel = e.getChannel();

		if (member != null && Utils.hasPerm(member, Permission.ADMINISTRATOR))
		{
			Utils.deleteMessage(e.getMessage());
			if (guild.getSystemChannel() != null)
				guild.getManager().setSystemChannel(null).queue();

			if (MySQL.getChannel(idLong) == channel.getIdLong())
			{
				final var defaultChannel = guild.getDefaultChannel();
				MySQL.upsertChannel(idLong, defaultChannel.getIdLong());
				channel.sendMessage(":white_check_mark: Log channel has been set to " + defaultChannel.getAsMention() + ". Type this command again in the channel you want to set as the log channel.").queue(m -> m.delete().queueAfter(5,  TimeUnit.SECONDS));
			}
			else
			{
				MySQL.upsertChannel(idLong, channel.getIdLong());
				channel.sendMessage(":white_check_mark: Log channel has been set to " + channel.getAsMention() + ". Type this command again to set the log channel to default guild channel.").queue(m -> m.delete().queueAfter(5,  TimeUnit.SECONDS));
			}
		}
		else
			Utils.sendMessage(channel, PermissionError.getErrorMessage("ADMINISTRATOR"), false);
	}

	@Override
	public final String getDescription() { return "Sets log channel"; }
	@Override
	public final boolean isAdmin() { return true; }
	@Override
	public final String getInvoke() { return "log"; }
	@Override
	public final Category getCategory() { return Category.MODERATION; }
	@Override
	public final String getUsage() { return "s!log"; }
}