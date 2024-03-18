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
 * Copyright (c) 2017-2024 ArleeDev
 */

package fr.quatrevieux.araknemu.game.fight.fighter.monster;

import fr.quatrevieux.araknemu.data.constant.Characteristic;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.monster.Monster;

/**
 * Sprite for monster
 *
 * The sprite type ID MUST be the class id
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Game.as#L520
 */
public final class ControlledMonsterFighterSprite extends MonsterFighterSprite {
    private final Fighter fighter;
    private final Monster monster;


    public ControlledMonsterFighterSprite(Fighter fighter, Monster monster) {
        super(fighter,monster);
        this.fighter = fighter;
        this.monster = monster;
    }

    @Override
    public String toString() {
        return
                cell() + ";" +
                        fighter.orientation().ordinal() + ";" +
                        "0;" + // Bonus value, not used on player
                        id() + ";" +
                        monster.name()+";" +
                        "19;" + //raceid
                        monster.gfxId()+"^100;" +
                        "0;1;0,0,0,-4,0;" +
                        monster.colors().toHexString(";") + ";" +
                        "null,null,null,null,null,1,;" +
                        monster.life()+";" +
                        monster.characteristics().get(Characteristic.ACTION_POINT) + ";" +
                        monster.characteristics().get(Characteristic.MOVEMENT_POINT) + ";" +
                        monster.characteristics().get(Characteristic.RESISTANCE_PERCENT_NEUTRAL) + ";" +
                        monster.characteristics().get(Characteristic.RESISTANCE_PERCENT_EARTH) + ";" +
                        monster.characteristics().get(Characteristic.RESISTANCE_PERCENT_FIRE) + ";" +
                        monster.characteristics().get(Characteristic.RESISTANCE_PERCENT_WATER) + ";" +
                        monster.characteristics().get(Characteristic.RESISTANCE_PERCENT_AIR) + ";" +
                        monster.characteristics().get(Characteristic.RESISTANCE_ACTION_POINT) + ";" +
                        monster.characteristics().get(Characteristic.RESISTANCE_MOVEMENT_POINT) + ";" +
                        fighter.team().number()+ ";" +
                        ";18;18;";
                        /*"Tofu" + ";" +
                        spriteInfo.race().ordinal() + ";" +
                        spriteInfo.gfxId() + "^" + spriteInfo.size() + ";" +
                        spriteInfo.gender().ordinal() + ";" +
                        fighter.level() + ";" +
                        "0,0,0,0;" + // @todo alignment
                        spriteInfo.colors().toHexString(";") + ";" +
                        spriteInfo.accessories() + ";" +
                        fighter.life().current() + ";" +
                        fighter.characteristics().get(Characteristic.ACTION_POINT) + ";" +
                        fighter.characteristics().get(Characteristic.MOVEMENT_POINT) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_PERCENT_NEUTRAL) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_PERCENT_EARTH) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_PERCENT_FIRE) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_PERCENT_WATER) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_PERCENT_AIR) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_ACTION_POINT) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_MOVEMENT_POINT) + ";" +
                        fighter.team().number() + ";" +
                        ";" // @todo mount*/


                /*cell() + ";" +
                        fighter.orientation().ordinal() + ";" +
                        "0;" + // @todo Bonus value (get from group ?)
                        id() + ";" +
                        name() + ";" +
                        type().id() + ";" +
                        gfxId() + "^100;" + // @todo size
                        monster.gradeNumber() + ";" +
                        monster.colors().toHexString(";") + ";" +
                        "0,0,0,0;" + // @todo accessories
                        fighter.life().current() + ";" +
                        fighter.characteristics().get(Characteristic.ACTION_POINT) + ";" +
                        fighter.characteristics().get(Characteristic.MOVEMENT_POINT) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_PERCENT_NEUTRAL) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_PERCENT_EARTH) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_PERCENT_FIRE) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_PERCENT_WATER) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_PERCENT_AIR) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_ACTION_POINT) + ";" +
                        fighter.characteristics().get(Characteristic.RESISTANCE_MOVEMENT_POINT) + ";" +
                        fighter.team().number()
                ;*/
    }
}
