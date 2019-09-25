package me.canelex.spidey.commands;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.Secrets;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.objects.json.SocialBlade;
import me.canelex.spidey.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

@SuppressWarnings("unused")
public class YouTubeChannelCommand implements ICommand
{

	private final Locale locale = new Locale("en", "EN");
	private final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
	private final SimpleDateFormat date = new SimpleDateFormat("EEEE, d.LLLL Y", locale);
	private final SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss", locale);
	private static final Logger LOG = LoggerFactory.getLogger(YouTubeChannelCommand.class);

	@Override
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var channel = e.getMessage().getContentRaw().substring(12);
		final var msg = e.getChannel().sendMessage("Fetching data..").complete(); //TODO temporary solution

		try
		{
			final var youtube = new YouTube.Builder(
					new NetHttpTransport(),
					new JacksonFactory(),
					request -> {})
					.setApplicationName("youtube-cmdline-search-sample")
					.setYouTubeRequestInitializer(new YouTubeRequestInitializer(Secrets.YOUTUBEAPIKEY))
					.build();

			final var search = youtube.search().list("snippet").setQ(channel).setType("channel");
			final var searchResponse = search.execute();

			if (!searchResponse.getItems().isEmpty()) {
				final var channelId = searchResponse.getItems().get(0).getSnippet().getChannelId();
				final var channels = youtube.channels().list("snippet, statistics");
				channels.setId(channelId);
				final var c = channels.execute().getItems().get(0);

				cal.setTimeInMillis(c.getSnippet().getPublishedAt().getValue());

				final var caltime = cal.getTime();
				final var creatdate = date.format(caltime);
				final var creattime = time.format(caltime);

				final var eb = Utils.createEmbedBuilder(e.getAuthor());
				final var sb = new SocialBlade().getYouTube(channelId);
				eb.setAuthor(c.getSnippet().getTitle(), "https://youtube.com/channel/" + channelId, "https://canelex.ymastersk.net/up/yt.png");
				eb.setColor(14765121);
				eb.setThumbnail(sb.getAvatar());
				eb.addField("Subscribers", "**" + sb.getSubs() + "**", false);
				eb.addField("Views", "**" + sb.getViews() + "**", false);
				eb.addField("Videos", "**" + sb.getVideos() + "**", false);
				eb.addField("Created", String.format("**%s** | **%s** UTC", creatdate, creattime), false);
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
				msg.editMessage(eb.build()).override(true).queue();
			}
			else
				Utils.sendMessage(e.getChannel(), ":no_entry: No results found.", false);
		}
		catch (final Exception ex)
		{
			LOG.error("There was an error requesting channel {}!", channel, ex);
		}
	}

	@Override
	public final String getDescription() { return "Shows info about entered YouTube channel"; }
	@Override
	public final boolean isAdmin() { return false; }
	@Override
	public final String getInvoke() { return "ytchannel"; }
	@Override
	public final Category getCategory() { return Category.SOCIAL; }
	@Override
	public final String getUsage() { return "s!ytchannel <channel/id>"; }
}