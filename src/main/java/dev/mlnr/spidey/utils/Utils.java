package dev.mlnr.spidey.utils;

import dev.mlnr.spidey.Core;
import dev.mlnr.spidey.objects.cache.Cache;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.invites.WrappedInvite;
import dev.mlnr.spidey.utils.requests.API;
import dev.mlnr.spidey.utils.requests.Requester;
import io.github.classgraph.ClassGraph;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static net.dv8tion.jda.api.entities.Activity.listening;
import static net.dv8tion.jda.api.entities.Activity.watching;

public class Utils
{
    private static final String INVITE_LINK = "https://discord.com/oauth2/authorize?client_id=468523263853592576&scope=bot&permissions=1342188724";
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final ClassGraph CLASS_GRAPH = new ClassGraph().whitelistPackages("dev.mlnr.spidey.commands");
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("EE, d.LLL y | HH:mm:ss");
    private static final Calendar CAL = Calendar.getInstance();
    public static final Pattern TEXT_PATTERN = Pattern.compile("[a-zA-Z0-9-_]+");

    private Utils()
    {
        super();
    }

    public static void sendMessage(final TextChannel ch, final String toSend)
    {
        if (ch.canTalk(ch.getGuild().getSelfMember()))
            ch.sendMessage(toSend).queue(null, failure -> {});
    }

    public static void sendMessage(final TextChannel ch, final MessageEmbed embed)
    {
        if (ch.canTalk(ch.getGuild().getSelfMember()))
            ch.sendMessage(embed).queue(null, failure -> {});
    }

    public static void sendPrivateMessage(final User user, final String toSend)
    {
        user.openPrivateChannel()
            .flatMap(channel -> channel.sendMessage(toSend))
            .queue(null, failure -> {});
    }

    public static void deleteMessage(final Message msg)
    {
        msg.delete().queue(null, failure -> {});
    }

    public static boolean canSetVanityUrl(final Guild g)
    {
        return g.getFeatures().contains("VANITY_URL");
    }

    public static EmbedBuilder createEmbedBuilder(final User u)
    {
        return new EmbedBuilder().setFooter("Command executed by " + u.getAsTag(), u.getEffectiveAvatarUrl());
    }

    public static String getInviteUrl()
    {
        return INVITE_LINK;
    }

    public static void sendPrivateMessageFormat(final User u, final String message, final Object... args)
    {
        sendPrivateMessage(u, String.format(message, args));
    }

    public static void returnError(final String errMsg, final Message origin)
    {
        origin.addReaction(Emojis.CROSS).queue(null, failure -> {});
        final var channel = origin.getTextChannel();
        if (channel.canTalk(channel.getGuild().getSelfMember()))
        {
            channel.sendMessage(String.format(":no_entry: %s.", errMsg))
                   .delay(Duration.ofSeconds(5))
                   .flatMap(Message::delete)
                   .flatMap(ignored -> origin.delete())
                   .queue(null, failure -> {});
        }
    }

    public static String generateSuccess(final int count, final User u)
    {
        return ":white_check_mark: Successfully deleted **" + count + "** message" + (count > 1 ? "s" : "") + (u == null ? "." : String.format(" by user **%s**.", u.getAsTag()));
    }

    public static void startup(final JDA jda)
    {
        final var commandsMap = Core.getCommands();
        final ArrayList<Supplier<Activity>> activities = new ArrayList<>(asList(
                () -> listening("your commands"),
                () -> watching("you"),
                () -> watching(jda.getGuildCache().size() + " guilds"),
                () -> watching(jda.getUserCache().size() + " users")
        ));
        try (final var result = CLASS_GRAPH.scan())
        {
            for (final var cls : result.getAllClasses())
            {
                final var cmd = (Command) cls.loadClass().getDeclaredConstructor().newInstance();
                commandsMap.put(cmd.getInvoke(), cmd);
                for (final var alias : cmd.getAliases())
                    commandsMap.put(alias, cmd);
            }
        }
        catch (final Exception e)
        {
            LOG.error("There was an error while registering the commands!", e);
        }
        Core.getExecutor().scheduleAtFixedRate(() -> jda.getPresence().setActivity(nextActivity(activities)), 0L, 30L, TimeUnit.SECONDS);
    }

    private static Activity nextActivity(final ArrayList<Supplier<Activity>> activities)
    {
        return activities.get(RANDOM.nextInt(activities.size())).get();
    }

    public static DataObject getJson(final String url, final API api)
    {
        return DataObject.fromJson(Requester.executeRequest(url, api));
    }

    public static void storeInvites(final Guild guild)
    {
        if (guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            guild.retrieveInvites().queue(invites -> invites.forEach(invite -> Cache.getInviteCache().put(invite.getCode(), new WrappedInvite(invite))));
    }

    public static String getTime(final long millis)
    {
        CAL.setTimeInMillis(millis);
        return SDF.format(CAL.getTime());
    }

    public static int getColorHex(final int value, final int max)
    {
        final var r = ((255 * value) / max);
        final var g = (255 * (max - value)) / max;
        return ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (0);
    }
}
