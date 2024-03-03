package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Accept pending party request
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L27
 */
public class InviteAcceptRequest implements Packet
{

    public static final class Parser implements SinglePacketParser<InviteAcceptRequest> {
        @Override
        public InviteAcceptRequest parse(String input) throws ParsePacketException {
            return new InviteAcceptRequest();
        }
        @Override
        public @MinLen(2) String code() {
            return "PA";
        }
    }
}
