package fr.quatrevieux.araknemu.network.game.out.party;

/**
 * Creates a party
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L120
 */
public class PartyJoined
{
    private final String playername;

    public PartyJoined(String playername)
    {
        this.playername = playername;
    }

    @Override
    public String toString() {
        return "PCK"+playername;
    }
}
