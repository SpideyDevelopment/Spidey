package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Message;
import me.canelex.spidey.Core;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
public class HelpCommand extends Core implements ICommand
{
    @Override
    public final void action(final String[] args, final Message message)
    {
        final HashMap<String, ICommand> commandsCopy = new HashMap<>();
        final var commandsMap = Core.getCommands();
        final var channel = message.getChannel();
        final var author = message.getAuthor();
        final var eb = Utils.createEmbedBuilder(author)
                            .setColor(Color.WHITE)
                            .setAuthor("Spidey's Commands", "https://github.com/caneleex/Spidey", message.getJDA().getSelfUser().getAvatarUrl());

        if (args.length < 2)
        {
            commandsMap.forEach(commandsCopy::put);
            final var iter = commandsCopy.entrySet().iterator();
            final var valueSet = new HashSet<>();
            while (iter.hasNext())
            {
                if (!valueSet.add(iter.next().getValue()))
                    iter.remove();
            }
            commandsCopy.remove("help");
            commandsCopy.entrySet().removeIf(entry -> !Utils.hasPerm(message.getMember(), entry.getValue().getRequiredPermission()));

            final HashMap<Category, List<ICommand>> categories = new HashMap<>();
            commandsCopy.values().forEach(cmd ->
            {
                final var list = categories.computeIfAbsent(cmd.getCategory(), ignored -> new ArrayList<>());
                list.add(cmd);
            });

            final var sb = new StringBuilder();
            categories.forEach((category, commandz) ->
            {
                sb.append("\n");
                sb.append(category.getFriendlyName());
                sb.append(" ").append("-").append(" ");
                sb.append(listToString(commandz, ICommand::getInvoke));
                eb.setDescription("Prefix: **s!**\n" + sb.toString());
            });
            Utils.sendMessage(channel, eb.build());
        }
        else
        {
            final var cmd = message.getContentRaw().substring(7);
            if (!commandsMap.containsKey(cmd))
                Utils.sendMessage(channel, ":no_entry: **" + cmd + "** isn't a valid command.", false);
            else
            {
                final var command = commandsMap.get(cmd);
                final var description = command.getDescription();
                final var usage = command.getUsage();
                final var requiredPermission = command.getRequiredPermission();
                final var aliases = command.getAliases();
                eb.setAuthor("Viewing command info - " + cmd);
                eb.setColor(Color.WHITE);
                eb.addField("Description", description == null ? "Unspecified" : description, false);
                eb.addField("Usage", usage == null ? "Unspecified" : "`" + usage + "` (<> = required, () = optional)", false);
                eb.addField("Category",  command.getCategory().getFriendlyName(), false);
                eb.addField("Required permission", requiredPermission == Permission.UNKNOWN ? "None" : requiredPermission.getName(), false);
                eb.addField("Aliases", aliases.isEmpty() ? "None" : String.join(", ", aliases), false);
                Utils.sendMessage(channel, eb.build());
            }
        }
    }

    private String listToString(final List<ICommand> list, final Function<ICommand, String> transformer)
    {
        final var builder = new StringBuilder();
        for (var i = 0; i < list.size(); i++)
        {
            final var cmd = list.get(i);
            final var aliases = cmd.getAliases();
            builder.append("`").append(transformer.apply(cmd)).append("`").append(aliases.isEmpty() ? "" : " (`" + String.join(", ", aliases) + "`)");
            if (i != list.size() - 1)
                builder.append(", ");
        }
        return builder.toString();
    }

    @Override
    public final String getInvoke() { return "help"; }
    @Override
    public final String getDescription() { return "Shows the help message"; }
    @Override
    public final String getUsage() { return "s!help (command)"; }
    @Override
    public final Category getCategory() { return Category.INFORMATIVE; }
}