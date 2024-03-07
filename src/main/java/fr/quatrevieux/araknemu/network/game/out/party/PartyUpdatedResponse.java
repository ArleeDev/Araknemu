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
 * Copyright (c) 2017-2024 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.network.game.out.party;

import fr.quatrevieux.araknemu.game.player.GamePlayer;

import java.util.HashSet;
import java.util.Set;

public final class PartyUpdatedResponse {
    /**
     * Constructs the party menu UIElement upon removing a party member
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L1227">...</a>
     */
    public static final class PlayerRemoved {
        private final int id;

        public PlayerRemoved(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "PM-" + id;
        }
    }

    /**
     * Constructs the party menu UIElement upon adding a new party member
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L198">...</a>
     */
    public static final class PlayerAdded {
        private final Set<GamePlayer> party;

        public PlayerAdded(Set<GamePlayer> players) {
            this.party = players;
        }

        public PlayerAdded(GamePlayer player) {
            this.party = new HashSet<>();
            party.add(player);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder("PM+");
            party.forEach(p -> builder.append(p.id()).append(";").append(p.name()).append(";").append(p.race().race().ordinal() * 10 + p.spriteInfo().gender().ordinal()).append(";") //maps to sprites: clientfolder/clips/sprites/(n).swf
                    .append(p.spriteInfo().colors().color1()).append(";").append(p.spriteInfo().colors().color2()).append(";").append(p.spriteInfo().colors().color3()).append(";").append(p.spriteInfo().accessories().toString()).append(";").append(p.properties().life().current()).append(",").append(p.properties().life().max()).append(";").append(p.properties().experience().level()).append(";").append(p.properties().characteristics().initiative()).append(";").append(p.properties().characteristics().discernment()).append(";") //prospecting
                    .append("0;") //"side", number, perhaps alignment? //TODO: impl
                    .append("\\|"));

            if (builder.length() > 1 && builder.charAt(builder.length() - 1) == '|') {
                builder.deleteCharAt(builder.length() - 1);
            }

            return builder.toString();
        }
    }

    /**
     * Reconstructs the party menu UIElement upon a change in party member data
     * (e.g. a partymember equipping a new item, changing their initiative and thus potentially the party order
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L232">...</a>
     */
    public static final class Reorder { //TODO: impl with listener
        private final Set<GamePlayer> party;

        public Reorder(Set<GamePlayer> party) {
            this.party = party;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder("PM~");
            party.forEach(p -> builder.append(p.id()).append(";").append(p.name()).append(";").append(p.race().race().ordinal() * 10 + p.spriteInfo().gender().ordinal()).append(";") //maps to sprites: clientfolder/clips/sprites/(n).swf
                    .append(p.spriteInfo().colors().color1()).append(";").append(p.spriteInfo().colors().color2()).append(";").append(p.spriteInfo().colors().color3()).append(";").append(p.spriteInfo().accessories().toString()).append(";").append(p.properties().life().current()).append(",").append(p.properties().life().max()).append(";").append(p.properties().experience().level()).append(";").append(p.properties().characteristics().initiative()).append(";").append(p.properties().characteristics().discernment()).append(";") //prospecting
                    .append("0;") //"side", number, perhaps alignment? //TODO: impl
                    .append("\\|"));

            if (builder.length() > 1 && builder.charAt(builder.length() - 1) == '|') {
                builder.deleteCharAt(builder.length() - 1);
            }

            return builder.toString();
        }
    }
}
