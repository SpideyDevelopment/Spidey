package dev.mlnr.spidey.commands.nsfw;

import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;

@SuppressWarnings("unused")
public class AnalCommand extends Command {

	public AnalCommand() {
		super("anal", new String[]{}, Category.NSFW, Permission.UNKNOWN, 0, 4);
	}

	@Override
	public void execute(String[] args, CommandContext ctx) {}
}