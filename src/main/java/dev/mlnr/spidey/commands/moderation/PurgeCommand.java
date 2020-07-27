package dev.mlnr.spidey.commands.moderation;

import dev.mlnr.spidey.Core;
import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.utils.Emojis;
import dev.mlnr.spidey.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.mlnr.spidey.utils.Utils.addReaction;

@SuppressWarnings({"unused", "StringBufferReplaceableByString"})
public class PurgeCommand extends Command
{
    public PurgeCommand()
    {
        super("purge", new String[]{"d", "delete"}, "Purges messages (by mentioned user)", "purge <count> (user)", Category.MODERATION,
                Permission.MESSAGE_MANAGE, 2, 6);
    }

    @Override
    public final void execute(final String[] args, final Message message)
    {
        final var channel = message.getTextChannel();

        if (!channel.getGuild().getSelfMember().hasPermission(channel, getRequiredPermission(), Permission.MESSAGE_HISTORY))
        {
            Utils.returnError("I don't have permission to purge messages or see the message history in this channel", message);
            return;
        }

        if (args.length == 0)
        {
            Utils.returnError("Wrong syntax", message);
            return;
        }

        var amount = 0;
        try
        {
            amount = Integer.parseInt(args[0]);
        }
        catch (final NumberFormatException ignored)
        {
            Utils.returnError("Entered value is either negative or not a number", message);
            return;
        }
        if (amount > 100 || amount < 1)
        {
            Utils.returnError("Please enter a number from 1-100", message);
            return;
        }

        final var mentioned = message.getMentionedUsers();
        final var user = mentioned.isEmpty() ? null : mentioned.get(0);
        final var limit = amount;

        message.delete().queue(ignored -> channel.getIterableHistory().cache(false).takeAsync(100).thenAcceptAsync(messages ->
        {
            final var msgs = user == null ? messages.subList(0, Math.min(messages.size(), limit)) : messages.stream().filter(msg -> msg.getAuthor().equals(user)).limit(limit).collect(Collectors.toList());
            if (msgs.isEmpty())
            {
                Utils.returnError("There are no messages to be deleted", message);
                return;
            }
            final var pinned = msgs.stream().filter(Message::isPinned).collect(Collectors.toList());
            if (pinned.isEmpty())
            {
                proceed(msgs, user, channel);
                return;
            }
            final var size = pinned.size();
            final var equalsOne = size == 1;
            final var builder = new StringBuilder("There ");
            builder.append(equalsOne ? "is" : "are").append(" **").append(size)
                    .append("** pinned message").append(equalsOne ? "" : "s").append(" selected for deletion. ")
                    .append("Are you sure you want to delete ").append(equalsOne ? "it" : "them").append("? ")
                    .append("Deleting a message will also unpin it.")
                    .append("\n\nReacting with :white_check_mark: will delete ").append(equalsOne ? "this message" : "these messages").append(".")
                    .append("\nReacting with :wastebasket: will delete each unpinned message.")
                    .append("\nReacting with :x: will cancel the deletion.")
                    .append("\n\nThe deletion will be cancelled automatically in **1 minute** if a decision isn't made.");
            channel.sendMessage(builder.toString()).queue(msg ->
            {
                final var wastebasket = "\uD83D\uDDD1";
                addReaction(msg, Emojis.CHECK);
                addReaction(msg, wastebasket);
                addReaction(msg, Emojis.CROSS);

                Core.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class,
                    ev -> ev.getUser() == message.getAuthor() && ev.getMessageIdLong() == msg.getIdLong(),
                    ev ->
                    {
                        switch (ev.getReactionEmote().getName())
                        {
                            case Emojis.CHECK:
                                Utils.deleteMessage(msg);
                                break;
                            case Emojis.CROSS:
                                Utils.deleteMessage(msg);
                                return;
                            case wastebasket:
                                msgs.removeAll(pinned);
                                if (msgs.isEmpty())
                                {
                                    Utils.returnError("There are no messages to be deleted", msg);
                                    return;
                                }
                                Utils.deleteMessage(msg);
                                break;
                            default:
                        }
                        proceed(msgs, user, channel);
                    }, 1, TimeUnit.MINUTES, () -> Utils.returnError("Sorry, you took too long", msg));
                });
        }).exceptionally(throwable ->
        {
            Utils.returnError("Unfortunately, i couldn't purge messages due to an internal error: **" + throwable.getMessage() + "**. Please report this message to the Developer", message);
            return null;
        }));
    }

    private void proceed(final List<Message> toDelete, final User user, final TextChannel channel)
    {
        final var future = CompletableFuture.allOf(channel.purgeMessages(toDelete).toArray(new CompletableFuture[0]));
        future.thenRunAsync(() -> channel.sendMessage(Utils.generateSuccess(toDelete.size(), user))
                                         .delay(Duration.ofSeconds(5))
                                         .flatMap(Message::delete)
                                         .queue());
    }
}