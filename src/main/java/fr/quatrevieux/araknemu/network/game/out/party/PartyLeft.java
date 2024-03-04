package fr.quatrevieux.araknemu.network.game.out.party;

/**
 * Leaves the current party
 * <p>
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L150
 */
public class PartyLeft {

	private final String kickerName;

	public PartyLeft(String kickerName) //kicked by
	{
		this.kickerName = kickerName;
	}

	public PartyLeft() //self leave
	{
		this.kickerName = "";
	}

	@Override
	public String toString()
	{
		return "PV"+kickerName;
	}
}
