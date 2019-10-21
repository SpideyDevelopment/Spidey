package me.canelex.spidey.objects.command;

public enum Category
{
    MODERATION("\uD83D\uDD28 Moderation"),
    MISC("\uD83D\uDCA1 Miscellaneous"),
    UTILITY("\uD83D\uDEE0 Utility"),
    INFORMATIVE("\u2139 Informative"),
    SOCIAL("\uD83D\uDCF1 Social");

    private final String friendlyName;

    Category(final String friendlyName)
    {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName()
    {
        return friendlyName;
    }
}