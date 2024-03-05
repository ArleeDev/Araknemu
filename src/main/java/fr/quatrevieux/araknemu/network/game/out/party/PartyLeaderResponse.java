package fr.quatrevieux.araknemu.network.game.out.party;

/**
 * Assigns a character as the party's leader
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L99">...</a>
 */
public class PartyLeaderResponse {
	private final int leaderId;

	public PartyLeaderResponse(int leaderId)
	{
		this.leaderId = leaderId;
	}

	@Override
	public String toString()
	{
		return "PL"+leaderId;
	}
}
