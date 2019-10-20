package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Icon;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.PermissionError;
import me.canelex.spidey.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("unused")
public class UploadEmoteCommand implements ICommand
{
    private static final Logger LOG = LoggerFactory.getLogger(UploadEmoteCommand.class);

    @Override
    public final void action(final GuildMessageReceivedEvent e)
    {
        final var channel = e.getChannel();
        final var guild = e.getGuild();
        final var message = e.getMessage();
        final var args = message.getContentRaw().split("\\s+");

        if (args.length < 2)
            Utils.returnError("Please provide a URL to retrieve the emote from", message);

        final var image = new ByteArrayOutputStream();
        try
        {
            final var con = (HttpURLConnection) new URL(args[1]).openConnection();
            con.setRequestProperty("User-Agent", "me.canelex.spidey");

            try (final var stream = con.getInputStream())
            {
                final var chunk = new byte[4096];
                var bytesRead = 0;

                while ((bytesRead = stream.read(chunk)) > 0)
                {
                    image.write(chunk, 0, bytesRead);
                }
            }
        }
        catch (final MalformedURLException ex)
        {
            LOG.error("There was an error while parsing the URL. URL: {}", args[1], ex);
            Utils.returnError("Please provide a valid URL to retrieve the emote from", message);
        }
        catch (final IOException ex)
        {
            LOG.error("There was an error!", ex);
            Utils.returnError("Unfortunately, we could not create the emote due to an internal error", message);
        }

        if (!Utils.hasPerm(e.getMember(), Permission.MANAGE_EMOTES))
            Utils.sendMessage(channel, PermissionError.getErrorMessage("MANAGE_EMOTES"), false);
        else if (guild.getMaxEmotes() == guild.getEmoteCache().size())
            Utils.returnError("Guild has the maximum amount of emotes", message);
        else
        {
            var name = "";
            if (args.length == 3)
                name = args[2];
            else
                name = args[1].substring(args[1].lastIndexOf('/') + 1, args[1].lastIndexOf('.'));
            if (!Utils.hasPerm(guild.getSelfMember(), Permission.MANAGE_EMOTES))
                Utils.returnError("Spidey does not have the permission to upload emotes", message);
            else
                guild.createEmote(name, Icon.from(image.toByteArray())).queue(emote -> Utils.sendMessage(channel, "Emote " + emote.getAsMention() + " has been successfully uploaded!", false));
        }
    }

    @Override
    public final String getDescription() { return "Uploads the image from the provided url as an emote if possible"; }
    @Override
    public final boolean isAdmin() { return true; }
    @Override
    public final String getInvoke() { return "uploademote"; }
    @Override
    public final Category getCategory() { return Category.UTILITY; }
    @Override
    public final String getUsage() { return "s!uploademote <link> (name)"; }
}