package me.canelex.spidey.commands;

import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.Utils;

import java.awt.*;

@SuppressWarnings("unused")
public class AvatarCommand implements ICommand
{
	@Override
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var author = e.getAuthor();
		final var eb = Utils.createEmbedBuilder(author).setColor(Color.WHITE);
		final var users = e.getMessage().getMentionedUsers();
		final var u = users.isEmpty() ? author : users.get(0);
		final var avatarUrl = u.getAvatarUrl();

		eb.setAuthor("Avatar of user " + u.getAsTag());
		eb.setDescription(String.format("[Avatar link](%s)", avatarUrl));
		eb.setImage(avatarUrl);

		Utils.sendMessage(e.getChannel(), eb.build());
	}

	@Override
	public final String getDescription() { return "Shows avatar of you or of the mentioned user"; }
	@Override
	public final boolean isAdmin() { return false; }
	@Override
	public final String getInvoke() { return "avatar"; }
	@Override
	public final Category getCategory() { return Category.INFORMATIVE; }
	@Override
	public final String getUsage() { return "s!avatar (@someone)"; }
}