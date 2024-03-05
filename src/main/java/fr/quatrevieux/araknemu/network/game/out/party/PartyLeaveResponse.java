package fr.quatrevieux.araknemu.network.game.out.party;

/**
 * Leaves the current party
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L150">...</a>
 */
public class PartyLeaveResponse {

	public static class Leave {
		@Override
		public String toString()
		{
			return "PV";
		}
	}

	public static class Kicked {
		private final String kickerName;

		public Kicked(String kickerName)
		{
			this.kickerName = kickerName;
		}

		@Override
		public String toString()
		{
			return "PV"+kickerName;
		}
	}
}

