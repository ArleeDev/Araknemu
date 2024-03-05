package fr.quatrevieux.araknemu.network.game.out.party;

public class PartyCreatedResponse
{

	/**
	 * Creates a party
	 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L120">...</a>
	 */
	public static class Created
	{
		private final String playername;

		public Created(String playername)
		{
			this.playername = playername;
		}

		@Override
		public String toString() {
			return "PCK"+playername;
		}
	}

	/**
	 * Error upon trying to invite a player to your party when it is already full
	 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L142">...</a>
	 */
	public static class FailedFull
	{
		@Override
		public String toString()
		{
			return "PCEf";
		}
	}

	/**
	 * Error upon trying to invite a player who is already in a party
	 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L137">...</a>
	 */
	public static class FailedAlreadyGrouped
	{
		@Override
		public String toString()
		{
			return "PCEa";
		}
	}
}
