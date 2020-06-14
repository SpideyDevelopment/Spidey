package me.canelex.spidey.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.classgraph.ClassGraph;
import me.canelex.spidey.Core;
import me.canelex.spidey.objects.cache.Cache;
import me.canelex.spidey.objects.command.Command;
import me.canelex.spidey.objects.invites.WrappedInvite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static net.dv8tion.jda.api.entities.Activity.listening;
import static net.dv8tion.jda.api.entities.Activity.watching;

public class Utils
{
    private static final String INVITE_LINK = "https://discord.com/oauth2/authorize?client_id=545938274368356352&scope=bot&permissions=1342188724";
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final ClassGraph graph = new ClassGraph().whitelistPackages("me.canelex.spidey.commands");
    private static final char[] SUFFIXES = {'k', 'M', 'B'};
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("EE, d.LLL y | HH:mm:ss");
    private static final Calendar CAL = Calendar.getInstance();
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final ThreadFactoryBuilder THREAD_FACTORY = new ThreadFactoryBuilder().setUncaughtExceptionHandler((t, e) -> LOG.error("There was an exception in thread {}: {}", t.getName(), e.getMessage()));
    public static final Pattern TEXT_PATTERN = Pattern.compile("[a-zA-Z0-9-_]+");

    private Utils()
    {
        super();
    }

    public static boolean hasPerm(final Member toCheck, final Permission perm)
    {
        return toCheck.hasPermission(perm);
    }

    public static void sendMessage(final MessageChannel ch, final String toSend)
    {
        ch.sendMessage(toSend).queue();
    }

    public static void sendMessage(final MessageChannel ch, final MessageEmbed embed)
    {
        ch.sendMessage(embed).queue();
    }

    public static void sendPrivateMessage(final User user, final String toSend)
    {
        user.openPrivateChannel()
            .flatMap(channel -> channel.sendMessage(toSend))
            .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
    }

    public static void deleteMessage(final Message msg)
    {
        msg.delete().queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
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

        commandsMap.clear(); //just to make sure that the commands map is empty
        try (final var result = graph.scan())
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
        return activities.get(random.nextInt(activities.size())).get();
    }

    public static String getSiteContent(final String url)
    {
        final var request = new Request.Builder().url(url).header("user-agent", "me.canelex.spidey").build();
        try (final var body = HTTP_CLIENT.newCall(request).execute().body())
        {
            return body != null ? body.string() : "";
        }
        catch (final IOException ex)
        {
            LOG.error("There was an error while executing a request for url {}", url);
        }
        return "";
    }

    public static DataObject getJson(final String url)
    {
        return DataObject.fromJson(getSiteContent(url));
    }

    public static void storeInvites(final Guild guild)
    {
        if (hasPerm(guild.getSelfMember(), Permission.MANAGE_SERVER))
            guild.retrieveInvites().queue(invites -> invites.forEach(invite -> Cache.getInviteCache().put(invite.getCode(), new WrappedInvite(invite))));
    }

    public static String cleanString(final String original)
    {
        return StringEscapeUtils.unescapeJava(
            StringEscapeUtils.unescapeHtml4(
                original
                    .replaceAll("<.*?>", "")
                    .replaceAll("\"", "")));
    }

    public static String getCompactNumber(final long number)
    {
        final var sn = String.valueOf(number);
        final var length = sn.length();
        if (number < 1000)
            return sn;
        final var magnitude = (length - 1) / 3;
        var digits = (length - 1) % 3 + 1;

        var value = new char[4];
        for (var i = 0; i < digits; i++)
        {
            value[i] = sn.charAt(i);
        }
        if (digits == 1 && sn.charAt(1) != '0')
        {
            value[digits++] = '.';
            value[digits++] = sn.charAt(1);
        }
        value[digits++] = SUFFIXES[magnitude - 1];
        return new String(value, 0, digits);
    }

    public static String getTime(final long millis)
    {
        CAL.setTimeInMillis(millis);
        return SDF.format(CAL.getTime());
    }

    public static String format(final long input)
    {
        return FORMATTER.format(input);
    }

    public static ScheduledExecutorService createScheduledThread(final String name)
    {
        return Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY.setNameFormat(name).build());
    }

    public static ExecutorService createThread(final String name)
    {
        return Executors.newSingleThreadExecutor(THREAD_FACTORY.setNameFormat(name).build());
    }
}