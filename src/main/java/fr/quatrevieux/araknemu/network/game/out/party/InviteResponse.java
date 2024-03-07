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
import fr.quatrevieux.araknemu.network.game.out.info.Error;

/**
 * Represents the complete set of partyInvite responses (PI, PA, PR packets)
 */
public final class InviteResponse {

    /**
     * Represents the complete set of partyInviteRequest responses (PI packets)
     */
    public static final class Ask {

        /**
         * Sends a party invite request
         * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L48">...</a>
         */
        public static final class Invited {
            private final GamePlayer inviter;
            private final GamePlayer invitee;

            public Invited(GamePlayer inviter, GamePlayer invitee) {
                this.inviter = inviter;
                this.invitee = invitee;
            }

            public String toString() {
                return "PIK" + inviter.name() + "|" + invitee.name();
            }
        }

        /**
         * Fail if target is occupied
         */
        public static final class FailedBusy {
            private final Error error = Error.cantInvitePlayerBusy();

            @Override
            public String toString() {
                return error.toString();
            }
        }

        /**
         * Denies a party invite request if the source(inviter)'s party is full
         * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L86">...</a>
         */
        public static final class FailedPartyFull {
            public String toString() {
                return "PIEf";
            }
        }

        /**
         * Denies a party invite request if the target(invitee) is unfindable (e.g. logged out)
         * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L91">...</a>
         */
        public static final class FailedCantFind {
            private final String inviteeName;

            public FailedCantFind(String inviteeName) {
                this.inviteeName = inviteeName;
            }

            public String toString() {
                return "PIEn" + inviteeName;
            }
        }

        /**
         * Denies a party invite request if the target(invitee) is already in a party
         * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L81">...</a>
         */
        public static final class FailedAlreadyGrouped {
            public String toString() {
                return "PIEa";
            }
        }
    }

    /**
     * Accepts a pending party invite request
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L115">...</a>
     */
    public static final class Accept {
        @Override
        public String toString() {
            return "PA";
        }
    }

    /**
     * Refuses a pending party invite request
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L110">...</a>
     */
    public static final class Refuse {
        @Override
        public String toString() {
            return "PR";
        }
    }
}
