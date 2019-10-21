package me.canelex.spidey.commands;

import me.canelex.jda.api.entities.Emote;
import me.canelex.jda.api.entities.Message;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.Utils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

@SuppressWarnings("unused")
public class GuildCommand implements ICommand
{
	private final Calendar cal = Calendar.getInstance();
	private final SimpleDateFormat date = new SimpleDateFormat("EE, d.LLL Y | HH:mm:ss", new Locale("en", "EN"));

	@Override
	public final void action(final String[] args, final Message message)
	{
		final var eb = Utils.createEmbedBuilder(message.getAuthor());
		final var guild = message.getGuild();
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
		final var creation = date.format(cal.getTime());
		eb.addField("Creation", creation, true);

		final var vanityUrl = guild.getVanityUrl();
		if (!Utils.canSetVanityUrl(guild)) //could use ternary here too, but i don't use it because of readability
			eb.addField("Custom invite/Vanity url", "Guild isn't eligible to set vanity url", true);
        else
			eb.addField("Custom invite/Vanity url", vanityUrl == null ? "Guild has no vanity url set" : vanityUrl, true);

        eb.addField("Roles", "" + (guild.getRoleCache().size() - 1), true);

		final var st = new StringBuilder();

		var ec = 0;
		final var emotes = guild.getEmoteCache();
		final var an = emotes.stream().filter(Emote::isAnimated).count();

		for (final var emote : emotes)
		{
			ec++;
			final var mention = emote.getAsMention();
			if (ec == emotes.size())
				st.append(mention);
			else
				st.append(mention).append(" ");
		}

		if (ec > 0)
		{
			if (st.length() > 1024)
				eb.addField(String.format("Emotes (**%s** | **%s** animated)", ec, an), "Limit exceeded", false);
			else
				eb.addField(String.format("Emotes (**%s** | **%s** animated)", ec, an), (st.toString().length() == 0) ? "None" : st.toString(), false);
		}

		Utils.sendMessage(message.getChannel(), eb.build());
	}

	@Override
	public final String getDescription() { return "Shows you info about this guild"; }
	@Override
	public final String getInvoke() { return "guild"; }
	@Override
	public final Category getCategory() { return Category.INFORMATIVE; }
	@Override
	public final String getUsage() { return "s!guild"; }
}