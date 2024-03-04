package fr.quatrevieux.araknemu.network.game.out.party;

/**
 * Assigns a character as the party's leader
 * <p>
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L99
 */
public class PartyLeaderAssigned {
	private final int leaderId;

	public PartyLeaderAssigned(int leaderId)
	{
		this.leaderId = leaderId;
	}

	@Override
	public String toString()
	{
		return "PL"+leaderId;
	}
}
