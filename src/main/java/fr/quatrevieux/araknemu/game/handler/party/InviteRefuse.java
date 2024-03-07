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

import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyInviteService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.InviteRefuseRequest;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class InviteRefuse implements PacketHandler<GameSession, InviteRefuseRequest> {

    private final PartyInviteService partyInviteService;

    public InviteRefuse(PartyInviteService partyInviteService) {
        this.partyInviteService = partyInviteService;
    }

    @Override
    public void handle(GameSession session, InviteRefuseRequest inPacket) throws Exception {
        final GamePlayer player = NullnessUtil.castNonNull(session.player());
        partyInviteService.refuse(player);
    }

    @Override
    public Class<InviteRefuseRequest> packet() {
        return InviteRefuseRequest.class;
    }
}
