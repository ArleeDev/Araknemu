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

/**
 * Leaves the current party
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L150">...</a>
 */
public final class PartyLeaveResponse {

    public static final class Leave {
        @Override
        public String toString() {
            return "PV";
        }
    }

    public static final class Kicked {
        private final String kickerName;

        public Kicked(String kickerName) {
            this.kickerName = kickerName;
        }

        @Override
        public String toString() {
            return "PV" + kickerName;
        }
    }
}
