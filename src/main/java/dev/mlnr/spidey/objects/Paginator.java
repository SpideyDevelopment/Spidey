package dev.mlnr.spidey.objects;

import dev.mlnr.spidey.cache.PaginatorCache;
import dev.mlnr.spidey.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.function.BiConsumer;

public class Paginator {
	private final long invokeChannelId;
	private final long paginatorMessageId;
	private final long authorId;
	private final int totalPages;
	private final BiConsumer<Integer, EmbedBuilder> pagesConsumer;
	private int currentPage;

	private final I18n i18n;
	private final PaginatorCache paginatorCache;

	public Paginator(long invokeChannelId, long paginatorMessageId, long authorId, int totalPages, BiConsumer<Integer, EmbedBuilder> pagesConsumer, I18n i18n, PaginatorCache paginatorCache) {
		this.invokeChannelId = invokeChannelId;
		this.paginatorMessageId = paginatorMessageId;
		this.authorId = authorId;
		this.totalPages = totalPages;
		this.pagesConsumer = pagesConsumer;

		this.i18n = i18n;
		this.paginatorCache = paginatorCache;
	}

	public void switchPage(Paginator.Action action, TextChannel channel) {
		var newPageBuilder = new EmbedBuilder().setColor(Utils.SPIDEY_COLOR);
		switch (action) {
			case REMOVE:
				paginatorCache.removePaginator(paginatorMessageId);
				return;
			case BACKWARDS:
				if (currentPage == 0) {
					return;
				}
				var previousPage = currentPage - 1;
				pagesConsumer.accept(previousPage, newPageBuilder);
				newPageBuilder.setFooter(i18n.get("paginator.page", previousPage + 1, totalPages));
				currentPage--;
				break;
			case FORWARD:
				var nextPage = currentPage + 1;
				if (nextPage == totalPages) {
					return;
				}
				pagesConsumer.accept(nextPage, newPageBuilder);
				newPageBuilder.setFooter(i18n.get("paginator.page", nextPage + 1, totalPages));
				currentPage++;
				break;
		}
		channel.editMessageById(paginatorMessageId, newPageBuilder.build()).queue();
	}

	public long getInvokeChannelId() {
		return this.invokeChannelId;
	}

	public long getAuthorId() {
		return this.authorId;
	}

	public enum Action {
		BACKWARDS,
		FORWARD,
		REMOVE
	}
}