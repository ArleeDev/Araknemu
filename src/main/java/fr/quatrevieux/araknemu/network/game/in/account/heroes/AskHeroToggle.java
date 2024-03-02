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
 * Copyright (c) 2017-2020 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.network.game.in.account.heroes;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;


public final class AskHeroToggle implements Packet {

    final int characterId;
    final boolean value;

    public AskHeroToggle(int characterId, boolean value)
    {
        this.characterId=characterId;
        this.value=value;
    }

    public static final class Parser implements SinglePacketParser<AskHeroToggle> {
        @Override
        public AskHeroToggle parse(String input) throws ParsePacketException
        {
            String[] values = input.split("\\|");
            if(values.length!=2)
                throw new RuntimeException("Malformed packet: "+input);

            return new AskHeroToggle(Integer.parseInt(values[0]),Boolean.parseBoolean(values[1]));
        }

        @Override
        public @MinLen(3) String code() {
            return "AEb";
        }
    }
}
