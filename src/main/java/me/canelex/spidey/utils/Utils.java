package me.canelex.spidey.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.classgraph.ClassGraph;
import me.canelex.jda.api.EmbedBuilder;
import me.canelex.jda.api.OnlineStatus;
import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.*;
import me.canelex.jda.api.utils.data.DataObject;
import me.canelex.spidey.Core;
import me.canelex.spidey.Events;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.objects.invites.WrappedInvite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class Utils extends Core
{
    private static final String INVITE_LINK = "https://discordapp.com/oauth2/authorize?client_id=468523263853592576&scope=bot&permissions=268446900";
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final ClassGraph graph = new ClassGraph().whitelistPackages("me.canelex.spidey.commands").enableAllInfo().ignoreClassVisibility();
    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Invites-Check").setUncaughtExceptionHandler((t, e) -> LOG.error("There was an exception in thread {}: {}", t.getName(), e.getMessage())).build();
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(threadFactory);
    private static final Map<Long, ScheduledFuture<?>> SCHEDULERS = new HashMap<>();

    private Utils()
    {
        super();
    }

    public static boolean hasPerm(final Member toCheck, final Permission perm)
    {
        return toCheck.hasPermission(perm);
    }

    public static void sendMessage(final TextChannel ch, final String toSend, final boolean isSpoiler)
    {
        if (isSpoiler)
            ch.sendMessage("||" + toSend + "||").queue();
        else
            ch.sendMessage(toSend).queue();
    }

    public static void sendMessage(final TextChannel ch, final MessageEmbed embed)
    {
        ch.sendMessage(embed).queue();
    }

    private static void sendPrivateMessage(final User user, final String toSend)
    {
        user.openPrivateChannel().queue(channel -> channel.sendMessage(toSend).queue());
    }

    public static void deleteMessage(final Message msg)
    {
        msg.delete().queue();
    }

    public static boolean canSetVanityUrl(final Guild g)
    {
        return g.getFeatures().contains("VANITY_URL");
    }

    public static String replaceLast(final String text, final String regex, final String replacement)
    {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    public static EmbedBuilder createEmbedBuilder(final User u)
    {
        return new EmbedBuilder().setFooter("Command executed by " + u.getAsTag(), u.getAvatarUrl());
    }

    public static String getInviteUrl(final long guildId)
    {
        return String.format("https://discordapp.com/oauth2/authorize?client_id=468523263853592576&guild_id=%s&scope=bot&permissions=268446900", guildId);
    }

    public static String getInviteUrl()
    {
        return INVITE_LINK;
    }

    public static void sendPrivateMessageFormat(final User u, final String message, final Object... args)
    {
        sendPrivateMessage(u, String.format(message, args));
    }

    public static boolean isWeb(final Member member)
    {
        return member.getOnlineStatus(ClientType.WEB) != OnlineStatus.OFFLINE;
    }

    public static boolean isDesktop(final Member member)
    {
        return member.getOnlineStatus(ClientType.DESKTOP) != OnlineStatus.OFFLINE;
    }

    public static boolean isMobile(final Member member)
    {
        return member.getOnlineStatus(ClientType.MOBILE) != OnlineStatus.OFFLINE;
    }

    public static void returnError(final String errMsg, final Message origin)
    {
        origin.addReaction(Emojis.CROSS).queue();
        origin.getTextChannel().sendMessage(String.format(":no_entry: %s.", errMsg)).queue(m ->
        {
            origin.delete().queueAfter(5, TimeUnit.SECONDS, null, userGone -> {});
            m.delete().queueAfter(5, TimeUnit.SECONDS, null, botGone -> {});
        });
    }

    public static String generateSuccess(final int count, final User u)
    {
        return ":white_check_mark: Successfully deleted **" + count + "** message" + (count > 1 ? "s" : "") + (u == null ? "." : String.format(" by user **%s**.", u.getAsTag()));
    }

    public static void registerCommands()
    {
        Core.commands.clear();
        try (final var result = graph.scan())
        {
            for (final var cls : result.getClassesImplementing("me.canelex.spidey.objects.command.ICommand"))
            {
                final var cmd = (ICommand) cls.loadClass().getDeclaredConstructor().newInstance();
                Core.commands.put(cmd.getInvoke(), cmd);
                cmd.getAliases().forEach(alias -> Core.commands.put(alias, cmd));
            }
        }
        catch (final Exception e)
        {
            LOG.error("There was an error while registering the commands!", e);
        }
    }

    public static String getSiteContent(final String url)
    {
        var content = "";
        try
        {
            final var con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "me.canelex.spidey");

            final var in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            var inputLine = "";
            final var response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            content = response.toString();
        }
        catch (final IOException e)
        {
            LOG.error("There was an error parsing the site content!", e);
        }
        return content;
    }

    public static DataObject getJson(final String url)
    {
        return DataObject.fromJson(getSiteContent(url));
    }

    public static void storeInvites(final Guild guild)
    {
        guild.retrieveInvites().queue(invites -> invites.forEach(invite -> Events.getInvites().put(invite.getCode(), new WrappedInvite(invite))));
    }

    public static void stopInvitesCheck(final long id)
    {
        SCHEDULERS.get(id).cancel(true);
    }

    public static void startInvitesCheck(final Guild guild)
    {
        SCHEDULERS.put(guild.getIdLong(), EXECUTOR.scheduleAtFixedRate(() ->
            guild.retrieveInvites().queue(invites ->
            {
                invites.forEach(invite ->
                {
                    final var code = invite.getCode();
                    if (!Events.getInvites().containsKey(code))
                        Events.getInvites().put(code, new WrappedInvite(invite)); // an invite was created and we did not store it yet so we store it now
                });
                Events.getInvites().forEach((key, value) -> Invite.resolve(guild.getJDA(), key).queue(success -> {}, failure -> Events.getInvites().remove(key))); // an invite was deleted so we remove it from the map
            }), 60L, 30L, TimeUnit.SECONDS));
    }
}