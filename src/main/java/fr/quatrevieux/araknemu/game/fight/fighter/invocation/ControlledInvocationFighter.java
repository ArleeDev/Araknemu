/* This file is part of Araknemu.
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

package fr.quatrevieux.araknemu.game.fight.fighter.invocation;/*

/**
 * Fighter for a player
 */

import fr.quatrevieux.araknemu.game.fight.FighterSprite;
import fr.quatrevieux.araknemu.game.fight.fighter.monster.ControlledMonsterFighterSprite;
import fr.quatrevieux.araknemu.game.fight.fighter.operation.FighterOperation;
import fr.quatrevieux.araknemu.game.fight.fighter.player.PlayerFighter;
import fr.quatrevieux.araknemu.game.fight.team.FightTeam;
import fr.quatrevieux.araknemu.game.monster.Monster;
import fr.quatrevieux.araknemu.game.player.CharacterProperties;
import fr.quatrevieux.araknemu.game.player.PlayerSessionScope;
import fr.quatrevieux.araknemu.network.game.GameSession;

public final class ControlledInvocationFighter extends InvocationFighter implements PlayerSessionScope {
    private final PlayerFighter controller;

    @SuppressWarnings({"assignment", "argument"})
    public ControlledInvocationFighter(PlayerFighter controller, int id, Monster monster, FightTeam team) {
        super(id, monster, team, controller);
        this.controller = controller;
    }

    @Override
    public FighterSprite sprite()
    {
        return new ControlledMonsterFighterSprite(this,monster());
    }

    /**
     * Get the properties of the current character session
     */
    @Override
    public CharacterProperties properties() {
        return controller.properties();
    }

    /**
     * Register the scope to the session
     */
    @Override
    public void register(GameSession session) {
        controller.register(session);
    }

    /**
     * Remove the scope from the session
     */
    @Override
    public void unregister(GameSession session) {
        controller.unregister(session);
    }

    /**
     * Send the packet
     *
     * @param packet
     */
    @Override
    public void send(Object packet) {
        controller.send(packet);
    }

    @Override
    public <O extends FighterOperation> O apply(O operation) {
        operation.onControlledInvocation(this);

        return operation;
    }
}
