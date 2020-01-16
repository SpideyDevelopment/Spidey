package me.canelex.spidey;

import me.canelex.jda.api.EmbedBuilder;
import me.canelex.jda.api.Permission;
import me.canelex.jda.api.audit.ActionType;
import me.canelex.jda.api.entities.MessageType;
import me.canelex.jda.api.events.ShutdownEvent;
import me.canelex.jda.api.events.channel.text.TextChannelDeleteEvent;
import me.canelex.jda.api.events.guild.*;
import me.canelex.jda.api.events.guild.member.GuildMemberJoinEvent;
import me.canelex.jda.api.events.guild.member.GuildMemberLeaveEvent;
import me.canelex.jda.api.events.guild.update.GuildUpdateBoostTierEvent;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.jda.api.hooks.ListenerAdapter;
import me.canelex.spidey.objects.command.CommandHandler;
import me.canelex.spidey.objects.invites.WrappedInvite;
import me.canelex.spidey.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("ConstantConditions")
public class Events extends ListenerAdapter
{
	private static final ConcurrentMap<String, WrappedInvite> invitesMap = new ConcurrentHashMap<>();

	public static ConcurrentMap<String, WrappedInvite> getInvites()
	{
		return invitesMap;
	}

	@Override
	public final void onGuildMessageReceived(final GuildMessageReceivedEvent e)
	{
		final var guild = e.getGuild();
		final var message = e.getMessage();
		final var author = e.getAuthor();

		if (message.getContentRaw().startsWith("s!") && !author.isBot())
			CommandHandler.handle(message);

		final var channel = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		if (message.getType() == MessageType.GUILD_MEMBER_BOOST && channel != null)
		{
			Utils.deleteMessage(message);
			final var eb = new EmbedBuilder();
			eb.setAuthor("NEW BOOST");
			eb.setColor(16023551);
			eb.setThumbnail(author.getAvatarUrl());
			eb.setTimestamp(Instant.now());
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
			eb.setTimestamp(Instant.now());
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
			eb.setTimestamp(Instant.now());
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
			eb.setTimestamp(Instant.now());
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
		final var id = guild.getIdLong();
		final var channel = guild.getTextChannelById(MySQL.getChannel(id));
		final var role = guild.getRoleById(MySQL.getRole(id));
		final var userId = user.getId();

		if (channel != null)
		{
			if (role != null)
			{
				final var selfMember = guild.getSelfMember();
				if (!selfMember.canInteract(role) || !Utils.hasPerm(selfMember, Permission.MANAGE_ROLES))
					Utils.sendMessage(channel, "I'm not able to add the joinrole to user **" + user.getAsTag() + "** as i don't have permissions to do so.", false);
				else
					guild.addRoleToMember(userId, role).queue();
			}

			final var eb = new EmbedBuilder();
			eb.setAuthor("USER HAS JOINED");
			eb.setThumbnail(user.getAvatarUrl());
			eb.setColor(Color.GREEN);
			eb.setTimestamp(Instant.now());
			eb.addField("User", "**" + user.getAsTag() + "**", true);
			eb.addField("ID", "**" + userId + "**", true);

			guild.retrieveInvites().queue(invites ->
			{
				for (var invite : invites)
				{
					final var code = invite.getCode();
					final var wrappedInvite = invitesMap.get(code);
					if (invitesMap.containsKey(code) && invite.getUses() > wrappedInvite.getUses())
					{
						wrappedInvite.incrementUses();
						eb.addField("Invite link", "**" + invite.getUrl() + "**", false);
						eb.addField("Inviter", "**" + invite.getInviter().getAsTag() + "**", true);
						break;
					}
				}
				Utils.sendMessage(channel, eb.build());
			});
		}
	}

	@Override
	public final void onGuildReady(@NotNull final GuildReadyEvent e)
	{
		final var guild = e.getGuild();
		Utils.storeInvites(guild);
		Utils.startInvitesCheck(guild);
	}

	@Override
	public final void onGuildUnavailable(@NotNull final GuildUnavailableEvent e)
	{
		Utils.stopInvitesCheck(e.getGuild());
	}

	@Override
	public final void onGuildAvailable(@NotNull final GuildAvailableEvent e)
	{
		Utils.startInvitesCheck(e.getGuild());
	}

	@Override
	public final void onGuildJoin(final GuildJoinEvent e)
	{
		final var guild = e.getGuild();
		Utils.storeInvites(guild);
		Utils.startInvitesCheck(guild);
	}

	@Override
	public final void onGuildLeave(final GuildLeaveEvent e)
	{
		final var guild = e.getGuild();
		final var id = guild.getIdLong();
		invitesMap.entrySet().removeIf(entry -> entry.getValue().getGuildId() == id);
		MySQL.removeChannel(id);
		Utils.stopInvitesCheck(guild);
	}

	@Override
	public final void onTextChannelDelete(final TextChannelDeleteEvent e)
	{
		final var id = e.getGuild().getIdLong();
		if (e.getChannel().getIdLong() == MySQL.getChannel(id))
			MySQL.removeChannel(id);
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
			eb.setTimestamp(Instant.now());
			eb.addField("Boost tier", "**" + e.getNewBoostTier().getKey() + "**", true);
			eb.addField("Boosts", "**" + guild.getBoostCount() + "**", true);
			Utils.sendMessage(channel, eb.build());
		}
	}

	@Override
	public final void onShutdown(@NotNull final ShutdownEvent e)
	{
		invitesMap.clear();
	}
}