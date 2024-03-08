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
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.characteristic.event.CharacteristicsChanged;

public class UpdatePartyOnStatsChanged implements Listener<CharacteristicsChanged> {
    private final GamePlayer trigger;
    private final PartyService service;

    public UpdatePartyOnStatsChanged(GamePlayer trigger, PartyService service) {
        this.trigger = trigger;
        this.service = service;
    }

    /**
     * Handle the event
     *
     * @param event The event instance
     */
    @Override
    public void on(CharacteristicsChanged event) {
        service.updatePartyUI(trigger);
    }

    /**
     * Get the event class
     */
    @Override
    public Class<CharacteristicsChanged> event() {
        return CharacteristicsChanged.class;
    }
}
