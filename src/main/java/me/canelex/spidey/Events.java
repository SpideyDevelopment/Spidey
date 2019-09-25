package me.canelex.spidey;

import me.canelex.jda.api.EmbedBuilder;
import me.canelex.jda.api.audit.ActionType;
import me.canelex.jda.api.entities.MessageType;
import me.canelex.jda.api.events.guild.GuildBanEvent;
import me.canelex.jda.api.events.guild.GuildLeaveEvent;
import me.canelex.jda.api.events.guild.GuildUnbanEvent;
import me.canelex.jda.api.events.guild.member.GuildMemberJoinEvent;
import me.canelex.jda.api.events.guild.member.GuildMemberLeaveEvent;
import me.canelex.jda.api.events.guild.update.GuildUpdateBoostTierEvent;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.jda.api.hooks.ListenerAdapter;
import me.canelex.spidey.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@SuppressWarnings("ConstantConditions")
public class Events extends ListenerAdapter
{
	@Override
	public final void onGuildMessageReceived(final GuildMessageReceivedEvent e)
	{
		final var guild = e.getGuild();
		final var message = e.getMessage();
		final var content = message.getContentRaw();
		final var author = e.getAuthor();

		if (content.startsWith("s!") && !author.isBot())
			Core.handleCommand(Core.parser.parse(content, e));

		final var channel = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		if (message.getType() == MessageType.GUILD_MEMBER_BOOST && channel != null)
		{
			Utils.deleteMessage(message);
			final var eb = new EmbedBuilder();
			eb.setAuthor("NEW BOOST");
			eb.setColor(16023551);
			eb.setThumbnail(author.getAvatarUrl());
			eb.addField("Booster", "**" + author.getAsTag() + "**", true);
			eb.addField("Boosts", "**" + guild.getBoostCount() + "**", true);

			Utils.sendMessage(channel, eb.build());
		}
	}

	@Override
	public final void onGuildBan(final GuildBanEvent e)
	{
		final var user = e.getUser();
		final var guild = e.getGuild();

		final var channel = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		if (channel != null)
		{
			final var ban = guild.retrieveBan(user).complete();
			final var auditbans = guild.retrieveAuditLogs().type(ActionType.BAN).complete();
			final var banner = auditbans.get(0).getUser();
			final var eb = new EmbedBuilder();

			var reason = "";
			if (banner != null && banner.equals(e.getJDA().getSelfUser()))
				reason = (ban.getReason().equals("[Banned by Spidey#2370]") ?  "Unknown" : ban.getReason().substring(24));
			else
				reason = (ban.getReason() == null ? "Unknown" : ban.getReason());

			eb.setAuthor("NEW BAN");
			eb.setThumbnail(user.getAvatarUrl());
			eb.setColor(Color.RED);
			eb.addField("User", "**" + user.getAsTag() + "**", true);
			eb.addField("ID", "**" + user.getId() + "**", true);
			eb.addField("Moderator", banner == null ? "Unknown" : banner.getAsMention(), true);
			eb.addField("Reason", "**" + reason + "**", true);

			Utils.sendMessage(channel, eb.build());
		}
	}

	@Override
	public final void onGuildUnban(final GuildUnbanEvent e)
	{
		final var user = e.getUser();
		final var guild = e.getGuild();

		final var channel = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		if (channel != null)
		{
			final var eb = new EmbedBuilder();
			eb.setAuthor("UNBAN");
			eb.setColor(Color.GREEN);
			eb.setThumbnail(user.getAvatarUrl());
			eb.addField("User", "**" + user.getAsTag() + "**", true);
			eb.addField("ID", "**" + user.getId() + "**", true);
			Utils.sendMessage(channel, eb.build());
		}
	}

	@Override
	public final void onGuildMemberLeave(final GuildMemberLeaveEvent e)
	{
		final var user = e.getUser();
		final var guild = e.getGuild();

		final var channel = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		if (channel != null)
		{
			final var eb = new EmbedBuilder();
			eb.setAuthor("USER HAS LEFT");
			eb.setThumbnail(user.getAvatarUrl());
			eb.setColor(Color.RED);
			eb.addField("User", "**" + user.getAsTag() + "**", true);
			eb.addField("ID", "**" + user.getId() + "**", true);
			Utils.sendMessage(channel, eb.build());
		}
	}

	@Override
	public final void onGuildMemberJoin(@NotNull final GuildMemberJoinEvent e)
	{
		final var user = e.getUser();
		final var guild = e.getGuild();

		final var channel = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		if (channel != null)
		{
			final var eb = new EmbedBuilder();
			eb.setAuthor("USER HAS JOINED");
			eb.setThumbnail(user.getAvatarUrl());
			eb.setColor(Color.GREEN);
			eb.addField("User", "**" + user.getAsTag() + "**", true);
			eb.addField("ID", "**" + user.getId() + "**", true);
			Utils.sendMessage(channel, eb.build());
		}
	}

	@Override
	public final void onGuildLeave(final GuildLeaveEvent e)
	{
		MySQL.removeChannel(e.getGuild().getIdLong());
	}

	@Override
	public final void onGuildUpdateBoostTier(final GuildUpdateBoostTierEvent e)
	{
		final var guild = e.getGuild();

		final var channel = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		if (channel != null)
		{
			final var eb = new EmbedBuilder();
			eb.setAuthor("GUILD BOOST TIER HAS CHANGED");
			eb.setColor(16023551);
			eb.addField("Boost tier", "**" + e.getNewBoostTier().getKey() + "**", true);
			eb.addField("Boosts", "**" + guild.getBoostCount() + "**", true);
			Utils.sendMessage(channel, eb.build());
		}
	}
}