package me.canelex.spidey.commands;

import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.PermissionError;
import me.canelex.spidey.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@SuppressWarnings("unused")
public class SlowmodeCommand implements ICommand {

	@Override
	public final void action(final GuildMessageReceivedEvent e) {

		final String neededPerm = "ADMINISTRATOR";

		if (e.getMember() != null && !Utils.hasPerm(e.getMember(), Permission.valueOf(neededPerm))) {

			Utils.sendMessage(e.getChannel(), PermissionError.getErrorMessage(neededPerm), false);

		}

		else {

			int seconds;

			final String par = e.getMessage().getContentRaw().substring(11);

			if (par.equals("off") || par.equals("false")) {

				seconds = 0;

			}

			else {

				try {

					seconds = Math.max(0, Math.min(Integer.parseInt(par), 21600));

				}

				catch (final NumberFormatException ignored) {

					Utils.sendMessage(e.getChannel(), ":no_entry: Couldn't parse argument.", false);
					return;

				}

			}

			e.getChannel().getManager().setSlowmode(seconds).queue();

		}

	}

	@Override
	public final String getDescription() { return "Sets a slowmode for channel. Limit: `21600s` - `6h`. Example - `s!slowmode <seconds | off>`"; }
	@Override
	public final boolean isAdmin() { return true; }
	@Override
	public final String getInvoke() { return "slowmode"; }
	@Override
	public final Category getCategory() { return Category.MODERATION; }
	@Override
	public final String getUsage() { return "s!slowmode <seconds/off>"; }

}