package fr.quatrevieux.araknemu.network.game.out.party.invite;

/**
 * Denies a party invite request if the target(invitee) is unfindable (e.g. logged out)
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L91
 */
public class InviteFailedCantFind
{
    private String inviteeName;

    public InviteFailedCantFind(String inviteeName)
    {
        this.inviteeName = inviteeName;
    }

    public String toString() { return "PIEn"+inviteeName; }
}
