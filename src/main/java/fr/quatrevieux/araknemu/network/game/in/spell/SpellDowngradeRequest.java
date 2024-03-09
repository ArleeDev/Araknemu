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
 * Copyright (c) 2017-2024 ArleeDev
 */

package fr.quatrevieux.araknemu.network.game.in.spell;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.PacketTokenizer;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import fr.quatrevieux.araknemu.util.ParseUtils;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Ask for downgrade a spell
 */
public final class SpellDowngradeRequest implements Packet {
    private final @NonNegative int heroId;
    private final @NonNegative int spellId;

    public SpellDowngradeRequest(@NonNegative int heroId, @NonNegative int spellId) {
        this.heroId = heroId;
        this.spellId = spellId;
    }

    public @NonNegative int heroId() {
        return heroId;
    }

    public @NonNegative int spellId() {
        return spellId;
    }

    public static final class Parser implements SinglePacketParser<SpellDowngradeRequest> {
        @Override
        public SpellDowngradeRequest parse(String input) throws ParsePacketException {
            final PacketTokenizer tokenizer = tokenize(input, ';');

            return new SpellDowngradeRequest(ParseUtils.parseNonNegativeInt(tokenizer.nextPart()), //heroID
                    ParseUtils.parseNonNegativeInt(tokenizer.nextPart())); //spellID
        }

        @Override
        public @MinLen(2) String code() {
            return "Aps";
        }
    }
}
