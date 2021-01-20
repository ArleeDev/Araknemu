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
 * Copyright (c) 2017-2021 Vincent Quatrevieux Jean-Alexandre Valentin
 */

package fr.quatrevieux.araknemu.game.fight.castable.effect.handler.invocations;

import java.util.Collections;

import fr.quatrevieux.araknemu.game.fight.Fight;
import fr.quatrevieux.araknemu.game.fight.castable.CastScope;
import fr.quatrevieux.araknemu.game.fight.castable.CastScope.EffectScope;
import fr.quatrevieux.araknemu.game.fight.castable.effect.handler.EffectHandler;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.fight.fighter.monster.InvocationFighter;
import fr.quatrevieux.araknemu.game.fight.fighter.monster.MonsterFighter;
import fr.quatrevieux.araknemu.game.monster.Monster;
import fr.quatrevieux.araknemu.game.monster.MonsterService;
import fr.quatrevieux.araknemu.network.game.out.fight.action.ActionEffect;
import fr.quatrevieux.araknemu.network.game.out.fight.turn.FighterTurnOrder;
import fr.quatrevieux.araknemu.network.game.out.game.AddSprites;

final public class MonsterInvocationHandler implements EffectHandler {
    final private MonsterService monsterService;
    final private Fight fight;

    public MonsterInvocationHandler(MonsterService monsterService, Fight fight) {
        this.monsterService = monsterService;
        this.fight = fight;
    }

    @Override
    public void buff(CastScope cast, EffectScope effect) {
        addMonsterToFight(cast, effect); // sadida lvl 100 puppet hit here
    }

    @Override
    public void handle(CastScope cast, EffectScope effect) {
        addMonsterToFight(cast, effect); // normal invocations
    }

    private void addMonsterToFight(CastScope cast, EffectScope effect) {
        int index = fight.fighters().stream().mapToInt(Fighter::id).min().getAsInt() - 1;
        InvocationFighter invocation = 
            new InvocationFighter(
                new MonsterFighter(
                    index, 
                    monsterService.load(effect.effect().min()).get(effect.effect().max() -1),
                    fight.turnList().currentFighter().team()
                ),
                cast.caster()
            );

        fight.addInvocation(invocation, cast.target());

        fight.send(new ActionEffect(181, cast.caster(), (new AddSprites(Collections.singleton(invocation.sprite()))).toString()));
        fight.send(new ActionEffect(999, cast.caster(), (new FighterTurnOrder(fight.turnList())).toString()));
    }
}
