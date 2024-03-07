package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Leaving current party
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L31">...</a>
 */
public class PartyLeaveRequest implements Packet {
	public final int leaveId;
	public final boolean kicked;

	public PartyLeaveRequest(int leaveId)
	{
		this.kicked=true;
		this.leaveId=leaveId;
	}

	public PartyLeaveRequest()
	{
		this.kicked=false;
		this.leaveId=-1; //not used
	}

	public static final class Parser implements SinglePacketParser<PartyLeaveRequest> {
		@Override
		public PartyLeaveRequest parse(String input) throws ParsePacketException
		{
			if(!input.isEmpty())
				return new PartyLeaveRequest(Integer.parseInt(input));
			return new PartyLeaveRequest();
		}

		@Override
		public @MinLen(2) String code()
		{
			return "PV";
		}
	}
}
