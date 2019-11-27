package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Message;
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
	public final void action(final String[] args, final Message message)
	{
		final var guild = message.getGuild();
		final var idLong = guild.getIdLong();
		final var channel = message.getChannel();

		final var requiredPermission = getRequiredPermission();
		if (Utils.hasPerm(message.getMember(), requiredPermission))
		{
			Utils.deleteMessage(message);
			if (guild.getSystemChannel() != null)
				guild.getManager().setSystemChannel(null).queue();

			if (MySQL.getChannel(idLong) == channel.getIdLong())
			{
				final var defaultChannel = guild.getDefaultChannel();
				MySQL.setChannel(idLong, defaultChannel.getIdLong());
				channel.sendMessage(":white_check_mark: Log channel has been set to " + defaultChannel.getAsMention() + ". Type this command again in the channel you want to set as the log channel.").queue(m -> m.delete().queueAfter(5,  TimeUnit.SECONDS));
			}
			else
			{
				MySQL.setChannel(idLong, channel.getIdLong());
				channel.sendMessage(":white_check_mark: Log channel has been set to <#" + channel.getIdLong() + ">. Type this command again to set the log channel to default guild channel.").queue(m -> m.delete().queueAfter(5,  TimeUnit.SECONDS));
			}
		}
		else
			Utils.sendMessage(channel, PermissionError.getErrorMessage(requiredPermission), false);
	}

	@Override
	public final String getDescription() { return "Sets log channel"; }
	@Override
	public final Permission getRequiredPermission() { return Permission.ADMINISTRATOR; }
	@Override
	public final String getInvoke() { return "log"; }
	@Override
	public final Category getCategory() { return Category.MODERATION; }
	@Override
	public final String getUsage() { return "s!log"; }
}