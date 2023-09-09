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

package fr.quatrevieux.araknemu.game.fight.ai.simulation.effect;

import fr.arakne.utils.value.Interval;
import fr.quatrevieux.araknemu.game.fight.ai.AI;
import fr.quatrevieux.araknemu.game.fight.ai.simulation.CastSimulation;
import fr.quatrevieux.araknemu.game.fight.ai.simulation.SpellScore;
import fr.quatrevieux.araknemu.game.fight.castable.CastScope;
import fr.quatrevieux.araknemu.game.fight.fighter.FighterData;
import fr.quatrevieux.araknemu.game.fight.map.BattlefieldCell;
import fr.quatrevieux.araknemu.game.spell.effect.SpellEffect;
import fr.quatrevieux.araknemu.game.world.creature.characteristics.Characteristics;

/**
 * Simulator for fixed damage effect on the caster
 */
public final class FixedCasterDamage implements EffectSimulator {
    @Override
    public void simulate(CastSimulation simulation, AI ai, CastScope.EffectScope<? extends FighterData, ? extends BattlefieldCell> effect) {
        final SpellEffect spellEffect = effect.effect();
        final int min = spellEffect.min();
        final int max = Math.max(spellEffect.max(), min);
        final int duration = spellEffect.duration();

        if (duration == 0) {
            simulation.addDamage(new Interval(min, max), ai.fighter());
        } else {
            // Limit duration to 10
            simulation.addPoison(new Interval(min, max), duration < 1 || duration > 10 ? 10 : duration, ai.fighter());
        }
    }

    @Override
    public void score(SpellScore score, SpellEffect effect, Characteristics characteristics) {
        score.setSuicide(effect.min() >= 1000);
    }
}
