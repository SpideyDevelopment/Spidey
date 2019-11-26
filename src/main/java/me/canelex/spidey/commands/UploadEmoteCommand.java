package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Icon;
import me.canelex.jda.api.entities.Message;
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
    public final void action(final String[] args, final Message message)
    {
        final var channel = message.getChannel();
        final var guild = message.getGuild();

        if (args.length < 2)
            Utils.returnError("Please provide a URL to retrieve the emote from", message);

        final var index = args[1].lastIndexOf('.');
        final var extension = args[1].substring(index + 1);
        if (Icon.IconType.fromExtension(extension) == Icon.IconType.UNKNOWN)
        {
            Utils.returnError("Please provide a URL containing an image", message);
            return;
        }

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
            con.disconnect();
        }
        catch (final MalformedURLException ex)
        {
            LOG.error("There was an error while parsing the URL. URL: {}", args[1], ex);
            Utils.returnError("Please provide a valid URL to retrieve the emote from", message);
            return;
        }
        catch (final IOException ex)
        {
            LOG.error("There was an error!", ex);
            Utils.returnError("Unfortunately, we could not create the emote due to an internal error", message);
            return;
        }

        final var requiredPermission = getRequiredPermission();
        if (!Utils.hasPerm(message.getMember(), requiredPermission))
            Utils.sendMessage(channel, PermissionError.getErrorMessage(requiredPermission), false);
        else if (guild.getMaxEmotes() == guild.getEmoteCache().size())
            Utils.returnError("Guild has the maximum amount of emotes", message);
        else
        {
            var name = "";
            if (args.length == 3)
                name = args[2];
            else
                name = args[1].substring(args[1].lastIndexOf('/') + 1, index);
            if (!(name.length() > 1 && name.length() < 33))
            {
                Utils.returnError("The name of the emote has to be between 2 and 32 in length", message);
                return;
            }
            else if (!name.matches("[a-zA-Z0-9-_]+"))
            {
                Utils.returnError("The name of the emote has to be in a valid format", message);
                return;
            }
            if (!Utils.hasPerm(guild.getSelfMember(), requiredPermission))
                Utils.returnError("Spidey does not have the permission to upload emotes", message);
            else
                guild.createEmote(name, Icon.from(image.toByteArray())).queue(emote ->
                    guild.retrieveEmotes().queue(emotes ->
                    {
                        final var amount = guild.getMaxEmotes() - emotes.size();
                        Utils.sendMessage(channel, "Emote " + emote.getAsMention() + " has been successfully uploaded! Emote slots left: **" + (amount == 0 ? "None" : amount) + "**", false);
                    })
                , failure ->
                    Utils.returnError("Unfortunately, we could not create the emote due to an internal error: **" + failure.getMessage() + "**. Please report this message to the Developer.", message));
        }
    }

    @Override
    public final String getDescription() { return "Uploads the image from the provided url as an emote if possible"; }
    @Override
    public final Permission getRequiredPermission() { return Permission.MANAGE_EMOTES; }
    @Override
    public final String getInvoke() { return "uploademote"; }
    @Override
    public final Category getCategory() { return Category.UTILITY; }
    @Override
    public final String getUsage() { return "s!uploademote <link> (name)"; }
}