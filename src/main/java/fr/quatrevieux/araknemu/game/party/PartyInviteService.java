package fr.quatrevieux.araknemu.game.party;

import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.out.party.InviteResponse;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class PartyInviteService
{
    private static class PartyInvite
    {
        private final GamePlayer inviter;
        private final GamePlayer invitee;

        private PartyInvite(GamePlayer inviter, GamePlayer invitee) {
            this.inviter = inviter;
            this.invitee = invitee;
        }

        @Override
        public boolean equals(@Nullable Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PartyInvite that = (PartyInvite) o;
            return Objects.equals(inviter, that.inviter) && Objects.equals(invitee, that.invitee);
        }
    }


    //TODO: eventsubscriber: remove open invite if one of the two links in invite logs out or joins fight

    private final Set<PartyInvite> invites = new HashSet<>();

    public boolean create(GamePlayer inviter, GamePlayer invitee)
    {
        PartyInvite inv = new PartyInvite(inviter, invitee);
        invites.add(inv);
        return true;
    }

    private Optional<PartyInvite> getIfContains(GamePlayer player)
    {
        return invites.stream()
                .filter(invite -> invite.invitee.equals(player) || invite.inviter.equals(player))
                .findFirst();
    }

    public boolean removeIfContains(GamePlayer player)
    {
        return invites.stream()
                .filter(invite -> invite.invitee.equals(player) || invite.inviter.equals(player))
                .findFirst().map(inv -> {
                    invites.remove(inv);
                    return true;
                }).orElse(false);
    }

    public Optional<GamePlayer> getInviterByInvitee(GamePlayer invitee)
    {
        return getIfContains(invitee).map(invite -> invite.inviter);
    }

    public Optional<GamePlayer> getInviteeByInviter(GamePlayer inviter)
    {
        return getIfContains(inviter).map(invite -> invite.invitee);
    }

    public Optional<GamePlayer> getOtherPlayer(GamePlayer player)
    {
        boolean getInviter = getInviterByInvitee(player).isPresent();
        boolean getInvitee = getInviteeByInviter(player).isPresent();

        if(getInviter)
            return getInviterByInvitee(player);
        else if(getInvitee)
            return getInviteeByInviter(player);
        else
            return Optional.empty();
    }

    public boolean contains(GamePlayer player)
    {
        return getIfContains(player).isPresent();
    }

    public boolean inviteRefuse(GamePlayer player)
    {
        getOtherPlayer(player).map((otherPlayer) -> {
            player.send(new InviteResponse.Refuse());
            otherPlayer.send(new InviteResponse.Refuse());
            removeIfContains(player);
            return true;
        });
        return false;
    }
}
