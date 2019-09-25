package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
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
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var guild = e.getGuild();
		final var log = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		final var member = e.getMember();
		final var author = e.getAuthor();
		final var channel = e.getChannel();
		final var message = e.getMessage();

		if (member != null && !Utils.hasPerm(member, Permission.ADMINISTRATOR))
			Utils.sendMessage(channel, PermissionError.getErrorMessage("ADMINISTRATOR"), false);
		else
		{
			if (log != null)
			{
				final var question = message.getContentRaw().substring(7);
				Utils.deleteMessage(message);
				channel.sendMessage("Poll: **" + question + "**").queue(m -> {
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
	public final boolean isAdmin() { return true; }
	@Override
	public final String getInvoke() { return "poll"; }
	@Override
	public final Category getCategory() { return Category.UTILITY; }
	@Override
	public final String getUsage() { return "s!poll <question>"; }
}