package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Displays all party members' locations on the minimap
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L40">...</a>
 */
public class PartyPositionsRequest implements Packet {

    public static final class Parser implements SinglePacketParser<PartyPositionsRequest> {
        @Override
        public PartyPositionsRequest parse(String input) throws ParsePacketException {
            return new PartyPositionsRequest();
        }
        @Override
        public @MinLen(2) String code() {
            return "PW";
        }
    }
}