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
public class Inviting implements Packet {

    public static final class Parser implements SinglePacketParser<Inviting> {
        @Override
        public Inviting parse(String input) throws ParsePacketException {
            return null;
        }
        @Override
        public @MinLen(2) String code() {
            return "PI";
        }
    }
}
