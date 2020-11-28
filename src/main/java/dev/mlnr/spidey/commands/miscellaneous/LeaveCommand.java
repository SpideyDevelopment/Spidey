package dev.mlnr.spidey.commands.miscellaneous;

import dev.mlnr.spidey.Spidey;
import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import dev.mlnr.spidey.utils.Emojis;
import dev.mlnr.spidey.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static dev.mlnr.spidey.utils.Utils.addReaction;

@SuppressWarnings("unused")
public class LeaveCommand extends Command
{
	public LeaveCommand()
	{
		super("leave", new String[]{}, "Spidey will leave your server", "leave", Category.MISC, Permission.MANAGE_SERVER, 0, 0);
	}

	@Override
	public void execute(final String[] args, final CommandContext ctx)
	{
		final var guild = ctx.getGuild();
		final var channel = ctx.getTextChannel();

		Utils.deleteMessage(ctx.getMessage());
		channel.sendMessage("Do you really want me to leave?").queue(prompt ->
		{
			addReaction(prompt, Emojis.CHECK);
			addReaction(prompt, Emojis.CROSS);
			Spidey.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class,
					ev ->
					{
						final var name = ev.getReactionEmote().getName();
						return ev.getUser() == ctx.getAuthor() && ev.getMessageIdLong() == prompt.getIdLong() && (name.equals(Emojis.CHECK) || name.equals(Emojis.CROSS));
					},
					ev ->
					{
						if (ev.getReactionEmote().getName().equals(Emojis.CHECK))
						{
							Utils.deleteMessage(prompt);
							channel.sendMessage("I'm sad that i have to leave.. **Thanks for using me though**!")
									.delay(Duration.ofSeconds(5))
									.flatMap(Message::delete)
									.flatMap(ignored -> guild.leave())
									.queue();
							return;
						}
						Utils.deleteMessage(prompt);
						channel.sendMessage("I'm not leaving this time, yay. :tada:")
							   .delay(Duration.ofSeconds(5))
							   .flatMap(Message::delete)
							   .queue();
					}, 1, TimeUnit.MINUTES, () -> ctx.replyError("Sorry, but you took too long. I'm not leaving this time"));
			});
	}
}