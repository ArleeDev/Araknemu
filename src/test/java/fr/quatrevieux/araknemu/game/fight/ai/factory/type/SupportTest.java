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
 * Copyright (c) 2017-2021 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.fight.ai.factory.type;

import fr.quatrevieux.araknemu.game.fight.ai.AiBaseCase;
import fr.quatrevieux.araknemu.game.fight.ai.action.builder.GeneratorBuilder;
import fr.quatrevieux.araknemu.game.fight.ai.simulation.Simulator;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupportTest extends AiBaseCase {
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        actionFactory = new Support(container.get(Simulator.class));
        dataSet.pushFunctionalSpells();
    }

    @Test
    void shouldBoostAlliesFirst() throws NoSuchFieldException, IllegalAccessException {
        configureFight(b -> b
            .addSelf(fb -> fb.cell(210).spell(27, 5))
            .addAlly(fb -> fb.cell(198))
            .addEnemy(fb -> fb.cell(165))
        );

        setMP(0);

        removeSpell(6);

        assertCast(27, 183);
        assertInCastEffectArea(198);
    }

    @Test
    void shouldAttackIfCantBoost() throws NoSuchFieldException, IllegalAccessException {
        configureFight(b -> b
            .addSelf(fb -> fb.cell(210))
            .addAlly(fb -> fb.cell(198))
            .addEnemy(fb -> fb.cell(165))
        );

        removeSpell(6);

        assertCast(3, 165);
    }

    @Test
    void shouldMoveNearAlliesIfCantAttack() throws NoSuchFieldException, IllegalAccessException {
        configureFight(b -> b
            .addSelf(fb -> fb.cell(210))
            .addAlly(fb -> fb.cell(198))
            .addEnemy(fb -> fb.cell(165))
        );

        setAP(2);
        removeSpell(6);

        assertEquals(5, distance(getAlly(1)));

        generateAndPerformMove();
        assertEquals(168, fighter.cell().id());
        assertEquals(2, distance(getAlly(1)));
    }

    @Test
    void shouldDoNothingOtherwise() throws NoSuchFieldException, IllegalAccessException {
        configureFight(b -> b
            .addSelf(fb -> fb.cell(210))
            .addAlly(fb -> fb.cell(196))
            .addEnemy(fb -> fb.cell(165))
        );

        setAP(2);
        removeSpell(6);

        assertDotNotGenerateAction();
    }

    @Test
    void shouldBoostSelfFirstWhenAlone() {
        configureFight(b -> b
            .addSelf(fb -> fb.cell(210).spell(27, 5))
            .addEnemy(fb -> fb.cell(165))
        );

        setMP(0);

        assertCast(6, 210);
    }

    @Test
    void shouldAttackIfCantBoostSelfWhenAlone() throws NoSuchFieldException, IllegalAccessException {
        configureFight(b -> b
            .addSelf(fb -> fb.cell(210))
            .addEnemy(fb -> fb.cell(165))
        );

        removeSpell(6);

        assertCast(3, 165);
    }

    @Test
    void shouldMoveFarEnemiesIfCantAttackButWithEnemyInRangeWhenAlone() throws NoSuchFieldException, IllegalAccessException {
        configureFight(b -> b
            .addSelf(fb -> fb.cell(210))
            .addEnemy(fb -> fb.cell(165))
        );

        setAP(2);
        removeSpell(6);

        assertEquals(3, distance(getEnemy(0)));

        generateAndPerformMove();
        assertEquals(168, fighter.cell().id());
        assertEquals(6, distance(getEnemy(0)));
    }

    @Test
    void shouldDoNothingOtherwiseWhenAlone() throws NoSuchFieldException, IllegalAccessException {
        configureFight(b -> b
            .addSelf(fb -> fb.cell(210))
            .addEnemy(fb -> fb.cell(165))
        );

        setAP(2);
        setMP(0);
        removeSpell(6);

        assertDotNotGenerateAction();
    }

    private int distance(Fighter other) {
        return fighter.cell().coordinate().distance(other.cell());
    }
}