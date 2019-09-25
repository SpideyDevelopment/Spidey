package me.canelex.spidey.commands;

import me.canelex.jda.api.EmbedBuilder;
import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Message;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.MySQL;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.PermissionError;
import me.canelex.spidey.utils.Utils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class WarnCommand implements ICommand
{
	@Override
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var maxArgs = 3;
		final var msg = e.getMessage();
		final var member = e.getMember();

		msg.delete().queueAfter(5, TimeUnit.SECONDS);

		if (member != null && !Utils.hasPerm(member, Permission.BAN_MEMBERS))
		{
			Utils.sendMessage(e.getChannel(), PermissionError.getErrorMessage("BAN_MEMBERS"), false);
			return;
		}

		final var args = msg.getContentRaw().trim().split("\\s+", maxArgs);
		if (args.length < 3)
		{
			Utils.returnError("Wrong syntax", msg);
			return;
		}

		if (!Message.MentionType.USER.getPattern().matcher(args[1]).matches())
		{
			Utils.returnError("Wrong syntax (no mention)", msg);
			return;
		}

		final var members = msg.getMentionedMembers();
		if (members.isEmpty())
		{
			Utils.returnError("User wasn't found", msg);
			return;
		}
		final var mb = members.get(0);

		if (!member.canInteract(mb))
		{
			Utils.returnError("Can't warn the user due to permission hierarchy position", msg);
			return;
		}

		final var eb = new EmbedBuilder();
		final var guild = e.getGuild();

		final var channel = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		if (channel != null)
		{
			final var user = mb.getUser();
			final var author = e.getAuthor();
			eb.setAuthor("NEW WARN");
			eb.setThumbnail(user.getAvatarUrl());
			eb.addField("User", "**" + user.getAsTag() + "**", true);
			eb.addField("ID", "**" + user.getId() + "**", true);
			eb.addField("Moderator", "**" + author.getAsTag() + "**", true);
			eb.addField("Reason", "**" + args[2] + "**", true);
			eb.setColor(Color.ORANGE);
			Utils.sendMessage(channel, eb.build());
			Utils.sendPrivateMessageFormat(user, ":warning: You've been warned in the guild **%s** from **%s** for **%s**.", guild.getName(), author.getName(), args[2]);
		}
	}

	@Override
	public final String getDescription() { return "Warns user"; }
	@Override
	public final boolean isAdmin() { return true; }
	@Override
	public final String getInvoke() { return "warn"; }
	@Override
	public final Category getCategory() { return Category.MODERATION; }
	@Override
	public final String getUsage() { return "s!warn <@someone> <reason>"; }
}