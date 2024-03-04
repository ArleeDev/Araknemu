package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Refuse pending party request
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L23
 */
public class InviteRefuseRequest implements Packet
{

	private final String code;

	public InviteRefuseRequest(String code)
	{
		this.code = code;
	}

	public String code()
	{
		return code;
	}

	public static final class Parser implements SinglePacketParser<InviteRefuseRequest>
	{
		@Override
		public InviteRefuseRequest parse(String input) throws ParsePacketException
		{
			return new InviteRefuseRequest(code());
		}

		@Override
		public @MinLen(2) String code()
		{
			return "PR";
		}
	}
}
