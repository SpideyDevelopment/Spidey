package me.canelex.spidey.commands;

import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.objects.json.Reddit;
import me.canelex.spidey.utils.Utils;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class RedditCommand implements ICommand
{
	@Override
	public final void action(final GuildMessageReceivedEvent e)
	{
		final var subreddit = e.getMessage().getContentRaw().substring(9);
		final var reddit = new Reddit().getSubReddit(subreddit);
		if (reddit == null)
		{
			e.getChannel().sendMessage(":no_entry: Subreddit not found.").queue(m -> {
				m.delete().queueAfter(5, TimeUnit.SECONDS);
				e.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
			});
			return;
		}
		final var eb = Utils.createEmbedBuilder(e.getAuthor());
		final var comIcon = reddit.getCommunityIcon().length() == 0 ? "https://canelex.ymasterskk.net/up/reddit.png" : reddit.getCommunityIcon();
		eb.setAuthor("r/" + reddit.getName(), "https://reddit.com/r/" + subreddit, "https://canelex.ymastersk.net/up/reddit.png");
		eb.setThumbnail(reddit.getIcon().length() == 0 ? comIcon : reddit.getIcon());
		eb.setColor(16727832);
		eb.addField("Subscribers", "**" + reddit.getSubs() + "**", false);
		eb.addField("Active users", "**" + reddit.getActive() + "**", false);
		eb.addField("Title", (reddit.getTitle().length() == 0 ? "**None**" : reddit.getTitle()), false);
		eb.addField("Description", (reddit.getDesc().length() == 0 ? "**None**" : reddit.getDesc()), false);
		eb.addField("NSFW", "**" + (reddit.isNsfw() ? "Yes" : "No") + "**", false);
		Utils.sendMessage(e.getChannel(), eb.build());
	}

	@Override
	public final String getDescription() { return "Shows you info about entered subreddit. For example `s!reddit PewdiepieSubmissions`."; }
	@Override
	public final boolean isAdmin() { return false; }
	@Override
	public final String getInvoke() { return "reddit"; }
	@Override
	public final Category getCategory() { return Category.SOCIAL; }
	@Override
	public final String getUsage() { return "s!reddit <subreddit>"; }
}