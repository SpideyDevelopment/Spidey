package me.canelex.spidey.objects.json;

import me.canelex.jda.api.utils.data.DataObject;
import me.canelex.spidey.utils.Utils;

public class Reddit
{
    private int subs;
    private String name;
    private String desc;
    private String title;
    private int active;
    private boolean nsfw;
    private String icon;
    private String comIcon;

    public final Reddit getSubReddit(final String name)
    {
        return !exists(name) ? null : fromJson(Utils.getJson(
                "https://reddit.com/r/" + name + "/about.json"));
    }

    private boolean exists(final String name)
    {
        return 1 == Utils.getJson("https://reddit.com/subreddits/search.json?limit=1&q=" + name).getObject("data").getInt("dist");
    }

    private Reddit fromJson(final DataObject o)
    {
        final var data = o.getObject("data");
        this.subs = data.getInt("subscribers");
        this.name = data.getString("display_name");
        this.desc = Utils.cleanString(data.getString("public_description"));
        this.title = data.getString("title");
        this.active = data.getInt("accounts_active");
        this.nsfw = data.getBoolean("over18");
        this.icon = data.getString("icon_img");
        this.comIcon = data.getString("community_icon");
        return this;
    }

    public final int getSubs(){ return subs; }
    public final String getName() { return name; }
    public final String getDesc() { return desc; }
    public final String getTitle() { return title; }
    public final int getActive() { return active; }
    public final boolean isNsfw() { return nsfw; }
    public final String getIcon() { return icon; }
    public final String getCommunityIcon() { return comIcon; }
}