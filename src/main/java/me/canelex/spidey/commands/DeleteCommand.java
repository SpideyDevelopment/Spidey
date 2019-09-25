package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.PermissionError;
import me.canelex.spidey.utils.Utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class DeleteCommand implements ICommand
{
	private int count;

	@Override
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var maxArgs = 3;
		final var msg = e.getMessage();
		final var channel = e.getChannel();
		final var member = e.getMember();
		final var mentionedUsers = msg.getMentionedUsers();

		msg.delete().complete();
		if (member != null && !Utils.hasPerm(member, Permission.BAN_MEMBERS))
		{
			Utils.sendMessage(channel, PermissionError.getErrorMessage("BAN_MEMBERS"), false);
			return;
		}

		final var args = msg.getContentRaw().trim().split("\\s+", maxArgs);
		if (args.length < 2)
		{
			Utils.returnError("Wrong syntax", msg);
			return;
		}
		if (mentionedUsers.isEmpty())
		{
			var amount = 0;
			try
			{
				amount = Integer.parseUnsignedInt(args[1]);
			}
			catch (final NumberFormatException ignored)
			{
				Utils.returnError("Entered value is either negative or not a number", msg);
				return;
			}
			if (amount == 0)
			{
				Utils.returnError("Please enter a number from 1-100", msg);
				return;
			}
			if (amount == 100)
				amount = 99;

			channel.getIterableHistory().cache(false).takeAsync(amount).thenAccept(msgs ->
			{
				count = msgs.size();
				CompletableFuture<Void> future;
				if (count == 1)
					future = msgs.get(0).delete().submit();
				else
				{
					final var list = channel.purgeMessages(msgs);
					future = CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
				}
				future.thenRunAsync(() -> channel.sendMessage(Utils.generateSuccess(count, null)).queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS)));
			});
		}
		else
		{
			var amount = 0;
			final var user = mentionedUsers.get(0);
			try
			{
				amount = Integer.parseUnsignedInt(args[2]);
			}
			catch (final NumberFormatException ignored)
			{
				Utils.returnError("Entered value is either negative or not a number", msg);
				return;
			}
			if (amount == 0)
			{
				Utils.returnError("Please enter a number from 1-100", msg);
				return;
			}
			if (amount == 100)
				amount = 99;

			final var a = amount;
			channel.getIterableHistory().cache(false).takeAsync(100).thenAccept(msgs ->
			{
				final var newList = msgs.stream().filter(m -> m.getAuthor().equals(user)).limit(a).collect(Collectors.toList());
				CompletableFuture<Void> future;
				if (newList.size() == 1)
					future = newList.get(0).delete().submit();
				else
				{
					final var requests = channel.purgeMessages(newList);
					future = CompletableFuture.allOf(requests.toArray(new CompletableFuture[0]));
				}
				future.thenRunAsync(() -> channel.sendMessage(Utils.generateSuccess(newList.size(), user)).queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS)));
			});
		}
	}

	@Override
	public final String getDescription() { return "Deletes messages (by mentioned user)"; }
	@Override
	public final boolean isAdmin() { return true; }
	@Override
	public final String getInvoke() { return "d"; }
	@Override
	public final Category getCategory() { return Category.MODERATION; }
	@Override
	public final String getUsage() { return "s!d <count/@someone> <count>"; }
}