/*
 * This file is part of Araknemu.
 *
 * Araknemu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Araknemu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Araknemu.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2017-2019 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.network.game.in.party;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Make entire party follow provided player's moves
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L44">...</a>
 */
public final class PartyFollowAllRequest implements Packet {
    private final char startStop;
    private final int targetId;

    public PartyFollowAllRequest(char startStop, int targetId) {
        this.startStop = startStop;
        this.targetId = targetId;
    }

    public char startStop() {
        return startStop;
    }

    public int targetId() {
        return targetId;
    }

    public static final class Parser implements SinglePacketParser<PartyFollowAllRequest> {
        @Override
        public PartyFollowAllRequest parse(String input) throws ParsePacketException {
            if (input.length() < 2 || (input.charAt(0) != '+' && input.charAt(0) != '-')) {
                throw new ParsePacketException(input, "malformed packet");
            }
            try {

                Integer.parseInt(input.substring(1));
            } catch (NumberFormatException e) {
                throw new ParsePacketException(input, "malformed packet");
            }

            return new PartyFollowAllRequest(input.charAt(0), Integer.parseInt(input.substring(1)));
        }

        @Override
        public @MinLen(2) String code() {
            return "PG";
        }
    }
}
