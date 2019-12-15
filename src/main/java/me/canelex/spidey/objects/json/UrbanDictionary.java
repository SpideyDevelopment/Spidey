package me.canelex.spidey.objects.json;

import me.canelex.jda.api.utils.data.DataObject;
import me.canelex.spidey.utils.Utils;

public class UrbanDictionary
{
    private String author;
    private String definition;
    private String example;
    private String word;
    private int likes;
    private int dislikes;

    public final UrbanDictionary getTerm(final String term)
    {
        return fromJson(Utils.getJson("http://api.urbandictionary.com/v0/define?term=" + term));
    }

    private UrbanDictionary fromJson(final DataObject o)
    {
        final var data = o.getArray("list").getObject(0);
        this.author = data.getString("author");
        this.definition = data.getString("definition");
        this.example = data.getString("example");
        this.word = data.getString("word");
        this.likes = data.getInt("thumbs_up");
        this.dislikes = data.getInt("thumbs_down");
        return this;
    }

    public final String getAuthor() { return author; }
    public final String getDefinition() { return definition; }
    public final String getExample() { return example; }
    public final String getWord() { return word; }
    public final int getLikes() { return likes; }
    public final int getDislikes() { return dislikes; }
}