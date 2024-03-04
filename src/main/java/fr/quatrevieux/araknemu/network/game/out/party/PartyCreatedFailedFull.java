package fr.quatrevieux.araknemu.network.game.out.party;

/**
 * Error upon trying to invite a player to your party when it is already full
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L142
 */
public class PartyCreatedFailedFull
{

	@Override
	public String toString()
	{
		return "PCEf";
	}
}
