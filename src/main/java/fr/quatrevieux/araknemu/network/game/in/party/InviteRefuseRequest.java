package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Refuse pending party request
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L23">...</a>
 */
public class InviteRefuseRequest implements Packet
{
	public static final class Parser implements SinglePacketParser<InviteRefuseRequest>
	{
		@Override
		public InviteRefuseRequest parse(String input) throws ParsePacketException
		{
			return new InviteRefuseRequest();
		}

		@Override
		public @MinLen(2) String code()
		{
			return "PR";
		}
	}
}
