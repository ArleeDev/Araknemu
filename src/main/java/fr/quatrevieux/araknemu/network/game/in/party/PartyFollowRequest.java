package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Follow provided player on the (mini)map
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L36">...</a>
 */
public class PartyFollowRequest implements Packet
{
	public final char startStop;
	public final int targetId;


	public PartyFollowRequest(char startStop, int targetId)
	{
		this.startStop = startStop;
		this.targetId = targetId;
	}

	public static final class Parser implements SinglePacketParser<PartyFollowRequest> {
		@Override
		public PartyFollowRequest parse(String input) throws ParsePacketException
		{
			if(input.length()<2||(input.charAt(0)!='+'&&input.charAt(0)!='-'))
				throw new ParsePacketException(input, "malformed packet");
			try
			{

				Integer.parseInt(input.substring(1));
			} catch (NumberFormatException e)
			{
				throw new ParsePacketException(input, "malformed packet");
			}

			return new PartyFollowRequest(input.charAt(0),Integer.parseInt(input.substring(1)));
		}

		@Override
		public @MinLen(2) String code()
		{
			return "PF";
		}
	}
}
