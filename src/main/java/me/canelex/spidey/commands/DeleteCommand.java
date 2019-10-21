package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Message;
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
	public final void action(final String[] args, final Message message)
	{
		final var channel = message.getChannel();
		final var mentionedUsers = message.getMentionedUsers();

		message.delete().complete();

		final var requiredPermission = getRequiredPermission();
		if (!Utils.hasPerm(message.getMember(), requiredPermission))
		{
			Utils.sendMessage(channel, PermissionError.getErrorMessage(requiredPermission), false);
			return;
		}

		if (args.length < 2)
		{
			Utils.returnError("Wrong syntax", message);
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
				Utils.returnError("Entered value is either negative or not a number", message);
				return;
			}
			if (amount == 0)
			{
				Utils.returnError("Please enter a number from 1-100", message);
				return;
			}
			if (amount == 100)
				amount = 99;

			channel.getIterableHistory().cache(false).takeAsync(amount).thenAccept(messages ->
			{
				count = messages.size();
				CompletableFuture<Void> future;
				if (count == 1)
					future = messages.get(0).delete().submit();
				else
					future = CompletableFuture.allOf(channel.purgeMessages(messages).toArray(new CompletableFuture[0]));
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
				Utils.returnError("Entered value is either negative or not a number", message);
				return;
			}
			if (amount == 0)
			{
				Utils.returnError("Please enter a number from 1-100", message);
				return;
			}
			if (amount == 100)
				amount = 99;

			final var a = amount;
			channel.getIterableHistory().cache(false).takeAsync(100).thenAccept(messages ->
			{
				final var newList = messages.stream().filter(m -> m.getAuthor().equals(user)).limit(a).collect(Collectors.toList());
				CompletableFuture<Void> future;
				if (newList.size() == 1)
					future = newList.get(0).delete().submit();
				else
					future = CompletableFuture.allOf(channel.purgeMessages(newList).toArray(new CompletableFuture[0]));
				future.thenRunAsync(() -> channel.sendMessage(Utils.generateSuccess(newList.size(), user)).queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS)));
			});
		}
	}

	@Override
	public final String getDescription() { return "Deletes messages (by mentioned user)"; }
	@Override
	public final Permission getRequiredPermission() { return Permission.MESSAGE_MANAGE; }
	@Override
    public final int getMaxArgs() { return 3; }
	@Override
	public final String getInvoke() { return "d"; }
	@Override
	public final Category getCategory() { return Category.MODERATION; }
	@Override
	public final String getUsage() { return "s!d <count/@someone> <count>"; }
}