package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Follow entire party's movements on the (mini)map
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L44
 */
public class PartyFollowingAll implements Packet {

    public static final class Parser implements SinglePacketParser<PartyFollowingAll> {
        @Override
        public PartyFollowingAll parse(String input) throws ParsePacketException {
            return null;
        }
        @Override
        public @MinLen(2) String code() {
            return "PG";
        }
    }
}
