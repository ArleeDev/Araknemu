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

package fr.quatrevieux.araknemu.game.exploration.map.cell.trigger.action.teleport;

import fr.quatrevieux.araknemu.data.value.Position;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.exploration.ExplorationPlayer;
import fr.quatrevieux.araknemu.game.exploration.interaction.action.ActionType;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.network.game.out.game.MapData;
import fr.quatrevieux.araknemu.network.game.out.game.action.GameActionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeleportTest extends GameBaseCase {
    private Teleport teleport;
    private ExplorationMapService service;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushMaps().pushSubAreas().pushAreas();
        service = container.get(ExplorationMapService.class);
    }

    @Test
    void performOnSameMap() throws Exception {
        teleport = new Teleport(service, 123, new Position(10300, 321));
        teleport.perform(explorationPlayer());

        assertEquals(
            new Position(10300, 321),
            explorationPlayer().position()
        );

        assertFalse(explorationPlayer().interactions().busy());

        requestStack.assertLast("GA;4;"+explorationPlayer().id()+";"+explorationPlayer().id()+",321");
    }

    @Test
    void performOnSameMapInvalidCell() throws Exception {
        teleport = new Teleport(service, 123, new Position(10300, 1000));
        assertThrows(IllegalStateException.class, () -> teleport.perform(explorationPlayer()));

        assertEquals(new Position(10300, 279), explorationPlayer().position());
        assertFalse(explorationPlayer().interactions().busy());
    }

    @Test
    void teleportOnOtherMap() throws Exception {
        ExplorationPlayer player = explorationPlayer();
        requestStack.clear();
        teleport = new Teleport(service, 123, new Position(10540, 321));
        teleport.perform(player);

        assertEquals(new Position(10540, 321), player.position());

        requestStack.assertAll(
            new MapData(player.map()),
            new GameActionResponse("", ActionType.CHANGE_MAP, player.id(), "")
        );
    }

    @Test
    void teleportOnOtherMapInvalidCell() throws Exception {
        ExplorationPlayer player = explorationPlayer();
        requestStack.clear();
        teleport = new Teleport(service, 123, new Position(10540, 1000));
        assertThrows(IllegalStateException.class, () -> teleport.perform(player));

        assertEquals(new Position(10300, 279), player.position());
        assertFalse(explorationPlayer().interactions().busy());
    }
}
