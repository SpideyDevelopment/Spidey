package dev.mlnr.spidey.commands.music;

import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import dev.mlnr.spidey.objects.command.category.Category;
import dev.mlnr.spidey.utils.MusicUtils;
import net.dv8tion.jda.api.Permission;

@SuppressWarnings("unused")
public class RemoveCommand extends Command {
	public RemoveCommand() {
		super("remove", new String[]{"rem"}, Category.MUSIC, Permission.UNKNOWN, 0, 0);
	}

	@Override
	public boolean execute(String[] args, CommandContext ctx) {
		var guild = ctx.getGuild();
		var musicPlayer = ctx.getCache().getMusicPlayerCache().getMusicPlayer(guild);
		if (musicPlayer == null) {
			ctx.replyErrorLocalized("music.messages.failure.no_music");
			return false;
		}
		var trackScheduler = musicPlayer.getTrackScheduler();
		var queue = trackScheduler.getQueueAsList();
		if (queue.isEmpty()) {
			ctx.replyErrorLocalized("music.messages.failure.queue_empty");
			return false;
		}
		if (args.length == 0) {
			ctx.replyErrorLocalized("commands.remove.other.provide");
			return false;
		}
		ctx.getArgumentAsUnsignedInt(0, trackPosition -> {
			var size = queue.size();
			if (trackPosition == 0 || trackPosition > size) {
				ctx.replyErrorLocalized("number.range", size);
				return;
			}
			var actualPosition = trackPosition - 1;
			var selectedTrack = queue.get(actualPosition);
			if (!MusicUtils.canInteract(ctx.getMember(), selectedTrack)) {
				ctx.replyErrorLocalized("music.messages.failure.cant_interact_requester", "remove someone else's song from the queue");
				return;
			}
			queue.remove(actualPosition);
			ctx.reactLike();
			ctx.replyLocalized("commands.remove.other.removed", selectedTrack.getInfo().title);
		});
		return true;
	}
}