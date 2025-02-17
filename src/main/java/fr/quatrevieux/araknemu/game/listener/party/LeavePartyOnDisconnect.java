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

package fr.quatrevieux.araknemu.game.listener.party;

import fr.quatrevieux.araknemu.core.event.Listener;
import fr.quatrevieux.araknemu.game.handler.event.Disconnected;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;

/**
 * Leave the party when disconnect
 */
public final class LeavePartyOnDisconnect implements Listener<Disconnected> {
    private final GamePlayer player;
    private final PartyService service;

    public LeavePartyOnDisconnect(GamePlayer player, PartyService service) {
        this.player = player;
        this.service = service;
    }

    @Override
    public void on(Disconnected event) {
        service.leave(player);
    }

    @Override
    public Class<Disconnected> event() {
        return Disconnected.class;
    }
}
