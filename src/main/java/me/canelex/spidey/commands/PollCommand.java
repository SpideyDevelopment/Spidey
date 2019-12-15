package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Message;
import me.canelex.spidey.MySQL;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.Emojis;
import me.canelex.spidey.utils.PermissionError;
import me.canelex.spidey.utils.Utils;

import java.awt.*;

@SuppressWarnings("unused")
public class PollCommand implements ICommand
{
	@Override
	public final void action(final String[] args, final Message message)
	{
		final var guild = message.getGuild();
		final var log = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		final var author = message.getAuthor();
		final var channel = message.getChannel();

		final var requiredPermission = getRequiredPermission();
		if (!Utils.hasPerm(message.getMember(), requiredPermission))
			Utils.sendMessage(channel, PermissionError.getErrorMessage(requiredPermission), false);
		else
		{
			if (log != null)
			{
				final var question = message.getContentRaw().substring(7);
				Utils.deleteMessage(message);
				channel.sendMessage("Poll: **" + question + "**").queue(m ->
				{
					m.addReaction(Emojis.LIKE).queue();
					m.addReaction(Emojis.SHRUG).queue();
					m.addReaction(Emojis.DISLIKE).queue();
					final var eb = Utils.createEmbedBuilder(author);
					eb.setAuthor("NEW POLL");
					eb.setColor(Color.ORANGE);
					eb.addField("Question", "**" + question + "**", false);
					eb.setFooter("Poll created by " + author.getAsTag(), author.getAvatarUrl());
					Utils.sendMessage(log, eb.build());
				});
			}
		}
	}

	@Override
	public final String getDescription() { return "Creates a new poll"; }
	@Override
	public final Permission getRequiredPermission() { return Permission.ADMINISTRATOR; }
	@Override
	public final String getInvoke() { return "poll"; }
	@Override
	public final Category getCategory() { return Category.UTILITY; }
	@Override
	public final String getUsage() { return "s!poll <question>"; }
}