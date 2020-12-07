package dev.mlnr.spidey.commands.miscellaneous;

import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;

@SuppressWarnings("unused")
public class InviteCommand extends Command
{
    public InviteCommand()
    {
        super("invite", new String[]{}, "Sends you Spidey's invite link to PM if possible", "invite", Category.MISC, Permission.UNKNOWN, 0, 0);
    }

    @Override
    public void execute(final String[] args, final CommandContext ctx)
    {
        ctx.getAuthor().openPrivateChannel()
           .flatMap(channel -> channel.sendMessage("Link for inviting me: https://spidey.mlnr.dev"))
           .onErrorFlatMap(ignored -> ctx.getTextChannel().sendMessage("I couldn't send you a PM, here's the link: https://spidey.mlnr.dev"))
           .queue();
    }
}