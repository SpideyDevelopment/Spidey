package me.canelex.spidey.objects.search;

import me.canelex.spidey.Secrets;
import me.canelex.spidey.utils.Utils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static me.canelex.spidey.utils.Utils.cleanString;

public class GoogleSearch
{
    private String title;
    private String content;
    private String url;
    private static final String GOOGLE_URL = "https://www.googleapis.com/customsearch/v1/?cx=015021391643023377625:kq7ex3xgvoq&key=" + Secrets.GOOGLE_API_KEY + "&num=1&q=%s";

    public final GoogleSearch getResult(final String terms)
    {
        final var searchUrl = String.format(GOOGLE_URL, terms.replace(" ", "%20"));
        final var o = Utils.getJson(searchUrl).getArray("items").getObject(0);
        this.title = cleanString(o.getString("title"));
        this.content = cleanString(o.getString("snippet")).replaceAll("\\s+", " ");
        this.url = URLDecoder.decode(cleanString(o.getString("link")), StandardCharsets.UTF_8);
        return this;
    }

    public final String getContent()
    {
        if (url.startsWith("https://www.youtube.com/watch?"))
            return "<" + url.replace("https://www.youtube.com/watch?v=", "https://youtu.be/") + "> - *" + title + "*: \"" + content + "\"";
        else
            return "<" + url.replace("www.", "") + "> - *" + title + "*: \"" + content + "\"";
    }
}