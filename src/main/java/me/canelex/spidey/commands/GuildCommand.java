package me.canelex.spidey.commands;

import me.canelex.jda.api.entities.Emote;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.Utils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class GuildCommand implements ICommand
{
	private final Locale locale = new Locale("en", "EN");
	private final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
	private final SimpleDateFormat date = new SimpleDateFormat("EE, d.LLL Y", locale);
	private final SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss", locale);

	@Override
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var guild = e.getGuild();
		final var eb = Utils.createEmbedBuilder(e.getAuthor());
		eb.setColor(Color.ORANGE);
		eb.setThumbnail(guild.getIconUrl());

		eb.addField("Server Name", guild.getName(), true);
		eb.addField("Server ID", guild.getId(), true);

		eb.addField("Owner", guild.getOwner().getUser().getAsTag(), true);
		eb.addField("Owner ID", guild.getOwnerId(), true);

		eb.addField("Text Channels", "" + guild.getTextChannelCache().size(), true);
		eb.addField("Voice Channels", "" + guild.getVoiceChannelCache().size(), true);

		eb.addField("Members", "" + guild.getMemberCache().size(), true);
		eb.addField("Verification Level", guild.getVerificationLevel().name(), true);

		eb.addField("Boost tier", "" + guild.getBoostTier().getKey(), true);
		eb.addField("Boosts", "" + guild.getBoostCount(), true);

		eb.addField("Region", guild.getRegionRaw(), true);

		cal.setTimeInMillis(guild.getTimeCreated().toInstant().toEpochMilli());
		final var creatdate = date.format(cal.getTime());
		final var creattime = time.format(cal.getTime());
		eb.addField("Creation", String.format( "%s | %s", creatdate, creattime), true);

		if (!Utils.canSetVanityUrl(guild)) //could use ternary here too, but i don't use it because of readability
			eb.addField("Custom invite/Vanity url", "Guild isn't eligible to set vanity url", true);
        else
			eb.addField("Custom invite/Vanity url", guild.getVanityUrl() == null ? "Guild has no vanity url set" : guild.getVanityUrl(), true);

		final var roles = guild.getRoleCache().stream().filter(role -> guild.getPublicRole() != role).collect(Collectors.toList());
        eb.addField("Roles", "" + roles.size(), true);

		final var st = new StringBuilder();

		var ec = 0;
		final var an = guild.getEmotes().stream().filter(Emote::isAnimated).count();

		for (final var emote : guild.getEmotes())
		{
			ec++;
			if (ec == guild.getEmoteCache().size())
				st.append(emote.getAsMention());
			else
				st.append(emote.getAsMention()).append(" ");
		}

		if (ec > 0)
		{
			if (st.length() > 1024)
				eb.addField(String.format("Emotes (**%s** | **%s** animated)", ec, an), "Limit exceeded", false);
			else
				eb.addField(String.format("Emotes (**%s** | **%s** animated)", ec, an), (st.toString().length() == 0) ? "None" : st.toString(), false);
		}

		Utils.sendMessage(e.getChannel(), eb.build());
	}

	@Override
	public final String getDescription() { return "Shows you info about this guild"; }
	@Override
	public final boolean isAdmin() { return false; }
	@Override
	public final String getInvoke() { return "guild"; }
	@Override
	public final Category getCategory() { return Category.INFORMATIVE; }
	@Override
	public final String getUsage() { return "s!guild"; }
}