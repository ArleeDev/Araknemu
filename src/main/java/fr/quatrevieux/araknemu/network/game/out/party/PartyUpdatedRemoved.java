package fr.quatrevieux.araknemu.network.game.out.party;

/**
 * Constructs the party menu UIElement upon a player leaving the party
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L227
 */
public class PartyUpdatedRemoved
{
	private final int id;

	public PartyUpdatedRemoved(int id)
	{
		this.id = id;
	}

	@Override
	public String toString()
	{
		return "PM-"+id;
	}
}
