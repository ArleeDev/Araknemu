package fr.quatrevieux.araknemu.network.game.out.party.invite;

import fr.quatrevieux.araknemu.game.player.GamePlayer;

/**
 * Sends a party invite request
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L48
 */
public class Invited {
    private final GamePlayer inviter;
    private final GamePlayer invitee;


    public Invited(GamePlayer inviter, GamePlayer invitee)
    {
        this.inviter=inviter;
        this.invitee=invitee;
    }

    public String toString() {
        return "PIK"+inviter.name()+"|"+invitee.name();
    }
}
