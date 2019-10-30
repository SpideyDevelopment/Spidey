package me.canelex.spidey.objects.invites;

import me.canelex.jda.api.entities.Invite;

public class WrappedInvite
{
    private long guildId;
    private int uses;

    public WrappedInvite(final Invite invite)
    {
        this.guildId = invite.getGuild().getIdLong();
        this.uses = invite.getUses();
    }

    public long getGuildId()
    {
        return guildId;
    }

    public int getUses()
    {
        return uses;
    }

    public void incrementUses()
    {
        this.uses++;
    }
}