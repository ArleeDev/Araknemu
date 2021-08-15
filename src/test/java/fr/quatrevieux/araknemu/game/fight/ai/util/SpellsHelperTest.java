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

package fr.quatrevieux.araknemu.game.fight.ai.util;

import fr.quatrevieux.araknemu.game.fight.ai.AiBaseCase;
import fr.quatrevieux.araknemu.game.fight.ai.simulation.CastSimulation;
import fr.quatrevieux.araknemu.game.fight.ai.simulation.Simulator;
import fr.quatrevieux.araknemu.game.fight.turn.action.Action;
import fr.quatrevieux.araknemu.game.fight.turn.action.cast.Cast;
import fr.quatrevieux.araknemu.game.spell.Spell;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpellsHelperTest extends AiBaseCase {
    @Test
    void available() {
        configureFight(fb -> fb
            .addSelf(b -> b.cell(123))
            .addEnemy(b -> b.cell(125))
        );

        assertArrayEquals(new int [] {17, 3, 6}, helper().available().mapToInt(Spell::id).toArray());

        setAP(3);
        assertArrayEquals(new int [] {17, 6}, helper().available().mapToInt(Spell::id).toArray());

        setAP(1);
        assertArrayEquals(new int [] {}, helper().available().mapToInt(Spell::id).toArray());
    }

    @Test
    void hasAvailable() throws NoSuchFieldException, IllegalAccessException {
        configureFight(fb -> fb
            .addSelf(b -> b.cell(123))
            .addEnemy(b -> b.cell(125))
        );

        assertTrue(helper().hasAvailable());

        setAP(1);
        assertFalse(helper().hasAvailable());

        setAP(6);
        removeSpell(17);
        removeSpell(3);
        removeSpell(6);
        assertFalse(helper().hasAvailable());
    }

    @Test
    void withEffect() {
        configureFight(fb -> fb
            .addSelf(b -> b.cell(123))
            .addEnemy(b -> b.cell(125))
        );

        assertArrayEquals(new int [] {6}, helper().withEffect(265).mapToInt(Spell::id).toArray());
        setAP(1);
        assertArrayEquals(new int [] {6}, helper().withEffect(265).mapToInt(Spell::id).toArray());
    }

    @Test
    void simulate() {
        configureFight(fb -> fb
            .addSelf(b -> b.cell(123))
            .addEnemy(b -> b.cell(125))
        );

        assertTrue(helper().simulate(container.get(Simulator.class)).anyMatch(simulation -> simulation.spell().id() == 3 && simulation.target().id() == 125));
        assertTrue(helper().simulate(container.get(Simulator.class)).anyMatch(simulation -> simulation.spell().id() == 6 && simulation.target().id() == 123));

        // Out of range
        assertFalse(helper().simulate(container.get(Simulator.class)).anyMatch(simulation -> simulation.spell().id() == 3 && simulation.target().id() == 22));
        assertFalse(helper().simulate(container.get(Simulator.class)).anyMatch(simulation -> simulation.spell().id() == 6 && simulation.target().id() == 121));

        // Not enough AP
        setAP(3);
        assertFalse(helper().simulate(container.get(Simulator.class)).anyMatch(simulation -> simulation.spell().id() == 3));
    }

    @Test
    void createAction() {
        configureFight(fb -> fb
            .addSelf(b -> b.cell(123))
            .addEnemy(b -> b.cell(125))
        );

        CastSimulation simulation = helper().simulate(container.get(Simulator.class)).filter(s -> s.spell().id() == 3 && s.target().id() == 125).findFirst().get();
        Action action = helper().createAction(simulation);

        assertInstanceOf(Cast.class, action);
        assertEquals(3, Cast.class.cast(action).spell().id());
        assertEquals(125, Cast.class.cast(action).target().id());
    }

    @Test
    void caster() {
        configureFight(fb -> fb
            .addSelf(b -> b.cell(123))
            .addEnemy(b -> b.cell(125))
        );

        assertNotNull(helper().caster());
        assertSame(helper().caster(), helper().caster());
    }

    private SpellsHelper helper() {
        return ai.helper().spells();
    }
}
