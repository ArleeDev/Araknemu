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
 * Copyright (c) 2017-2023 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.listener.fight.spectator;

import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.fight.Fight;
import fr.quatrevieux.araknemu.game.fight.FightBaseCase;
import fr.quatrevieux.araknemu.game.fight.map.BattlefieldObject;
import fr.quatrevieux.araknemu.game.fight.spectator.Spectator;
import fr.quatrevieux.araknemu.game.fight.spectator.event.StartWatchFight;
import fr.quatrevieux.araknemu.network.game.out.fight.battlefield.AddZones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

class SendBattlefieldObjectsToSpectatorTest extends FightBaseCase {
    private Fight fight;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        fight = createSimpleFight(container.get(ExplorationMapService.class).load(10340));
    }

    @Test
    void onStartWatchFightWithObjects() throws SQLException {
        BattlefieldObject bo1 = Mockito.mock(BattlefieldObject.class);
        BattlefieldObject bo2 = Mockito.mock(BattlefieldObject.class);
        BattlefieldObject bo3 = Mockito.mock(BattlefieldObject.class);

        Mockito.when(bo1.caster()).thenReturn(fight.fighters().get(0));
        Mockito.when(bo2.caster()).thenReturn(fight.fighters().get(1));
        Mockito.when(bo3.caster()).thenReturn(fight.fighters().get(0));

        Mockito.when(bo1.cell()).thenReturn(fight.map().get(123));
        Mockito.when(bo2.cell()).thenReturn(fight.map().get(220));
        Mockito.when(bo3.cell()).thenReturn(fight.map().get(150));

        Mockito.when(bo1.size()).thenReturn(2);
        Mockito.when(bo2.size()).thenReturn(1);
        Mockito.when(bo3.size()).thenReturn(3);

        Mockito.when(bo1.color()).thenReturn(1);
        Mockito.when(bo2.color()).thenReturn(2);
        Mockito.when(bo3.color()).thenReturn(3);

        fight.nextState();

        fight.map().addObject(bo1);
        fight.map().addObject(bo2);
        fight.map().addObject(bo3);

        SendBattlefieldObjectsToSpectator listener = new SendBattlefieldObjectsToSpectator(new Spectator(gamePlayer(), fight));
        requestStack.clear();

        listener.on(new StartWatchFight());

        requestStack.assertAll(
            new AddZones(new AddZones.Zone[] {
                new AddZones.Zone(123, 2, 1),
                new AddZones.Zone(220, 1, 2),
                new AddZones.Zone(150, 3, 3),
            })
        );
    }

    @Test
    void onStartWatchFightWithoutObjects() throws SQLException {
        fight.nextState();

        SendBattlefieldObjectsToSpectator listener = new SendBattlefieldObjectsToSpectator(new Spectator(gamePlayer(), fight));
        requestStack.clear();

        listener.on(new StartWatchFight());

        requestStack.assertEmpty();
    }
}