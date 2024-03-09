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

package fr.quatrevieux.araknemu.game.handler.spell;

import fr.quatrevieux.araknemu.core.network.exception.ErrorPacket;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.spell.SpellDowngradeRequest;
import fr.quatrevieux.araknemu.network.game.out.spell.SpellDowngradeError;
import org.checkerframework.checker.nullness.util.NullnessUtil;

/**
 * Upgrade the spell
 */
public final class DowngradeSpell implements PacketHandler<GameSession, SpellDowngradeRequest> {
    @Override
    public void handle(GameSession session, SpellDowngradeRequest packet) throws Exception {
        try {
            NullnessUtil.castNonNull(session.player()).properties().spells()
                    .entry(packet.spellId())
                    .downgrade()
            ;
        } catch (Exception e) {
            throw new ErrorPacket(new SpellDowngradeError(), e);
        }
    }

    @Override
    public Class<SpellDowngradeRequest> packet() {
        return SpellDowngradeRequest.class;
    }
}
