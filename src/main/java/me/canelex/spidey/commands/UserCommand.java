package me.canelex.spidey.commands;

import me.canelex.jda.api.entities.Message;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.Utils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

@SuppressWarnings("unused")
public class UserCommand implements ICommand
{
	private final Calendar cal = Calendar.getInstance();
	private final SimpleDateFormat date = new SimpleDateFormat("EE, d.LLL Y | HH:mm:ss", new Locale("en", "EN"));

	@Override
	public final void action(final String[] args, final Message message)
	{
		final var author = message.getAuthor();
		final var eb = Utils.createEmbedBuilder(author);
		final var musers = message.getMentionedUsers();
		final var u = musers.isEmpty() ? author : musers.get(0);
		final var guild = message.getGuild();
		final var m = guild.getMember(u);
		final var nick = m.getNickname();
		
		eb.setAuthor("USER INFO - " + u.getAsTag());
		eb.setColor(Color.WHITE);
		eb.setThumbnail(u.getAvatarUrl());
		eb.addField("ID", u.getId(), false);

		if (nick != null)
			eb.addField("Nickname for this guild", nick, false);

		cal.setTimeInMillis(u.getTimeCreated().toInstant().toEpochMilli());
		final var creation = date.format(cal.getTime());
		eb.addField("Account created", creation, true);

		cal.setTimeInMillis(m.getTimeJoined().toInstant().toEpochMilli());
		final var join = date.format(cal.getTime());
		eb.addField("User joined", join, false);

		if (guild.getBoosters().contains(m))
		{
			cal.setTimeInMillis(m.getTimeBoosted().toInstant().toEpochMilli());
			final var boost = date.format(cal.getTime());
			eb.addField("Boosting since", boost, false);
		}

		if (!m.getRoles().isEmpty())
		{
			var i = 0;
			final var s = new StringBuilder();

			for (final var role : m.getRoles())
			{
				i++;
				if (i == m.getRoles().size())
					s.append(role.getName());
				else
					s.append(role.getName()).append(", ");
			}
			eb.addField("Roles [**" + i + "**]", s.toString(), false);
		}
		Utils.sendMessage(message.getChannel(), eb.build());
	}

	@Override
	public final String getDescription() { return "Shows info about you or mentioned user"; }
	@Override
	public final String getInvoke() { return "user"; }
	@Override
	public final Category getCategory() { return Category.INFORMATIVE; }
	@Override
	public final String getUsage() { return "s!user (@someone)"; }
}