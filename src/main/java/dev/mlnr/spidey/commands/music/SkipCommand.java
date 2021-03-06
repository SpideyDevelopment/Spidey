package dev.mlnr.spidey.commands.music;

import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import dev.mlnr.spidey.objects.command.category.Category;
import dev.mlnr.spidey.objects.music.MusicPlayer;
import dev.mlnr.spidey.utils.MusicUtils;
import net.dv8tion.jda.api.Permission;

@SuppressWarnings("unused")
public class SkipCommand extends Command {
	public SkipCommand() {
		super("skip", "Skips the current song", Category.MUSIC, Permission.UNKNOWN, 0);
	}

	@Override
	public boolean execute(CommandContext ctx) {
		var musicPlayer = ctx.getCache().getMusicPlayerCache().getMusicPlayer(ctx.getGuild());
		var i18n = ctx.getI18n();
		if (musicPlayer == null) {
			ctx.replyErrorLocalized("music.messages.failure.no_music");
			return false;
		}
		var playingTrack = musicPlayer.getPlayingTrack();
		if (playingTrack == null) {
			ctx.replyErrorLocalized("music.messages.failure.no_song");
			return false;
		}
		if (MusicUtils.canInteract(ctx.getMember(), playingTrack)) {
			skip(musicPlayer, ctx);
			return true;
		}
		if (!MusicUtils.isMemberConnected(ctx)) {
			ctx.replyError(i18n.get("commands.skip.same_channel"));
			return false;
		}
		var trackScheduler = musicPlayer.getTrackScheduler();
		var author = ctx.getUser();
		var mention = author.getAsMention();
		if (trackScheduler.hasSkipVoted(author)) {
			trackScheduler.removeSkipVote(author);
			ctx.reply(i18n.get("commands.skip.removed") + " [" + mention + "]");
			return true;
		}
		trackScheduler.addSkipVote(author);
		var skipVotes = trackScheduler.getSkipVotes();
		var requiredSkipVotes = trackScheduler.getRequiredSkipVotes();
		if (skipVotes < requiredSkipVotes) {
			ctx.reply(i18n.get("commands.skip.added") + " **" + skipVotes + "**/**" + requiredSkipVotes + "** [" + mention + "]");
			return true;
		}
		skip(musicPlayer, ctx);
		return true;
	}

	private void skip(MusicPlayer musicPlayer, CommandContext ctx) {
		musicPlayer.skip();
		ctx.replyLocalized("commands.skip.skipped");
	}
}