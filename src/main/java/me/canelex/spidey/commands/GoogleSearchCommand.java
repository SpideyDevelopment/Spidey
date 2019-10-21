package me.canelex.spidey.commands;

import me.canelex.jda.api.entities.Message;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.objects.search.GoogleSearch;
import me.canelex.spidey.utils.Utils;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unused")
public class GoogleSearchCommand implements ICommand
{
	@Override
	public final void action(final String[] args, final Message message)
	{
		final var result = new GoogleSearch().getResult(
				StringUtils.join(args, "+", 1, args.length));
		Utils.sendMessage(message.getChannel(), result.getContent(), false);
	}

	@Override
	public final String getDescription() { return "Allows you to search for results on Google"; }
	@Override
	public final String getInvoke() { return "g"; }
	@Override
	public final Category getCategory() { return Category.MISC; }
	@Override
	public final String getUsage() { return "s!g <query>"; }
}