package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

import java.util.Optional;

/**
 * Leaving current party
 * <p>
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L31
 */
public class PartyLeaveRequest implements Packet {
	private final Optional<Integer> leaveId;

	public PartyLeaveRequest(int leaveId) //data only provided if kicked, else empty
	{
		this.leaveId = Optional.of(leaveId);
	}

	public PartyLeaveRequest()
	{
		this.leaveId = Optional.empty();
	}

	public Optional<Integer> leaveId()
	{
		return leaveId;
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
