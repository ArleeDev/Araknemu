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

package fr.quatrevieux.araknemu.game.handler.account;

import fr.quatrevieux.araknemu.core.network.exception.ErrorPacket;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.account.AskRestat;
import fr.quatrevieux.araknemu.network.game.out.basic.Noop;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public class RestatCharacter implements PacketHandler<GameSession, AskRestat> {

    @Override
    public void handle(GameSession session, AskRestat packet) throws Exception {
        try {
            validate(session, packet);
            NullnessUtil.castNonNull(session.player()).properties().characteristics().restat();
        } catch (RuntimeException e) {
            throw new ErrorPacket(new Noop(), e);
        }
    }

    @Override
    public Class<AskRestat> packet() {
        return AskRestat.class;
    }

    private boolean validate(GameSession session, AskRestat packet) {
        if (session == null) {
            throw new RuntimeException("current session does not exist");
        } else if (session.player() == null) {
            throw new RuntimeException("current session does not contain an active player");
        } else if (NullnessUtil.castNonNull(session.player()).id() != packet.playerId()) {
            throw new RuntimeException("current session playerId does not match packet's playerId");
        }

        return true;
    }
}
