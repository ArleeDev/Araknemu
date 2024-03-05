package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;


/**
 * Invite request
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L19
 */
public class InviteRequest implements Packet {
	public final String inviteeName;

	public InviteRequest(String inviteeName)
	{
		this.inviteeName = inviteeName;
	}

	public static final class Parser implements SinglePacketParser<InviteRequest> {
		@Override
		public InviteRequest parse(String input) throws ParsePacketException
		{
			return new InviteRequest(input);
		}

		@Override
		public @MinLen(2) String code()
		{
			return "PI";
		}
	}
}
