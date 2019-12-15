package me.canelex.spidey.commands;

import me.canelex.jda.api.entities.Message;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.Utils;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class InviteCommand implements ICommand
{
    @Override
    public final void action(final String[] args, final Message message)
    {
        message.delete().queueAfter(5, TimeUnit.SECONDS);
        Utils.sendPrivateMessageFormat(message.getAuthor(), "Link for inviting me: ||" + Utils.getInviteUrl() + "||");
        message.getChannel().sendMessage(":white_check_mark: Sent you an invite link.").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
    }

    @Override
    public final String getDescription() { return "Sends you Spidey's invite link to PM"; }
    @Override
    public final String getInvoke() { return "invite"; }
    @Override
    public final Category getCategory() { return Category.MISC; }
    @Override
    public final String getUsage() { return "s!invite"; }
}