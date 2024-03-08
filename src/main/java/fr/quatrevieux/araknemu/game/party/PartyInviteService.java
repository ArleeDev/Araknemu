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

package fr.quatrevieux.araknemu.game.party;

import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import fr.quatrevieux.araknemu.network.game.out.party.InviteResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyCreatedResponse;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class PartyInviteService {
    //TODO: listener: remove open invite if one of the two links in invite logs out or joins fight
    private final Set<PartyInvite> invites = new HashSet<>();
    private final PlayerService playerService;
    private final PartyService partyService;

    public PartyInviteService(PlayerService playerService, PartyService partyService) {
        this.playerService = playerService;
        this.partyService = partyService;
    }

    /**
     * Validates InviteRequest, if validated: creates party invite between inviter and invitee
     */
    public boolean ask(GamePlayer inviter, String inviteeName) {
        final Optional<GamePlayer> maybeInvitee = playerService.online().stream().filter(p -> p.name().equals(inviteeName)).findFirst();
        if (!maybeInvitee.isPresent()) {
            inviter.send(new InviteResponse.Ask.FailedCantFind(inviteeName));
            return false;
        }
        final GamePlayer invitee = maybeInvitee.get();
        if (invitee.isFighting()) {
            inviter.send(new InviteResponse.Ask.FailedBusy());
            return false;
        }
        if (getIfContains(invitee).isPresent()) {
            inviter.send(new InviteResponse.Ask.FailedAlreadyGrouped());
            return false;
        }
        if (partyService.isPartied(invitee)) {
            inviter.send(new PartyCreatedResponse.FailedAlreadyGrouped());
            return false;
        }
        if (partyService.isFull(inviter).isPresent() && partyService.isFull(inviter).get()) {
            inviter.send(new InviteResponse.Ask.FailedPartyFull());
            return false;
        }

        createAndSend(inviter, invitee);
        return true;
    }

    private Optional<PartyInvite> getIfContains(GamePlayer player) {
        return invites.stream().filter(invite -> invite.invitee.equals(player) || invite.inviter.equals(player)).findFirst();
    }

    /**
     * creates party invite prompt between inviter and invitee
     */
    private void createAndSend(GamePlayer inviter, GamePlayer invitee) {
        invites.add(new PartyInvite(inviter, invitee));
        final InviteResponse.Ask.Invited outPacket = new InviteResponse.Ask.Invited(inviter, invitee);
        inviter.send(outPacket);
        invitee.send(outPacket);
    }

    /**
     * Validates InviteAccept. If validated: inviter and invitee join new party OR invitee joins existing party
     */
    public boolean accept(GamePlayer invitee) {
        if (!getIfContains(invitee).isPresent()) {
            throw new RuntimeException("Player(" + invitee.name() + ") accepted party invite but invite has not been registered)");
        }
        if (partyService.isPartied(invitee)) {
            invitee.send(new PartyCreatedResponse.FailedAlreadyGrouped());
            return false;
        }

        final PartyInvite invite = getIfContains(invitee).get();
        final GamePlayer inviter = invite.inviter;

        return partyService.isFull(inviter).map(full -> { //inviter is in party, invitee joins their party
            if (full) {
                invitee.send(new PartyCreatedResponse.FailedFull());
                return false;
            } else {
                invites.remove(invite);
                partyService.joinExisting(inviter, invitee);
                inviter.send(new InviteResponse.Accept());
                return true;
            }
        }).orElseGet(() -> { //inviter is not in party, creating new party
            invites.remove(invite);
            partyService.joinNew(inviter, invitee);
            inviter.send(new InviteResponse.Accept());
            return true;
        });
    }

    /**
     * Validates InviteRefuse. If validated: pending invitation is refused
     */
    public boolean refuse(GamePlayer player) {
        if (!getIfContains(player).isPresent()) {
            throw new RuntimeException("Player(" + player.name() + ") refused party invite but invite has not been registered)");
        }

        final PartyInvite invite = getIfContains(player).get();

        player.send(new InviteResponse.Refuse());
        invite.getOther(player).send(new InviteResponse.Refuse());
        invites.remove(invite);
        return true;
    }

    private static final class PartyInvite {
        private final GamePlayer inviter;
        private final GamePlayer invitee;

        private PartyInvite(GamePlayer inviter, GamePlayer invitee) {
            this.inviter = inviter;
            this.invitee = invitee;
        }

        private GamePlayer getOther(GamePlayer one) {
            if (one.equals(inviter)) {
                return invitee;
            }
            return inviter;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final PartyInvite that = (PartyInvite) o;
            return Objects.equals(inviter, that.inviter) && Objects.equals(invitee, that.invitee);
        }
    }
}
