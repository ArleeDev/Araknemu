package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Make entire party follow provided player's moves
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L44">...</a>
 */
public class PartyFollowAllRequest implements Packet {

    public final char startStop;
    public final int targetId;

    public PartyFollowAllRequest(char startStop, int targetId)
    {
        this.startStop = startStop;
        this.targetId = targetId;
    }

    public static final class Parser implements SinglePacketParser<PartyFollowAllRequest> {
        @Override
        public PartyFollowAllRequest parse(String input) throws ParsePacketException
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

            return new PartyFollowAllRequest(input.charAt(0),Integer.parseInt(input.substring(1)));
        }

        @Override
        public @MinLen(2) String code()
        {
            return "PG";
        }
    }
}
