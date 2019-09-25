package me.canelex.spidey.commands;

import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.Utils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

@SuppressWarnings("unused")
public class UserCommand implements ICommand
{
	private final Locale locale = new Locale("en", "EN");
	private final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
	private final SimpleDateFormat date = new SimpleDateFormat("EEEE, d.LLLL Y", locale);
	private final SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss", locale);

	@Override
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var author = e.getAuthor();
		final var eb = Utils.createEmbedBuilder(author);
		final var musers = e.getMessage().getMentionedUsers();
		final var u = musers.isEmpty() ? author : musers.get(0);
		final var guild = e.getGuild();
		final var m = guild.getMember(u);
		final var nick = m.getNickname();
		
		eb.setAuthor("USER INFO - " + u.getAsTag());
		eb.setColor(Color.WHITE);
		eb.setThumbnail(u.getAvatarUrl());
		eb.addField("ID", u.getId(), false);

		if (nick != null)
			eb.addField("Nickname for this guild", nick, false);

		cal.setTimeInMillis(u.getTimeCreated().toInstant().toEpochMilli());
		final var creatdate = date.format(cal.getTime());
		final var creattime = time.format(cal.getTime());

		eb.addField("Account created", String.format( "%s | %s UTC", creatdate, creattime), false);

		cal.setTimeInMillis(m.getTimeJoined().toInstant().toEpochMilli());
		final var joindate = date.format(cal.getTime());
		final var jointime = time.format(cal.getTime());

		eb.addField("User joined", String.format( "%s | %s UTC", joindate, jointime), false);

		if (guild.getBoosters().contains(m))
		{
			cal.setTimeInMillis(m.getTimeBoosted().toInstant().toEpochMilli());
			final var boostdate = date.format(cal.getTime());
			final var boosttime = time.format(cal.getTime());
			eb.addField("Boosting since", String.format("%s | %s UTC", boostdate, boosttime), false);
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
		Utils.sendMessage(e.getChannel(), eb.build());
	}

	@Override
	public final String getDescription() { return "Shows info about you or mentioned user"; }
	@Override
	public final boolean isAdmin() { return false; }
	@Override
	public final String getInvoke() { return "user"; }
	@Override
	public final Category getCategory() { return Category.INFORMATIVE; }
	@Override
	public final String getUsage() { return "s!user (@someone)"; }
}