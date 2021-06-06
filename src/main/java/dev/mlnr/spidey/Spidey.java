package dev.mlnr.spidey;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import dev.mlnr.blh.core.api.BLHBuilder;
import dev.mlnr.blh.core.api.BotList;
import dev.mlnr.blh.jda.BLHJDAListener;
import dev.mlnr.spidey.cache.Cache;
import dev.mlnr.spidey.events.*;
import dev.mlnr.spidey.objects.I18n;
import dev.mlnr.spidey.utils.ConcurrentUtils;
import dev.mlnr.spidey.utils.MusicUtils;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;

public class Spidey {
	private static final Logger logger = LoggerFactory.getLogger(Spidey.class);
	private final JDA jda;

	private final DatabaseManager databaseManager = new DatabaseManager();

	public Spidey() throws LoginException, InterruptedException {
		I18n.loadLanguages();
		MusicUtils.registerSources();

		RestAction.setDefaultFailure(null);
		MessageAction.setDefaultMentions(EnumSet.noneOf(Message.MentionType.class));

		var blh = new BLHBuilder().setDevModePredicate(botId -> botId != 772446532560486410L)
				.setSuccessLoggingEnabled(false)
				.setUnavailableEventsEnabled(false)
				.addBotList(BotList.TOP_GG, System.getenv("topgg"))
				.addBotList(BotList.DISCORDLIST_SPACE, System.getenv("botlistspace"))
				.addBotList(BotList.DBOATS, System.getenv("dboats"))
				.addBotList(BotList.DSERVICES, System.getenv("dservices"))
				.addBotList(BotList.DBOTS_GG, System.getenv("dbotsgg"))
				.addBotList(BotList.DBL, System.getenv("dbl"))
				.build();

		var cache = new Cache(this);

		jda = JDABuilder.create(System.getenv("Spidey"),
				GUILD_BANS,
				GUILD_INVITES,
				GUILD_MEMBERS,
				GUILD_MESSAGES,
				GUILD_MESSAGE_REACTIONS,
				GUILD_EMOJIS,
				GUILD_VOICE_STATES)
			.disableCache(
				MEMBER_OVERRIDES,
				ROLE_TAGS,
				ACTIVITY,
				CLIENT_STATUS,
				ONLINE_STATUS
			)
			.setMemberCachePolicy(MemberCachePolicy.VOICE)
			.setChunkingFilter(ChunkingFilter.NONE)
			.addEventListeners(new ReadyEvents(databaseManager, cache), new BanEvents(cache), new DeleteEvents(cache), new GuildEvents(databaseManager, cache),
					new InviteEvents(cache), new MemberEvents(cache), new MessageEvents(cache), new PaginatorEvent(cache), new VoiceEvent(cache),
					new InteractionEvents(cache), ConcurrentUtils.getEventWaiter(), new BLHJDAListener(blh))
			.setActivity(Activity.watching("myself load"))
			.setStatus(OnlineStatus.DO_NOT_DISTURB)
			.setGatewayEncoding(GatewayEncoding.ETF)
			.setAudioSendFactory(new NativeAudioSendFactory())
			.build()
			.awaitReady();
	}

	public static void main(String[] args) {
		try {
			new Spidey();
		}
		catch (Exception e) {
			logger.error("There was an error while building JDA", e);
		}
	}

	public JDA getJDA() {
		return jda;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}
}