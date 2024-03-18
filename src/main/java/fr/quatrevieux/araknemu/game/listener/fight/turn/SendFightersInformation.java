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

package fr.quatrevieux.araknemu.game.listener.fight.turn;

import fr.quatrevieux.araknemu.core.event.Listener;
import fr.quatrevieux.araknemu.game.fight.Fight;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.fight.fighter.invocation.ControlledInvocationFighter;
import fr.quatrevieux.araknemu.game.fight.fighter.operation.FighterOperation;
import fr.quatrevieux.araknemu.game.fight.fighter.player.PlayerFighter;
import fr.quatrevieux.araknemu.game.fight.turn.event.NextTurnInitiated;
import fr.quatrevieux.araknemu.network.game.out.account.Stats;
import fr.quatrevieux.araknemu.network.game.out.fight.turn.TurnMiddle;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Send the fighters information between two turns
 */
public final class SendFightersInformation implements Listener<NextTurnInitiated> {
    private static final SendStats sendStats = new SendStats();
    private final Fight fight;

    public SendFightersInformation(Fight fight) {
        this.fight = fight;
    }

    @Override
    public void on(NextTurnInitiated event) {
        fight.send(new TurnMiddle(fight.fighters()));
        ControlledInvocationFighter late = null;

        final List<Fighter> temp = fight.fighters().stream().collect(Collectors.toList());

        if (fight.getTurnList() != null && fight.turnList().nextFighter() instanceof ControlledInvocationFighter) {
            late = (ControlledInvocationFighter) fight.turnList().nextFighter();
            temp.remove(late);
        }

        temp.forEach(fighter -> fighter.apply(sendStats)); // Life of the current fighter can only be synchronized by this packet

        if (late != null) { //lateinits the next controlledsummon to the summoner's UI
            late.send(new Stats(late));
        }
    }

    @Override
    public Class<NextTurnInitiated> event() {
        return NextTurnInitiated.class;
    }

    private static final class SendStats implements FighterOperation {
        @Override
        public void onPlayer(PlayerFighter fighter) {
            fighter.send(new Stats(fighter.properties()));
        }
    }
}
