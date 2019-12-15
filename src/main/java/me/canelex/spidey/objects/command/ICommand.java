package me.canelex.spidey.objects.command;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Message;

import java.util.Collections;
import java.util.List;

public interface ICommand
{
	void action(final String[] args, final Message message);
	String getDescription();
	String getInvoke();
	String getUsage();
    Category getCategory();
	default Permission getRequiredPermission() { return Permission.UNKNOWN; }
	default List<String> getAliases() { return Collections.emptyList(); }
	default int getMaxArgs() { return 0; }
}