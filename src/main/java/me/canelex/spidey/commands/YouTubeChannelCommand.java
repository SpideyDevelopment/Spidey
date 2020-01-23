package me.canelex.spidey.commands;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import me.canelex.jda.api.entities.Message;
import me.canelex.spidey.Secrets;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.objects.json.SocialBlade;
import me.canelex.spidey.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Locale;

@SuppressWarnings("unused")
public class YouTubeChannelCommand implements ICommand
{
	private final Calendar cal = Calendar.getInstance();
	private final SimpleDateFormat date = new SimpleDateFormat("EE, d.LLL Y | HH:mm:ss", new Locale("en", "EN"));
	private static final Logger LOG = LoggerFactory.getLogger(YouTubeChannelCommand.class);
	private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

	@Override
	public final void action(final String[] args, final Message message)
	{
		final var channel = message.getChannel();

		try
		{
			final var youtube = new YouTube.Builder(
					new NetHttpTransport(),
					new JacksonFactory(),
					request -> {})
					.setApplicationName("youtube-4HEad")
					.setYouTubeRequestInitializer(new YouTubeRequestInitializer(Secrets.YOUTUBEAPIKEY))
					.build();

			final var search = youtube.search().list("snippet").setQ(message.getContentRaw().substring(12)).setType("channel");
			final var searchResponse = search.execute();

			if (!searchResponse.getItems().isEmpty())
			{
				final var channelId = searchResponse.getItems().get(0).getSnippet().getChannelId();
				final var channels = youtube.channels().list("snippet, statistics").setId(channelId);
				final var c = channels.execute().getItems().get(0);

				cal.setTimeInMillis(c.getSnippet().getPublishedAt().getValue());
				final var creation = date.format(cal.getTime());

				final var eb = Utils.createEmbedBuilder(message.getAuthor());
				final var sb = new SocialBlade().getYouTube(channelId);
				final var subs = sb.getSubs();
				final var views = sb.getViews();
				eb.setAuthor(c.getSnippet().getTitle(), "https://youtube.com/channel/" + channelId, "https://canelex.ymastersk.net/up/yt.png");
				eb.setColor(14765121);
				eb.setThumbnail(sb.getAvatar());
				eb.addField("Subscribers", (subs >= 1000 ? "**" + Utils.getCompactNumber(subs) + "** (**" + FORMATTER.format(subs) + "**)" : "**" + subs + "**"), false);
				eb.addField("Views", (views >= 1000 ? "**" + Utils.getCompactNumber(views) + "** (**" + FORMATTER.format(views) + "**)" : "**" + views + "**"), false);
				eb.addField("Videos", "**" + sb.getVideos() + "**", false);
				eb.addField("Created", "**" + creation + "**", false);
				eb.addField("Partner", (sb.isPartner() ? "**Yes**" : "**No**"), false);
				eb.addField("Verified", (sb.isVerified() ? "**Yes**" : "**No**"), false);
				eb.addField("Country", "**" + sb.getCountry() + "**", false);
				final var latestVideo = Utils.getSiteContent("https://beta.decapi.me/youtube/latest_video/?id=" + channelId);
				eb.addField("Latest video", (latestVideo.equals("An error occurred retrieving videos for channel: " + channelId) ? "**This channel has no videos**" : latestVideo), false);

				if (!sb.getBanner().equals("http://s.ytimg.com/yts/img/channels/c4/default_banner-vfl7DRgTn.png"))
				{
					eb.addBlankField(false);
					eb.setImage(sb.getBanner());
				}
				channel.sendMessage("Fetching data..").delay(Duration.ofSeconds(4)).flatMap(msg -> msg.editMessage(eb.build()).override(true)).queue();
			}
			else
				Utils.sendMessage(channel, ":no_entry: No results found.", false);
		}
		catch (final Exception ex)
		{
			LOG.error("There was an error requesting channel {}!", channel, ex);
		}
	}

	@Override
	public final String getDescription() { return "Shows info about entered YouTube channel"; }
	@Override
	public final String getInvoke() { return "ytchannel"; }
	@Override
	public final Category getCategory() { return Category.SOCIAL; }
	@Override
	public final String getUsage() { return "s!ytchannel <channel/id>"; }
}