package fr.quatrevieux.araknemu.network.game.out.party;

/**
 * Represents the complete set of partyFllow responses (PF+, PF-, PFE packets)
 */
public class PartyFollowResponse {

    /**
     * Tracks movements of the assigned party member on the (mini)map
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L174">...</a>
     */
    public static class Start {
        private final int targetId;

        public Start(int targetId)
        {
            this.targetId = targetId;
        }

        @Override
        public String toString() {
            return "PF+"+targetId;
        }
    }

    /**
     * Stops tracking movements of the assigned party member on the (mini)map
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L174">...</a>
     */
    public static class Stop {

        @Override
        public String toString() {
            return "PF-";
        }
    }

    /**
     * Tracks movements of the assigned party member on the (mini)map
     * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L180">...</a>
     */
    public static class FailedNotGrouped {

        @Override
        public String toString() {
            return "PFE";
        }
    }
}