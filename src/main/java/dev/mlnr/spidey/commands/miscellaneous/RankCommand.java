package dev.mlnr.spidey.commands.miscellaneous;

import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import dev.mlnr.spidey.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class RankCommand extends Command
{
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new CountingThreadFactory(() -> "Spidey", "Rank Card Generator"));
    private static final Logger LOGGER = LoggerFactory.getLogger(RankCommand.class);
    private static Font spideyFont;

    public RankCommand()
    {
        super("rank", new String[]{}, "Shows your or mentioned user's rank in this server", "rank (User#Discriminator, @user, user id or username/nickname)", Category.MISC,
                Permission.UNKNOWN, 0, 0);
    }

    static
    {
        try
        {
            spideyFont = Font.createFont(Font.TRUETYPE_FONT, RankCommand.class.getResourceAsStream("/font.otf"));
            spideyFont = spideyFont.deriveFont(50.0f);
        }
        catch (final Exception ex)
        {
            LOGGER.error("The font couldn't be loaded!", ex);
        }
    }

    @Override
    public void execute(final String[] args, final CommandContext ctx)
    {
        THREAD_POOL.submit(() ->
        {
            final var user = args.length == 0 ? ctx.getAuthor() : Utils.getUserFromArgument(args[0], ctx.getTextChannel(), ctx.getMessage());
            if (user == null)
            {
                ctx.replyError("User not found");
                return;
            }
            try
            {
                final var avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl() + "?size=256"));

                // make the avatar round

                final var roundAvatar = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                final var roundAvatarGraphics = roundAvatar.createGraphics();
                roundAvatarGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                roundAvatarGraphics.setClip(new Ellipse2D.Float(0, 0, 256, 256));
                roundAvatarGraphics.drawImage(avatar, 0, 0, 256, 256, null);
                roundAvatarGraphics.dispose();

                // downscale the avatar

                final var downscaledAvatar = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                final var downscaledAvatarGraphics = downscaledAvatar.createGraphics();
                downscaledAvatarGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                downscaledAvatarGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                downscaledAvatarGraphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                downscaledAvatarGraphics.drawImage(roundAvatar, 0, 0, 128, 128, null);
                downscaledAvatarGraphics.dispose();

                // compute the rank card

                final var rankCard = ImageIO.read(this.getClass().getResource("/ranktemplate.png")); // load the template
                final var rankCardGraphics = rankCard.createGraphics();
                rankCardGraphics.drawImage(downscaledAvatar, 85, 61, 128, 128, null);

                // text

                rankCardGraphics.setFont(spideyFont);
                rankCardGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                rankCardGraphics.drawString(user.getAsTag(), 445, 77);

                //

                rankCardGraphics.dispose();

                // sending

                final var baos = new ByteArrayOutputStream();
                ImageIO.write(rankCard, "png", baos);
                ctx.getTextChannel().sendFile(baos.toByteArray(), "rankcard.png").queue();
            }
            catch (final Exception ex)
            {
                ctx.replyError("Unfortunately, i couldn't generate a rank card due to an internal error: **" + ex.getMessage() + "**. Please report this message to the Developer");
            }
        });
    }
}