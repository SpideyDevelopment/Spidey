package dev.mlnr.spidey.objects.command;

import dev.mlnr.spidey.cache.Cache;
import dev.mlnr.spidey.objects.I18n;
import dev.mlnr.spidey.utils.ArgumentUtils;
import dev.mlnr.spidey.utils.Emojis;
import dev.mlnr.spidey.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class CommandContext {
	private final String[] args;
	private final GuildMessageReceivedEvent event;
	private final I18n i18n;

	private final Cache cache;

	public CommandContext(String[] args, GuildMessageReceivedEvent event, I18n i18n, Cache cache) {
		this.args = args;
		this.event = event;
		this.i18n = i18n;

		this.cache = cache;
	}

	public Message getMessage() {
		return event.getMessage();
	}

	public User getAuthor() {
		return event.getAuthor();
	}

	public Member getMember() {
		return event.getMember();
	}

	public TextChannel getTextChannel() {
		return event.getChannel();
	}

	public Guild getGuild() {
		return event.getGuild();
	}

	public JDA getJDA() {
		return event.getJDA();
	}

	public I18n getI18n() {
		return this.i18n;
	}

	public GuildMessageReceivedEvent getEvent() {
		return this.event;
	}

	public Cache getCache() {
		return cache;
	}

	// interaction methods

	public void reply(EmbedBuilder embedBuilder) {
		Utils.sendMessage(getTextChannel(), embedBuilder.build(), getMessage());
	}

	public void reply(String content) {
		Utils.sendMessage(getTextChannel(), content, getMessage());
	}

	public void replyLocalized(String key, Object... args) {
		reply(i18n.get(key, args));
	}

	public void replyError(String error) {
		replyError(error, Emojis.CROSS);
	}

	public void replyErrorLocalized(String key, Object... args) {
		replyError(i18n.get(key, args));
	}

	public void replyError(String error, String failureEmoji) {
		Utils.returnError(error, getMessage(), failureEmoji);
	}

	public void reactLike() {
		Utils.addReaction(getMessage(), Emojis.LIKE);
	}

	// arg stuff

	public void getArgumentAsInt(int argIndex, IntConsumer consumer) {
		ArgumentUtils.parseArgumentAsInt(args[argIndex], this, consumer);
	}

	public void getArgumentAsUnsignedInt(int argIndex, IntConsumer consumer) {
		ArgumentUtils.parseArgumentAsUnsignedInt(args[argIndex], this, consumer);
	}

	public void getArgumentAsUser(int argIndex, Consumer<User> consumer) {
		ArgumentUtils.parseArgumentAsUser(args[argIndex], this, consumer);
	}

	public void getArgumentAsRole(int argIndex, Consumer<Role> consumer) {
		ArgumentUtils.parseArgumentAsRole(args[argIndex], this, consumer);
	}

	public void getArgumentAsTextChannel(int argIndex, Consumer<TextChannel> consumer) {
		ArgumentUtils.parseArgumentAsTextChannel(args[argIndex], this, consumer);
	}

	public void getArgumentAsVoiceChannel(int argIndex, Consumer<VoiceChannel> consumer) {
		ArgumentUtils.parseArgumentAsVoiceChannel(args[argIndex], this, consumer);
	}
}