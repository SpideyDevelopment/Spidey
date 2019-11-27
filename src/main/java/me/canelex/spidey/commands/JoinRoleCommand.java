package me.canelex.spidey.commands;

import me.canelex.jda.api.Permission;
import me.canelex.jda.api.entities.Message;
import me.canelex.spidey.MySQL;
import me.canelex.spidey.objects.command.Category;
import me.canelex.spidey.objects.command.ICommand;
import me.canelex.spidey.utils.PermissionError;
import me.canelex.spidey.utils.Utils;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class JoinRoleCommand implements ICommand
{
    private static final Pattern ID_PATTERN = Pattern.compile("\\d+");

    @Override
    public final void action(final String[] args, final Message message)
    {
        final var guild = message.getGuild();
        final var guildId = guild.getIdLong();
        final var channel = message.getChannel();
        final var member = message.getMember();
        final var requiredPermission = getRequiredPermission();

        if (!Utils.hasPerm(member, requiredPermission))
        {
            Utils.sendMessage(channel, PermissionError.getErrorMessage(requiredPermission), false);
            return;
        }

        final var dbRole = MySQL.getRole(guildId);
        if (args.length == 1)
        {
            if (dbRole == 0)
                Utils.returnError("You don't have the join Role set", message);
            else
            {
                MySQL.removeRole(guildId);
                Utils.sendMessage(channel, ":white_check_mark: The join Role has been removed.", false);
            }
            return;
        }
        if (!ID_PATTERN.matcher(args[1]).matches())
        {
            Utils.returnError("Please enter a valid ID", message);
            return;
        }

        final var roleId = Long.parseUnsignedLong(args[1]);
        if (dbRole == roleId)
        {
            Utils.returnError("The join Role is already set to this Role", message);
            return;
        }

        final var role = guild.getRoleById(roleId);
        if (role == null)
        {
            Utils.returnError("There is no such Role in this guild", message);
            return;
        }
        if (!member.canInteract(role))
            Utils.returnError("You can't set the join Role to a Role which you can't interact with", message);
        else
        {
            MySQL.setRole(guildId, roleId);
            Utils.sendMessage(channel, ":white_check_mark: The join Role has been set to Role `" + role.getName() + "`.", false);
        }
    }

    @Override
    public final String getDescription() { return "Sets/removes the Role that is added to a member after joining"; }
    @Override
    public final Permission getRequiredPermission() { return Permission.ADMINISTRATOR; }
    @Override
    public final String getInvoke() { return "joinrole"; }
    @Override
    public final Category getCategory() { return Category.UTILITY; }
    @Override
    public final String getUsage() { return "s!joinrole (id of the Role, if not given, the Role will be reset if set)"; }
}