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
 * Represents the complete set of partyFllow responses (PF+, PF-, PFE packets)
 */
public final class PartyFollowResponse {

    /**
     * Tracks movements of the assigned party member on the (mini)map
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L174">...</a>
     */
    public static final class Start {
        private final int targetId;

        public Start(int targetId) {
            this.targetId = targetId;
        }

        @Override
        public String toString() {
            return "PF+" + targetId;
        }
    }

    /**
     * Stops tracking movements of the assigned party member on the (mini)map
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L174">...</a>
     */
    public static final class Stop {

        @Override
        public String toString() {
            return "PF-";
        }
    }

    /**
     * Tracks movements of the assigned party member on the (mini)map
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L180">...</a>
     */
    public static final class FailedNotGrouped {

        @Override
        public String toString() {
            return "PFE";
        }
    }
}
