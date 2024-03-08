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

public final class PartyCreatedResponse {

    /**
     * Creates a party
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L120">...</a>
     */
    public static final class Created {
        private final String playername;

        public Created(String playername) {
            this.playername = playername;
        }

        @Override
        public String toString() {
            return "PCK" + playername;
        }
    }

    /**
     * Error upon trying to invite a player to your party when it is already full
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L142">...</a>
     */
    public static final class FailedFull {
        @Override
        public String toString() {
            return "PCEf";
        }
    }

    /**
     * Error upon trying to invite a player who is already in a party
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L137">...</a>
     */
    public static final class FailedAlreadyGrouped {
        @Override
        public String toString() {
            return "PCEa";
        }
    }
}
