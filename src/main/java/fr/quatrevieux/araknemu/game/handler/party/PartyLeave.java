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
 * Copyright (c) 2017-2024 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.exception.ErrorPacket;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.PartyLeaveRequest;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class PartyLeave implements PacketHandler<GameSession, PartyLeaveRequest> {

    private final PartyService partyService;

    public PartyLeave(PartyService partyService) {
        this.partyService = partyService;
    }

    @Override
    public void handle(GameSession session, PartyLeaveRequest inPacket) throws Exception {
        try {
            final GamePlayer player = NullnessUtil.castNonNull(session.player());
            if (inPacket.kicked()) {
                partyService.kick(player, inPacket.leaveId());
            } else {
                partyService.leave(player);
            }
        } catch (Exception e) {
            throw new ErrorPacket(inPacket, e);
        }
    }

    @Override
    public Class<PartyLeaveRequest> packet() {
        return PartyLeaveRequest.class;
    }
}
