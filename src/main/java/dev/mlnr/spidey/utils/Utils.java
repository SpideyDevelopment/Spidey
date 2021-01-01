package dev.mlnr.spidey.utils;

import dev.mlnr.spidey.Spidey;
import dev.mlnr.spidey.cache.GeneralCache;
import dev.mlnr.spidey.cache.ResponseCache;
import dev.mlnr.spidey.objects.guild.InviteData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static net.dv8tion.jda.api.entities.Activity.listening;
import static net.dv8tion.jda.api.entities.Activity.watching;

public class Utils
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EE, d.LLL y | HH:mm:ss");
    public static final Pattern TEXT_PATTERN = Pattern.compile("[a-zA-Z0-9-_]+");
    public static final int SPIDEY_COLOR = 3288807;

    private Utils() {}

    public static void sendMessage(final TextChannel channel, final MessageEmbed embed)
    {
        sendMessage(channel, embed, null);
    }

    public static void sendMessage(final TextChannel channel, final MessageEmbed embed, final Message invokeMessage)
    {
        if (channel.canTalk() && channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS))
            channel.sendMessage(embed).queue(response -> setResponse(invokeMessage, response));
    }

    public static void sendMessage(final TextChannel channel, final String toSend)
    {
        sendMessage(channel, toSend, MessageAction.getDefaultMentions(), null);
    }

    public static void sendMessage(final TextChannel channel, final String toSend, final Set<Message.MentionType> allowedMentions, final Message invokeMessage)
    {
        if (channel.canTalk())
        {
            channel.sendMessage(toSend).allowedMentions(allowedMentions == null ? EnumSet.noneOf(Message.MentionType.class) : allowedMentions) // passing null to allowedMentions allows all mentions, nice logic JDA
                    .queue(response -> setResponse(invokeMessage, response));
        }
    }

    private static void setResponse(final Message invokeMessage, final Message responseMessage)
    {
        if (invokeMessage != null)
            ResponseCache.setResponseMessageId(invokeMessage.getIdLong(), responseMessage.getIdLong());
    }

    public static void deleteMessage(final Message msg)
    {
        final var channel = msg.getTextChannel();
        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            msg.delete().queue();
    }

    public static CompletableFuture<Void> purgeMessages(final Message... messages)
    {
        final var channel = messages[0].getTextChannel();
        return channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE)
                ? CompletableFuture.allOf(channel.purgeMessages(messages).toArray(new CompletableFuture[0]))
                : new CompletableFuture<>();
    }

    public static EmbedBuilder createEmbedBuilder(final User user)
    {
        return new EmbedBuilder().setFooter("Command executed by " + user.getAsTag(), user.getEffectiveAvatarUrl());
    }

    public static void addReaction(final Message message, final String reaction)
    {
        if (message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION))
            message.addReaction(reaction).queue();
    }

    public static void returnError(final String errMsg, final Message origin)
    {
        returnError(errMsg, origin, true);
    }

    public static void returnError(final String errMsg, final Message origin, final boolean includeDot)
    {
        returnError(errMsg, origin, Emojis.CROSS, includeDot);
    }

    public static void returnError(final String errMsg, final Message origin, final String failureEmoji, final boolean includeDot)
    {
        addReaction(origin, failureEmoji);
        final var channel = origin.getTextChannel();
        if (!channel.canTalk())
            return;
        channel.sendMessage(String.format(":no_entry: %s%s", errMsg, includeDot ? "." : ""))
                .delay(Duration.ofSeconds(7))
                .flatMap(Message::delete)
                .queue(success -> deleteMessage(origin));
    }

    public static String generateSuccess(final int count, final User u)
    {
        return ":white_check_mark: Successfully deleted **" + count + "** message" + (count > 1 ? "s" : "") + (u == null ? "." : String.format(" by user **%s**.", u.getAsTag()));
    }

    public static void startActivityScheduler(final JDA jda)
    {
        final var activities = new ArrayList<Supplier<Activity>>(asList( // we use supplier here so the values of getUserCache and getGuildCache are updated each time
                () -> listening("your commands"),
                () -> watching("you"),
                () -> watching(jda.getGuildCache().size() + " guilds"),
                () -> watching(getUserCount(jda) + " users")
        ));
        Spidey.getScheduler().scheduleAtFixedRate(() -> jda.getPresence().setActivity(nextActivity(activities)), 0, 30, TimeUnit.SECONDS);
    }

    private static int getUserCount(final JDA jda)
    {
        return jda.getGuildCache().applyStream(stream -> stream.mapToInt(Guild::getMemberCount).sum());
    }

    private static Activity nextActivity(final List<Supplier<Activity>> activities)
    {
        return activities.get(ThreadLocalRandom.current().nextInt(activities.size())).get();
    }

    public static void storeInvites(final Guild guild)
    {
        if (guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            guild.retrieveInvites().queue(invites -> invites.forEach(invite -> GeneralCache.getInviteCache().put(invite.getCode(), new InviteData(invite))));
    }

    public static String formatDate(final OffsetDateTime date)
    {
        return DATE_FORMATTER.format(date);
    }

    public static int getColorHex(final int value, final int max)
    {
        final var r = ((255 * value) / max);
        final var g = (255 * (max - value)) / max;
        return ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8);
    }

    public static <K, V> ExpiringMap<K, V> createDefaultExpiringMap()
    {
        return ExpiringMap.builder()
                .expirationPolicy(ExpirationPolicy.ACCESSED)
                .expiration(2, TimeUnit.MINUTES)
                .build();
    }
}