package me.canelex.spidey.objects.search;

import me.canelex.spidey.Secrets;
import me.canelex.spidey.utils.Utils;

public class GoogleSearch
{
    private GoogleSearch()
    {
        super();
    }

    private static final String GOOGLE_URL = "https://www.googleapis.com/customsearch/v1/?cx=%s&key=%s&num=1&q=%s";
    private static final String GOOGLE_API_KEY = Secrets.GOOGLE_API_KEY;

    public static SearchResult performSearch(String terms)
    {
        terms = terms.replace(" ", "%20");
        final var searchUrl = String.format(GOOGLE_URL, "015021391643023377625:kq7ex3xgvoq", GOOGLE_API_KEY, terms);
        final var o = Utils.getJson(searchUrl).getArray("items").getObject(0);
        return SearchResult.fromGoogle(o);
    }
}