package dev.mlnr.spidey.commands.settings;

import dev.mlnr.spidey.cache.GuildSettingsCache;
import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;

@SuppressWarnings("unused")
public class PrefixCommand extends Command
{
    public PrefixCommand()
    {
        super("prefix", new String[]{}, "Sets/removes the prefix for this server", "prefix (new prefix, if not given, the prefix will be reset if set)", Category.SETTINGS,
                Permission.MANAGE_SERVER, 0, 4);
    }

    @Override
    public void execute(final String[] args, final CommandContext ctx)
    {
        final var guild = ctx.getGuild();
        final var guildId = guild.getIdLong();
        final var currentPrefix = GuildSettingsCache.getPrefix(guildId);
        if (args.length == 0)
        {
            if (currentPrefix.equals("s!"))
                ctx.replyError("The prefix for this server is already set to the default one");
            else
            {
                GuildSettingsCache.setPrefix(guildId, "s!");
                ctx.reply(":white_check_mark: The prefix for this server has been reset to `s!`!");
            }
            return;
        }
        final var newPrefix = args[0];
        if (currentPrefix.equals(newPrefix))
        {
            ctx.replyError("The prefix for this server is already set to `" + currentPrefix + "`");
            return;
        }
        if (newPrefix.length() > 10)
        {
            ctx.replyError("The prefix can't be longer than 10 characters");
            return;
        }
        GuildSettingsCache.setPrefix(guildId, newPrefix);
        ctx.reply(":white_check_mark: The prefix has been successfully changed to `" + newPrefix + "`!");
    }
}