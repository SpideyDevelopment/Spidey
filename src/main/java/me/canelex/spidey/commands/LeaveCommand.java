package me.canelex.spidey.commands;

import me.canelex.jda.api.entities.Message;
import me.canelex.spidey.MySQL;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.Utils;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class LeaveCommand implements ICommand
{
	@Override
	public final void action(final String[] args, final Message message)
	{
		final var guild = message.getGuild();
		final var idLong = guild.getIdLong();
		final var channel = message.getChannel();
		
		if (!message.getMember().isOwner())
			Utils.sendMessage(channel, message.getAuthor().getAsMention() + ", you have to be the guild owner to do this.", false);
		else
		{
			channel.sendMessage("Bye.").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			Utils.deleteMessage(message);
			Utils.sendPrivateMessageFormat(guild.getOwner().getUser(), "I've left your server **%s**. If you'd want to invite me back, please use this URL: ||%s||. Thanks for using **Spidey**!", false, guild.getName(), Utils.getInviteUrl(idLong));
			MySQL.removeChannel(idLong);
			guild.leave().queue();
		}
	}

	@Override
	public final String getDescription() { return "Spidey will leave your server"; }
	@Override
	public final String getInvoke() { return "leave"; }
	@Override
	public final Category getCategory() { return Category.UTILITY; }
	@Override
	public final String getUsage() { return "s!leave"; }
}