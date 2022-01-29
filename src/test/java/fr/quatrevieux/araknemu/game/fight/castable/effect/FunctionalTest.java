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

package fr.quatrevieux.araknemu.game.fight.castable.effect;

import fr.quatrevieux.araknemu.data.constant.Characteristic;
import fr.quatrevieux.araknemu.game.fight.Fight;
import fr.quatrevieux.araknemu.game.fight.FightBaseCase;
import fr.quatrevieux.araknemu.game.fight.castable.effect.buff.Buff;
import fr.quatrevieux.araknemu.game.fight.castable.spell.SpellConstraintsValidator;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.fight.fighter.player.PlayerFighter;
import fr.quatrevieux.araknemu.game.fight.map.FightCell;
import fr.quatrevieux.araknemu.game.fight.module.CommonEffectsModule;
import fr.quatrevieux.araknemu.game.fight.module.IndirectSpellApplyEffectsModule;
import fr.quatrevieux.araknemu.game.fight.state.PlacementState;
import fr.quatrevieux.araknemu.game.fight.turn.FightTurn;
import fr.quatrevieux.araknemu.game.fight.turn.action.cast.Cast;
import fr.quatrevieux.araknemu.game.fight.turn.action.cast.CastSuccess;
import fr.quatrevieux.araknemu.game.fight.turn.action.util.CriticalityStrategy;
import fr.quatrevieux.araknemu.game.spell.Spell;
import fr.quatrevieux.araknemu.game.spell.SpellService;
import fr.quatrevieux.araknemu.network.game.out.fight.action.ActionEffect;
import fr.quatrevieux.araknemu.network.game.out.fight.action.FightAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionalTest extends FightBaseCase {
    private SpellService service;
    private Fight fight;

    private PlayerFighter fighter1;
    private PlayerFighter fighter2;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        service = container.get(SpellService.class);

        dataSet.pushFunctionalSpells();

        fight = createFight();
        fight.register(new CommonEffectsModule(fight));
        fight.register(new IndirectSpellApplyEffectsModule(fight, container.get(SpellService.class)));

        fighter1 = player.fighter();
        fighter2 = other.fighter();

        fighter1.move(fight.map().get(185));
        fighter2.move(fight.map().get(170));

        fight.state(PlacementState.class).startFight();
        fight.turnList().start();

        requestStack.clear();
    }

    @Test
    void poisonSpell() {
        castNormal(181, fighter1.cell()); // Tremblement

        Optional<Buff> buff1 = fighter1.buffs().stream().filter(buff -> buff.effect().effect() == 99).findFirst();
        Optional<Buff> buff2 = fighter2.buffs().stream().filter(buff -> buff.effect().effect() == 99).findFirst();

        assertTrue(buff1.isPresent());
        assertTrue(buff2.isPresent());

        assertEquals(5, buff1.get().remainingTurns());
        assertEquals(4, buff2.get().remainingTurns());

        assertEquals(fighter1.life().current(), fighter1.life().max());
        assertEquals(fighter2.life().current(), fighter2.life().max());

        fighter1.turn().stop();

        assertEquals(12, fighter2.life().max() - fighter2.life().current());
        requestStack.assertOne(ActionEffect.alterLifePoints(fighter1, fighter2, -12));

        assertEquals(4, buff1.get().remainingTurns());
        assertEquals(4, buff2.get().remainingTurns());

        fighter2.turn().stop();

        requestStack.assertOne(ActionEffect.alterLifePoints(fighter1, fighter1, -12));
        assertEquals(4, buff1.get().remainingTurns());
        assertEquals(3, buff2.get().remainingTurns());
        fighter1.turn().stop();

        requestStack.assertOne(ActionEffect.alterLifePoints(fighter1, fighter2, -12));
        assertEquals(3, buff1.get().remainingTurns());
        assertEquals(3, buff2.get().remainingTurns());
    }

    @Test
    void skipNextTurn() {
        castNormal(1630, fighter2.cell());

        Optional<Buff> found = fighter2.buffs().stream().filter(buff -> buff.effect().effect() == 140).findFirst();

        assertTrue(found.isPresent());
        assertEquals(140, found.get().effect().effect());
        assertEquals(1, found.get().remainingTurns());

        requestStack.assertOne(ActionEffect.skipNextTurn(fighter1, fighter2));

        fighter1.turn().stop();
        assertSame(fighter1, fight.turnList().currentFighter());
        fighter1.turn().stop();

        assertSame(fighter2, fight.turnList().currentFighter());
    }

    @Test
    void skipNextTurnSelfCast() {
        // #61 Skip next turn not working on self-buff
        castNormal(1630, fighter1.cell());

        fighter1.turn().stop();
        assertSame(fighter2, fight.turnList().currentFighter());
        fighter2.turn().stop();

        assertSame(fighter2, fight.turnList().currentFighter());
    }

    @Test
    void probableEffectSpell() {
        Spell spell = castNormal(109, fighter2.cell()); // Bluff

        int damage = fighter2.life().max() - fighter2.life().current();

        assertBetween(1, 50, damage);

        requestStack.assertAll(
            "GAS1",
            new FightAction(new CastSuccess(fighter1, spell, fighter2.cell(), false)),
            ActionEffect.usedActionPoints(fighter1, 4),
            ActionEffect.alterLifePoints(fighter1, fighter2, -damage),
            "GAF0|1"
        );
    }

    @Test
    void returnSpell() {
        castNormal(4, fighter1.cell()); // Return spell
        fighter1.turn().stop();

        castNormal(109, fighter1.cell()); // Bluff

        int damage = fighter2.life().max() - fighter2.life().current();

        assertBetween(1, 50, damage);

        requestStack.assertOne(ActionEffect.alterLifePoints(fighter2, fighter2, -damage));
        requestStack.assertOne(ActionEffect.returnSpell(fighter1, true));
    }

    @Test
    void pointsChange() {
        castNormal(115, fighter1.cell()); // Odorat

        Optional<Buff> addAp = fighter1.buffs().stream().filter(buff -> buff.effect().effect() == 111).findFirst();
        Optional<Buff> remAp = fighter1.buffs().stream().filter(buff -> buff.effect().effect() == 168).findFirst();
        Optional<Buff> addMp = fighter1.buffs().stream().filter(buff -> buff.effect().effect() == 128).findFirst();
        Optional<Buff> remMp = fighter1.buffs().stream().filter(buff -> buff.effect().effect() == 169).findFirst();

        assertTrue(addAp.isPresent());
        assertTrue(remAp.isPresent());
        assertTrue(addMp.isPresent());
        assertTrue(remMp.isPresent());

        requestStack.assertOne(ActionEffect.buff(addAp.get(), addAp.get().effect().min()));
        requestStack.assertOne(ActionEffect.buff(remAp.get(), -remAp.get().effect().min()));
        requestStack.assertOne(ActionEffect.buff(addMp.get(), addMp.get().effect().min()));
        requestStack.assertOne(ActionEffect.buff(remMp.get(), -remMp.get().effect().min()));

        assertBetween(2, 5, addAp.get().effect().min());
        assertBetween(1, 4, remAp.get().effect().min());
        assertBetween(2, 5, addMp.get().effect().min());
        assertBetween(1, 4, remMp.get().effect().min());

        int apChange = addAp.get().effect().min() - remAp.get().effect().min();
        int mpChange = addMp.get().effect().min() - remMp.get().effect().min();

        assertEquals(6 + apChange, fighter1.characteristics().get(Characteristic.ACTION_POINT));
        assertEquals(3 + mpChange, fighter1.characteristics().get(Characteristic.MOVEMENT_POINT));

        passTurns(4);

        assertEquals(6, fighter1.characteristics().get(Characteristic.ACTION_POINT));
        assertEquals(3, fighter1.characteristics().get(Characteristic.MOVEMENT_POINT));
    }

    @Test
    void addCharacteristic() {
        castNormal(42, fighter1.cell()); // Chance

        Optional<Buff> addLuck = fighter1.buffs().stream().filter(buff -> buff.effect().effect() == 123).findFirst();

        assertTrue(addLuck.isPresent());
        assertBetween(51, 60, addLuck.get().effect().min());
        assertEquals(addLuck.get().effect().min(), fighter1.characteristics().get(Characteristic.LUCK));
        requestStack.assertOne(ActionEffect.buff(addLuck.get(), addLuck.get().effect().min()));

        passTurns(5);

        assertEquals(0, fighter2.characteristics().get(Characteristic.LUCK));
    }

    @Test
    void removeCharacteristic() {
        castNormal(468, fighter2.cell()); // Flêche d'huile

        Optional<Buff> removeIntel = fighter2.buffs().stream().filter(buff -> buff.effect().effect() == 155).findFirst();

        assertTrue(removeIntel.isPresent());
        assertEquals(400, removeIntel.get().effect().min());
        assertEquals(-400, fighter2.characteristics().get(Characteristic.INTELLIGENCE));
        requestStack.assertOne(ActionEffect.buff(removeIntel.get(), -removeIntel.get().effect().min()));

        passTurns(5);

        assertEquals(0, fighter2.characteristics().get(Characteristic.INTELLIGENCE));
    }

    @Test
    void armor() {
        castNormal(1, fighter1.cell()); // Armure Incandescente
        fighter1.turn().stop();

        castNormal(3, fighter1.cell()); // Attaque naturelle

        requestStack.assertOne(ActionEffect.reducedDamage(fighter1, 27));
        requestStack.assertOne(ActionEffect.alterLifePoints(fighter2, fighter1, 0));

        fighter2.turn().stop();
        fighter1.turn().stop();

        castNormal(2, fighter1.cell()); // Aveuglement

        int damage = fighter1.life().max() - fighter1.life().current();

        assertBetween(3, 7, damage);
        requestStack.assertOne(ActionEffect.alterLifePoints(fighter2, fighter1, -damage));

        castNormal(181, fighter2.cell()); // Tremblement
        requestStack.clear();
        fighter2.turn().stop();

        requestStack.assertOne(ActionEffect.alterLifePoints(fighter2, fighter1, -5));
        requestStack.assertNotContainsPrefix("GA;105");
    }

    @Test
    void healOrMultiplyDamage() {
        int healCount = 0;

        for (int i = 0; i < 15; ++i) {
            fighter1.life().alter(fighter1, fighter1.life().max() - fighter1.life().current() - 15); // Fighter1 has -15 LP
            int lifeBefore = fighter1.life().current();

            castNormal(103, fighter1.cell()); // Chance d'Ecaflip
            fighter1.turn().stop();

            castNormal(3, fighter1.cell()); // Attaque naturelle

            int lifeChange = fighter1.life().current() - lifeBefore;

            requestStack.assertOne(ActionEffect.alterLifePoints(fighter2, fighter1, lifeChange));

            if (lifeChange < 0) {
                assertBetween(14, 22, -lifeChange);
            } else {
                assertBetween(7, 11, lifeChange);
                ++healCount;
            }

            fighter2.turn().stop();
            passTurns(3);
        }

        assertTrue(healCount > 1);
    }

    @Test
    void state() {
        castNormal(686, fighter1.cell()); // Picole

        requestStack.assertOne(ActionEffect.addState(fighter1, 1));
        assertTrue(fighter1.states().has(1));

        passTurns(1);

        castNormal(699, fighter1.cell()); // Lait de bambou

        requestStack.assertOne(ActionEffect.removeState(fighter1, 1));
        assertFalse(fighter1.states().has(1));
    }

    @Test
    void stateExpiration() {
        castNormal(686, fighter1.cell()); // Picole

        requestStack.assertOne(ActionEffect.addState(fighter1, 1));
        assertTrue(fighter1.states().has(1));

        passTurns(10); // 9 + 1 for current turn

        requestStack.assertOne(ActionEffect.removeState(fighter1, 1));
        assertFalse(fighter1.states().has(1));
    }

    @Test
    void dispelBuffs() {
        castNormal(42, fighter1.cell()); // Chance;

        passTurns(1);

        castCritical(49, fighter1.cell()); // Pelle Fantomatique

        requestStack.assertOne(ActionEffect.dispelBuffs(fighter1, fighter1));
        assertIterableEquals(Collections.EMPTY_LIST, fighter1.buffs());
    }

    @Test
    void heal() {
        fighter1.life().alter(fighter1, -50);

        castNormal(121, fighter1.cell()); // Mot curatif

        int heal = 50 + fighter1.life().current() - fighter1.life().max();
        assertBetween(32, 47, heal);

        requestStack.assertOne(ActionEffect.alterLifePoints(fighter1, fighter1, heal));
    }

    @Test
    void healAsBuff() {
        fighter1.life().alter(fighter1, -50);

        castNormal(131, fighter1.cell()); // Mot de Régénération

        int heal = 50 + fighter1.life().current() - fighter1.life().max();
        assertEquals(0, heal);

        passTurns(1);

        heal = 50 + fighter1.life().current() - fighter1.life().max();
        assertBetween(2, 10, heal);

        requestStack.assertOne(ActionEffect.alterLifePoints(fighter1, fighter1, heal));
    }

    @Test
    void healOnDamage() {
        castNormal(1556, fighter1.cell()); // Fourberie

        fighter1.life().alter(fighter1, -50);

        int heal = 50 + fighter1.life().current() - fighter1.life().max();
        assertEquals(37, heal);

        requestStack.assertOne(ActionEffect.alterLifePoints(fighter1, fighter1, heal));
    }

    @Test
    void avoidDamageByMovingBack() {
        fighter1.move(fight.map().get(150));
        fighter2.move(fight.map().get(165));

        castNormal(444, fighter1.cell()); // Dérobade
        fighter1.turn().stop();

        castNormal(183, fighter1.cell()); // Simple attack

        assertEquals(fighter1.life().max(), fighter1.life().current());
        assertEquals(135, fighter1.cell().id());
        requestStack.assertOne(ActionEffect.slide(fighter2, fighter1, fight.map().get(135)));
    }

    @Test
    void moveBack() {
        fighter1.move(fight.map().get(150));
        fighter2.move(fight.map().get(165));

        castNormal(128, fighter2.cell()); // Mot de Frayeur

        assertEquals(180, fighter2.cell().id());
        requestStack.assertOne(ActionEffect.slide(fighter1, fighter2, fight.map().get(180)));
    }

    @Test
    void moveToTargetCell() {
        fighter1.move(fight.map().get(291));
        fighter2.move(fight.map().get(277));

        castNormal(67, fight.map().get(235)); // Peur

        assertEquals(235, fighter2.cell().id());
        requestStack.assertOne(ActionEffect.slide(fighter1, fighter2, fight.map().get(235)));
    }

    @Test
    void moveFront() {
        fighter1.move(fight.map().get(305));
        fighter2.move(fight.map().get(193));

        castNormal(434, fight.map().get(193)); // Attirance

        assertEquals(277, fighter2.cell().id());
        requestStack.assertOne(ActionEffect.slide(fighter1, fighter2, fight.map().get(277)));
    }

    @Test
    void switchPosition() {
        fighter1.move(fight.map().get(305));
        fighter2.move(fight.map().get(193));

        castNormal(445, fight.map().get(193)); // Coopération

        assertEquals(193, fighter1.cell().id());
        assertEquals(305, fighter2.cell().id());

        requestStack.assertOne(ActionEffect.teleport(fighter1, fighter1, fight.map().get(193)));
        requestStack.assertOne(ActionEffect.teleport(fighter1, fighter2, fight.map().get(305)));
    }

    @Test
    void switchOnAttack() {
        fight.cancel(true);

        fight = fightBuilder()
            .addSelf(fb -> fb.cell(165))
            .addAlly(fb -> fb.player(other).cell(150))
            .addEnemy(fb -> fb.cell(192))
            .build(true)
        ;

        fight.state(PlacementState.class).startFight();
        fight.turnList().start();

        fighter1 = player.fighter();
        fighter2 = other.fighter();

        requestStack.clear();

        castNormal(440, fighter2.cell()); // Sacrifice
        fighter1.turn().stop();

        castNormal(183, fighter2.cell()); // Simple attack

        assertEquals(fighter2.life().max(), fighter2.life().current());
        assertBetween(15, 25, fighter1.life().max() - fighter1.life().current());

        assertEquals(150, fighter1.cell().id());
        assertEquals(165, fighter2.cell().id());

        requestStack.assertOne(ActionEffect.teleport(fighter1, fighter2, fight.map().get(165)));
        requestStack.assertOne(ActionEffect.teleport(fighter1, fighter1, fight.map().get(150)));
    }

    @Test
    void switchOnAttackWithChaining() {
        fight.cancel(true);

        fight = fightBuilder()
            .addSelf(fb -> fb.cell(328).charac(Characteristic.LUCK, 100))
            .addAlly(fb -> fb.cell(271).charac(Characteristic.LUCK, 50))
            .addAlly(fb -> fb.cell(211))
            .addEnemy(fb -> fb.cell(325))
            .build(true)
        ;

        List<Fighter> fighters = fight.fighters();

        fight.state(PlacementState.class).startFight();
        fight.turnList().start();

        castNormal(440, fight.map().get(271)); // Sacrifice
        fighters.get(0).turn().stop();
        fighters.get(3).turn().stop();

        castNormal(440, fight.map().get(211)); // Sacrifice
        fighters.get(1).turn().stop();
        fighters.get(2).turn().stop();
        fighters.get(0).turn().stop();

        castNormal(183, fight.map().get(211)); // Simple attack

        assertEquals(fighters.get(2).life().max(), fighters.get(2).life().current());
        assertEquals(fighters.get(1).life().max(), fighters.get(1).life().current());
        assertBetween(10, 17, fighters.get(0).life().max() - fighters.get(0).life().current());

        assertEquals(211, fighters.get(0).cell().id());
        assertEquals(328, fighters.get(1).cell().id());
        assertEquals(271, fighters.get(2).cell().id());

        requestStack.assertOne(ActionEffect.teleport(fighters.get(1), fighters.get(2), fight.map().get(271)));
        requestStack.assertOne(ActionEffect.teleport(fighters.get(1), fighters.get(1), fight.map().get(211)));
        requestStack.assertOne(ActionEffect.teleport(fighters.get(0), fighters.get(1), fight.map().get(328)));
        requestStack.assertOne(ActionEffect.teleport(fighters.get(0), fighters.get(0), fight.map().get(211)));
    }

    @Test
    void switchOnAttackWithSpellReturn() {
        fight.cancel(true);

        fight = fightBuilder()
            .addSelf(fb -> fb.cell(328).charac(Characteristic.LUCK, 100).charac(Characteristic.ACTION_POINT, 1))
            .addAlly(fb -> fb.cell(271).charac(Characteristic.LUCK, 50))
            .addEnemy(fb -> fb.cell(325))
            .build(true)
        ;

        List<Fighter> fighters = fight.fighters();

        fight.state(PlacementState.class).startFight();
        fight.turnList().start();

        castNormal(440, fight.map().get(271)); // Sacrifice
        castNormal(4, fight.map().get(328)); // Renvoi de Sort
        fighters.get(0).turn().stop();

        castNormal(183, fight.map().get(271)); // Simple attack

        assertEquals(fighters.get(0).life().max(), fighters.get(0).life().current());
        assertEquals(fighters.get(1).life().max(), fighters.get(1).life().current());
        assertBetween(10, 17, fighters.get(2).life().max() - fighters.get(2).life().current());

        assertEquals(271, fighters.get(0).cell().id());
        assertEquals(328, fighters.get(1).cell().id());

        requestStack.assertOne(ActionEffect.teleport(fighters.get(0), fighters.get(1), fight.map().get(328)));
        requestStack.assertOne(ActionEffect.teleport(fighters.get(0), fighters.get(0), fight.map().get(271)));
        requestStack.assertOne(ActionEffect.returnSpell(fighters.get(0), true));
    }

    /**
     * See: https://github.com/Arakne/Araknemu/pull/206#issuecomment-984841521
     */
    @Test
    void switchThenAttack() {
        fight.cancel(true);

        fight = fightBuilder()
            .addSelf(fb -> fb.cell(185))
            .addEnemy(fb -> fb.player(other).cell(170).maxLife(150).currentLife(150))
            .build(true)
        ;

        fighter1 = player.fighter();
        fighter2 = other.fighter();

        fight.state(PlacementState.class).startFight();
        fight.turnList().start();

        castNormal(577, fighter2.cell()); // Bambou Musical

        assertEquals(170, fighter1.cell().id());
        assertEquals(185, fighter2.cell().id());

        assertEquals(fighter1.life().max(), fighter1.life().current());
        assertBetween(71, 100, fighter2.life().max() - fighter2.life().current());

        requestStack.assertOne(ActionEffect.teleport(fighter1, fighter2, fight.map().get(185)));
        requestStack.assertOne(ActionEffect.teleport(fighter1, fighter1, fight.map().get(170)));
    }

    @Test
    void reflectDamageSpell() {
        castNormal(82, fighter1.cell()); // Contre
        fighter1.turn().stop();

        castNormal(183, fighter1.cell()); // Simple attack
        assertEquals(7, fighter2.life().max() - fighter2.life().current());

        requestStack.assertOne(ActionEffect.reflectedDamage(fighter1, 7));
        requestStack.assertOne(ActionEffect.alterLifePoints(fighter1, fighter2, -7));
    }

    @Test
    void reflectDamageCharacteristic() {
        fighter2.characteristics().alter(Characteristic.COUNTER_DAMAGE, 5);

        castNormal(183, fighter2.cell()); // Simple attack
        assertEquals(5, fighter1.life().max() - fighter1.life().current());

        requestStack.assertOne(ActionEffect.reflectedDamage(fighter2, 5));
        requestStack.assertOne(ActionEffect.alterLifePoints(fighter2, fighter1, -5));
    }

    @Test
    void switchOnAttackAndReflectDamage() {
        fight.cancel(true);

        fight = fightBuilder()
            .addSelf(fb -> fb.cell(328).charac(Characteristic.LUCK, 100).charac(Characteristic.COUNTER_DAMAGE, 1))
            .addAlly(fb -> fb.cell(271).charac(Characteristic.LUCK, 50))
            .addAlly(fb -> fb.cell(256))
            .addEnemy(fb -> fb.cell(325))
            .build(true)
        ;

        List<Fighter> fighters = fight.fighters();

        fight.state(PlacementState.class).startFight();
        fight.turnList().start();

        castNormal(440, fight.map().get(271)); // Sacrifice
        fighters.get(0).turn().stop();
        fighters.get(3).turn().stop();

        castNormal(183, fight.map().get(256)); // Simple attack

        assertTrue(fighters.get(1).life().isFull());
        assertTrue(fighters.get(2).life().isFull());

        int damage = fighters.get(0).life().max() - fighters.get(0).life().current();
        assertBetween(16, 18, damage);

        assertEquals(256, fighters.get(1).cell().id());
        assertEquals(328, fighters.get(2).cell().id());
        assertEquals(271, fighters.get(0).cell().id());

        // Damage reflected to himself
        requestStack.assertOne(ActionEffect.reflectedDamage(fighters.get(0), 1));
        requestStack.assertOne(ActionEffect.alterLifePoints(fighters.get(1), fighters.get(0), -damage + 1));
        requestStack.assertOne(ActionEffect.alterLifePoints(fighters.get(0), fighters.get(0), -1));

        // Position switches
        requestStack.assertOne(ActionEffect.teleport(fighters.get(0), fighters.get(2), fight.map().get(328)));
        requestStack.assertOne(ActionEffect.teleport(fighters.get(0), fighters.get(0), fight.map().get(256)));
        requestStack.assertOne(ActionEffect.teleport(fighters.get(0), fighters.get(1), fight.map().get(256)));
        requestStack.assertOne(ActionEffect.teleport(fighters.get(0), fighters.get(0), fight.map().get(271)));
    }

    @Test
    void actionPointLost() {
        fighter2.move(fight.map().get(211));
        fighter1.characteristics().alter(Characteristic.WISDOM, 100);

        castNormal(81, fighter2.cell()); // Ralentissement

        Buff buff = fighter2.buffs().stream().filter(b -> b.effect().effect() == 101).findFirst().get();
        assertEquals(4, fighter2.characteristics().get(Characteristic.ACTION_POINT));
        requestStack.assertOne(ActionEffect.buff(buff, -2));

        fighter1.turn().stop();
        assertEquals(4, fighter2.turn().points().actionPoints());

        fighter2.turn().stop();
        assertEquals(6, fighter2.characteristics().get(Characteristic.ACTION_POINT));
    }

    @Test
    void movementPointLost() {
        fighter2.move(fight.map().get(211));
        fighter1.characteristics().alter(Characteristic.WISDOM, 100);

        castNormal(50, fighter2.cell()); // Maladresse

        Buff buff = fighter2.buffs().stream().filter(b -> b.effect().effect() == 127).findFirst().get();
        assertEquals(1, fighter2.characteristics().get(Characteristic.MOVEMENT_POINT));
        requestStack.assertOne(ActionEffect.buff(buff, -2));

        fighter1.turn().stop();
        assertEquals(1, fighter2.turn().points().movementPoints());

        fighter2.turn().stop();
        assertEquals(3, fighter2.characteristics().get(Characteristic.MOVEMENT_POINT));
    }

    @Test
    void stealActionPoints() {
        fighter2.move(fight.map().get(241));
        fighter1.characteristics().alter(Characteristic.WISDOM, 100);

        castNormal(98, fighter2.cell()); // Vol du Temps

        Buff buffT = fighter2.buffs().stream().filter(b -> b.effect().effect() == 101).findFirst().get();
        Buff buffC = fighter1.buffs().stream().filter(b -> b.effect().effect() == 111).findFirst().get();

        assertEquals(8, fighter1.characteristics().get(Characteristic.ACTION_POINT));
        assertEquals(4, fighter1.turn().points().actionPoints());
        assertEquals(4, fighter2.characteristics().get(Characteristic.ACTION_POINT));

        requestStack.assertOne(ActionEffect.buff(buffT, -2));
        requestStack.assertOne(ActionEffect.buff(buffC, 2));

        fighter1.turn().stop();
        assertEquals(4, fighter2.turn().points().actionPoints());

        fighter2.turn().stop();
        assertEquals(6, fighter2.characteristics().get(Characteristic.ACTION_POINT));
        assertEquals(8, fighter1.characteristics().get(Characteristic.ACTION_POINT));

        fighter1.turn().stop();
        assertEquals(6, fighter1.characteristics().get(Characteristic.ACTION_POINT));
        assertEquals(6, fighter2.characteristics().get(Characteristic.ACTION_POINT));
    }

    @Test
    void stealMovementPoints() {
        fighter2.move(fight.map().get(241));
        fighter1.characteristics().alter(Characteristic.WISDOM, 100);

        castNormal(170, fighter2.cell()); // Flèche Immobilisation

        Buff buffT = fighter2.buffs().stream().filter(b -> b.effect().effect() == 127).findFirst().get();

        assertEquals(4, fighter1.turn().points().movementPoints());
        assertEquals(2, fighter2.characteristics().get(Characteristic.MOVEMENT_POINT));

        requestStack.assertOne(ActionEffect.buff(buffT, -1));
        requestStack.assertOne(ActionEffect.addMovementPoints(fighter1, 1));

        fighter1.turn().stop();
        assertEquals(2, fighter2.turn().points().movementPoints());

        fighter2.turn().stop();
        assertEquals(3, fighter2.characteristics().get(Characteristic.MOVEMENT_POINT));
    }

    @Test
    void casterFixedDamage() {
        castNormal(135, fighter2.cell()); // Mot de Sacrifice

        int damage = fighter1.life().max() - fighter1.life().current();

        assertBetween(31, 40, damage);
        assertTrue(fighter2.life().isFull());
    }

    @Test
    void fixedDamage() {
        castNormal(536, fighter1.cell()); // Banzai

        int damage = fighter1.life().max() - fighter1.life().current();

        assertEquals(5, damage);
    }

    @Test
    void fixedStealLife() {
        List<Fighter> fighters = configureFight(builder -> builder
            .addSelf(fb -> fb.cell(207).charac(Characteristic.LUCK, 100).currentLife(500).maxLife(1000))
            .addAlly(fb -> fb.cell(221).currentLife(1000).maxLife(1000))
            .addEnemy(fb -> fb.cell(325))
        );

        castNormal(450, fighters.get(1).cell()); // Folie sanguinaire

        int damage = fighters.get(1).life().max() - fighters.get(1).life().current();

        assertEquals(300, damage);
        assertEquals(800, fighters.get(0).life().current());
    }

    @Test
    void percentLifeDamage() {
        castNormal(951, fighter2.cell()); // Rocaille

        int damage = fighter2.life().max() - fighter2.life().current();

        assertEquals(44, damage);
    }

    @Test
    void percentLifeLostDamage() {
        fighter1.life().alter(fighter1, -100);
        castNormal(1708, fighter2.cell()); // Correction Bwork

        int damage = fighter2.life().max() - fighter2.life().current();

        assertEquals(30, damage);
    }

    @Test
    void punishment() {
        List<Fighter> fighters = configureFight(builder -> builder
            .addSelf(fb -> fb.cell(207).charac(Characteristic.LUCK, 100).currentLife(200).maxLife(500))
            .addEnemy(fb -> fb.cell(221).currentLife(500).maxLife(500))
        );

        castNormal(446, fighters.get(1).cell()); // Punition

        int damage = fighters.get(1).life().max() - fighters.get(1).life().current();

        assertEquals(122, damage);
    }

    @Test
    void motlotov() {
        fighter1.life().alter(fighter1, -195); // Set life to 100LP
        castNormal(427, fighter1.cell()); // Mot Lotof

        requestStack.assertOne(ActionEffect.changeAppearance(fighter1, fighter1, 7032, 2));

        fighter1.turn().stop();
        fighter2.turn().stop();

        requestStack.assertOne(ActionEffect.launchVisualEffect(
            fighter1,
            fighter1.cell(),
            container.get(SpellService.class).get(1679).level(5)
        ));
        requestStack.assertOne(ActionEffect.alterLifePoints(fighter1, fighter1, -33));
        requestStack.assertOne(ActionEffect.alterLifePoints(fighter1, fighter2, -33));
        requestStack.assertOne(ActionEffect.resetAppearance(fighter1, fighter1));

        assertEquals(67, fighter1.life().current());
        assertEquals(17, fighter2.life().current());
    }

    @Test
    void givePercentLife() {
        List<Fighter> fighters = configureFight(builder -> builder
            .addSelf(fb -> fb.cell(185).charac(Characteristic.LUCK, 100).currentLife(200).maxLife(200))
            .addAlly(fb -> fb.cell(199).currentLife(100).maxLife(200))
            .addEnemy(fb -> fb.cell(221))
        );

        castNormal(435, fighters.get(0).cell());

        assertEquals(120, fighters.get(1).life().current());
        assertEquals(180, fighters.get(0).life().current());

        requestStack.assertOne(ActionEffect.alterLifePoints(fighters.get(0), fighters.get(0), -20));
        requestStack.assertOne(ActionEffect.alterLifePoints(fighters.get(0), fighters.get(1), 20));
    }

    private List<Fighter> configureFight(Consumer<FightBuilder> configurator) {
        fight.cancel(true);

        FightBuilder builder = fightBuilder();

        configurator.accept(builder);

        fight = builder.build(true);

        List<Fighter> fighters = fight.fighters();

        fight.state(PlacementState.class).startFight();
        fight.turnList().start();

        return fighters;
    }

    private void passTurns(int number) {
        for (; number > 0; --number) {
            fighter1.turn().stop();
            fighter2.turn().stop();
        }
    }

    private Spell castNormal(int spellId, FightCell target) {
        FightTurn currentTurn = fight.turnList().current().get();
        Spell spell = service.get(spellId).level(5);

        currentTurn.perform(new Cast(
            currentTurn,
            currentTurn.fighter(),
            spell,
            target,
            new SpellConstraintsValidator(),

            // Ensure no critical hit / fail
            new CriticalityStrategy() {
                public int hitRate(int base) { return 0; }
                public int failureRate(int base) { return 0; }
                public boolean hit(int baseRate) { return false; }
                public boolean failed(int baseRate) { return false; }
            }
        ));

        currentTurn.terminate();

        return spell;
    }

    private Spell castCritical(int spellId, FightCell target) {
        FightTurn currentTurn = fight.turnList().current().get();
        Spell spell = service.get(spellId).level(5);

        currentTurn.perform(new Cast(
            currentTurn,
            currentTurn.fighter(),
            spell,
            target,
            new SpellConstraintsValidator(),

            // Ensure critical hit
            new CriticalityStrategy() {
                public int hitRate(int base) { return 100; }
                public int failureRate(int base) { return 0; }
                public boolean hit(int baseRate) { return true; }
                public boolean failed(int baseRate) { return false; }
            }
        ));

        currentTurn.terminate();

        return spell;
    }
}
