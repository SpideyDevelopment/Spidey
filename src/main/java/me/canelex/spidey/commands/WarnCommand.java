package me.canelex.spidey.commands;

import me.canelex.jda.api.EmbedBuilder;
import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Message;
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
	public final void action(final String[] args, final Message message)
	{
		final var member = message.getMember();

		message.delete().queueAfter(5, TimeUnit.SECONDS);

		final var requiredPermission = getRequiredPermission();
		if (!Utils.hasPerm(member, requiredPermission))
		{
			Utils.sendMessage(message.getChannel(), PermissionError.getErrorMessage(requiredPermission), false);
			return;
		}
		
		if (args.length < 3)
		{
			Utils.returnError("Wrong syntax", message);
			return;
		}

		if (!Message.MentionType.USER.getPattern().matcher(args[1]).matches())
		{
			Utils.returnError("Wrong syntax (no mention)", message);
			return;
		}

		final var members = message.getMentionedMembers();
		if (members.isEmpty())
		{
			Utils.returnError("User wasn't found", message);
			return;
		}
		final var mb = members.get(0);

		if (!member.canInteract(mb))
		{
			Utils.returnError("Can't warn the user due to permission hierarchy position", message);
			return;
		}

		final var eb = new EmbedBuilder();
		final var guild = message.getGuild();

		final var channel = guild.getTextChannelById(MySQL.getChannel(guild.getIdLong()));
		if (channel != null)
		{
			final var user = mb.getUser();
			final var author = message.getAuthor();
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
	public final Permission getRequiredPermission() { return Permission.BAN_MEMBERS; }
	@Override
	public final int getMaxArgs() { return 3; }
	@Override
	public final String getInvoke() { return "warn"; }
	@Override
	public final Category getCategory() { return Category.MODERATION; }
	@Override
	public final String getUsage() { return "s!warn <@someone> <reason>"; }
}