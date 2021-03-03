package dev.mlnr.spidey.handlers.command;

import dev.mlnr.spidey.Spidey;
import dev.mlnr.spidey.cache.Cache;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import dev.mlnr.spidey.objects.command.category.Category;
import dev.mlnr.spidey.utils.KSoftAPIHelper;
import dev.mlnr.spidey.utils.StringUtils;
import dev.mlnr.spidey.utils.Utils;
import io.github.classgraph.ClassGraph;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static dev.mlnr.spidey.handlers.command.CooldownHandler.cooldown;
import static dev.mlnr.spidey.handlers.command.CooldownHandler.isOnCooldown;

public class CommandHandler {
	private static final Map<String, Command> COMMANDS = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	static {
		try (var result = new ClassGraph().acceptPackages("dev.mlnr.spidey.commands").scan()) {
			for (var cls : result.getAllClasses()) {
				var cmd = (Command) cls.loadClass().getDeclaredConstructor().newInstance();
				COMMANDS.put(cmd.getInvoke(), cmd);
				for (var alias : cmd.getAliases())
					COMMANDS.put(alias, cmd);
			}
		}
		catch (Exception e) {
			logger.error("There was an error while registering the commands!", e);
		}
	}

	private CommandHandler() {}

	public static void handle(GuildMessageReceivedEvent event, String prefix, Spidey spidey, Cache cache) {
		var message = event.getMessage();
		var content = message.getContentRaw().substring(prefix.length()).trim();
		var guildSettingsCache = cache.getGuildSettingsCache();
		var guildId = message.getGuild().getIdLong();
		var i18n = guildSettingsCache.getMiscSettings(guildId).getI18n();
		if (content.isEmpty()) {
			Utils.returnError(i18n.get("command_failures.specify"), message);
			return;
		}
		var command = (content.contains(" ") ? content.substring(0, content.indexOf(' ')) : content).toLowerCase();
		var cmd = COMMANDS.get(command);
		if (cmd == null) {
			var similar = StringUtils.getSimilarCommand(command);
			Utils.returnError(i18n.get("command_failures.invalid.message", command) + " " + (similar == null
					? i18n.get("command_failures.invalid.check_help", prefix)
					: i18n.get("command_failures.invalid.suggestion", similar)), message);
			return;
		}
		var requiredPermission = cmd.getRequiredPermission();
		var member = message.getMember();
		var userId = member.getIdLong();
		if (!member.hasPermission(requiredPermission)) {
			Utils.returnError(i18n.get("command_failures.no_perms", requiredPermission.getName()), message);
			return;
		}
		if (isOnCooldown(userId, cmd)) {
			Utils.returnError(i18n.get("command_failures.cooldown"), message);
			return;
		}

		var vip = guildSettingsCache.getGeneralSettings(guildId).isVip();

		// NSFW COMMANDS HANDLING
		var channel = message.getTextChannel();
		var category = cmd.getCategory();
		if (category == Category.NSFW) {
			if (!channel.isNSFW()) {
				Utils.returnError(i18n.get("command_failures.only_nsfw"), message);
				return;
			}
			Utils.sendMessage(channel, KSoftAPIHelper.getNsfwImage(cmd.getInvoke(), member), message);
			cooldown(userId, cmd, vip);
			return;
		}
		//

		var maxArgs = cmd.getMaxArgs();
		var tmp = content.split("\\s+", maxArgs > 0 ? maxArgs + 1 : 0);
		var args = Arrays.copyOfRange(tmp, 1, tmp.length);
		cmd.execute(args, new CommandContext(args, event, i18n, spidey, cache));
		cooldown(userId, cmd, vip);
	}

	public static Map<String, Command> getCommands() {
		return COMMANDS;
	}
}