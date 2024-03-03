package fr.quatrevieux.araknemu.network.game.out.party.invite;

public class InviteFailedCantFind
{
    private String inviteeName;

    public InviteFailedCantFind(String inviteeName)
    {
        this.inviteeName = inviteeName;
    }

    public String toString() { return "PIEn"+inviteeName; }
}
