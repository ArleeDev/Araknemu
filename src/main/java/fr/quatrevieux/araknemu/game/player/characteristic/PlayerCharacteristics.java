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

package fr.quatrevieux.araknemu.game.player.characteristic;

import fr.quatrevieux.araknemu.core.event.Dispatcher;
import fr.quatrevieux.araknemu.data.constant.Characteristic;
import fr.quatrevieux.araknemu.data.living.entity.player.Player;
import fr.quatrevieux.araknemu.data.value.BoostStatsData;
import fr.quatrevieux.araknemu.game.item.effect.SpecialEffect;
import fr.quatrevieux.araknemu.game.item.type.AbstractEquipment;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.characteristic.event.CharacteristicsChanged;
import fr.quatrevieux.araknemu.game.player.inventory.PlayerInventory;
import fr.quatrevieux.araknemu.game.player.race.GamePlayerRace;
import fr.quatrevieux.araknemu.game.world.creature.characteristics.Characteristics;
import fr.quatrevieux.araknemu.game.world.creature.characteristics.DefaultCharacteristics;
import fr.quatrevieux.araknemu.game.world.creature.characteristics.MutableCharacteristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Characteristic map for player
 * This class will handle aggregation of stats, and computed stats
 */
public final class PlayerCharacteristics implements CharacterCharacteristics {
    private final MutableCharacteristics base;
    private final Dispatcher dispatcher;
    private final Player entity;
    private final GamePlayerRace race;
    private final GamePlayer player;

    private final SpecialEffects specials = new SpecialEffects();

    private Characteristics stuff;

    public PlayerCharacteristics(Dispatcher dispatcher, GamePlayer player, Player entity) {
        this.dispatcher = dispatcher;
        this.player = player;
        this.entity = entity;
        this.race = player.race();
        this.base = new MutableComputedCharacteristics(new BaseCharacteristics(dispatcher, race, entity));

        this.stuff = computeStuffStats(player.inventory());
    }

    @Override
    public int get(Characteristic characteristic) {
        return base.get(characteristic) + stuff.get(characteristic);
    }

    @Override
    public MutableCharacteristics base() {
        return base;
    }

    @Override
    public Characteristics stuff() {
        return stuff;
    }

    @Override
    public Characteristics feats() {
        // @todo #218
        return new DefaultCharacteristics();
    }

    @Override
    public Characteristics boost() {
        // @todo #218
        return new DefaultCharacteristics();
    }

    /**
     * Get the current special effects
     */
    public SpecialEffects specials() {
        return specials;
    }

    /**
     * Boost a characteristic
     *
     * @throws IllegalStateException When the character has no enough points for boost the required characteristic
     */
    public void boostCharacteristic(Characteristic characteristic) {
        final BoostStatsData.Interval interval = race.boost(characteristic, base.get(characteristic));

        final int points = entity.boostPoints() - interval.cost();

        if (points < 0) {
            throw new IllegalStateException("Not enough points for boost stats");
        }

        entity.setBoostPoints(points);
        base.add(characteristic, interval.boost());
    }

    @Override
    public int boostPoints() {
        return entity.boostPoints();
    }

    /**
     * Boost a characteristic by amount, or until out of boostpoints
     */
    public void boostCharacteristic(Characteristic characteristic, int amount) {
        int remainingPoints = entity.boostPoints();
        final int initAmount = base.get(characteristic);
        int add = 0;

        while (add < amount) {
            final BoostStatsData.Interval interval = race.boost(characteristic, initAmount + add);

            if (remainingPoints - interval.cost() >= 0) {
                remainingPoints -= interval.cost();
                add++;
            } else {
                break;
            }
        }

        if (remainingPoints < 0) {
            throw new IllegalStateException("Not enough points for boost stats");
        }

        entity.setBoostPoints(remainingPoints);
        base.add(characteristic, add);
    }

    /**
     * Calculates the amount of characteristics points a player has spent, sets their characteristics to 0 and refunds them the points
     * TODO: check if safe with scrolls and items
     */
    public void restat() {
        final List<Characteristic> stats = new ArrayList<>();
        stats.add(Characteristic.VITALITY);
        stats.add(Characteristic.WISDOM);
        stats.add(Characteristic.STRENGTH);
        stats.add(Characteristic.INTELLIGENCE);
        stats.add(Characteristic.LUCK);
        stats.add(Characteristic.AGILITY);

        final int basePoints = stats.stream().mapToInt(this::getPointsFromBase).sum();
        if (basePoints > 0) {
            final Map<Characteristic, Integer> values = stats.stream().filter(s -> base.get(s) != 0).collect(Collectors.toMap(s -> s, s -> 0));

            entity.setBoostPoints(entity.boostPoints() + basePoints);
            base.setAll(values);
        }
    }

    private int getPointsFromBase(Characteristic stat) {
        final int pointsInStat = base.get(stat);
        int currentPoint = 0;
        int characteristicsPointsSpent = 0;

        while (currentPoint < pointsInStat) {
            final BoostStatsData.Interval interval = race.boost(stat, currentPoint);

            characteristicsPointsSpent += interval.cost();
            currentPoint++;
        }

        return characteristicsPointsSpent;
    }

    @Override
    public int initiative() {
        final int value = race.initiative(player.properties().life().max()) + get(Characteristic.STRENGTH) + get(Characteristic.LUCK) + get(Characteristic.AGILITY) + get(Characteristic.INTELLIGENCE) + specials.get(SpecialEffects.Type.INITIATIVE);

        return Math.max(value * player.properties().life().current() / player.properties().life().max(), 1);
    }

    @Override
    public int discernment() {
        return race.startDiscernment() + get(Characteristic.LUCK) / 10 + specials.get(SpecialEffects.Type.DISCERNMENT);
    }

    @Override
    public int pods() {
        return race.startPods() + specials.get(SpecialEffects.Type.PODS) + get(Characteristic.STRENGTH) * 5;
    }

    /**
     * Rebuild the stuff stats
     */
    public void rebuildStuffStats() {
        stuff = computeStuffStats(player.inventory());

        dispatcher.dispatch(new CharacteristicsChanged());
    }

    /**
     * Rebuild the special effects
     */
    public void rebuildSpecialEffects() {
        specials.clear();

        for (AbstractEquipment equipment : player.inventory().equipments()) {
            for (SpecialEffect effect : equipment.specials()) {
                effect.apply(player);
            }
        }

        player.inventory().itemSets().applySpecials(player);
    }

    /**
     * Compute the stuff stats
     */
    private static Characteristics computeStuffStats(PlayerInventory inventory) {
        final MutableCharacteristics characteristics = new DefaultCharacteristics();

        for (AbstractEquipment equipment : inventory.equipments()) {
            equipment.apply(characteristics);
        }

        inventory.itemSets().apply(characteristics);

        return new ComputedCharacteristics<>(characteristics);
    }
}
