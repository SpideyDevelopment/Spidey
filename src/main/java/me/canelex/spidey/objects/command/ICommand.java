package me.canelex.spidey.objects.command;

import me.canelex.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public interface ICommand
{
	void action(final GuildMessageReceivedEvent e);
	String getDescription();
	boolean isAdmin();
	String getInvoke();
	String getUsage();
    default List<String> getAliases() { return Collections.emptyList(); }
    Category getCategory();
}