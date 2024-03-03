package fr.quatrevieux.araknemu.game.party;

import fr.quatrevieux.araknemu.game.GameConfiguration;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PartyInviteService
{
    //TODO: eventsubscriber: remove open invite if one of the two links in invite logs out or joins fight

    private final PlayerService playerService;
    private final GameConfiguration configuration;
    private final Logger logger;
    private final Set<PartyInvite> invites = new HashSet<>();

    public PartyInviteService(PlayerService playerService, GameConfiguration configuration, Logger logger) {
        this.playerService = playerService;
        this.configuration = configuration;
        this.logger=logger;
    }

    public PartyInvite create(GamePlayer inviter, GamePlayer invitee)
    {
        PartyInvite inv = new PartyInvite(inviter,invitee,logger);
        invites.add(inv);
        return inv;
    }

    public Optional<PartyInvite> getIfContains(GamePlayer player)
    {
        return invites.stream()
                .filter(invite -> invite.getInvitee().equals(player) || invite.getInviter().equals(player))
                .findFirst();
    }

    public boolean removeIfContains(GamePlayer player)
    {
        return invites.stream()
                .filter(invite -> invite.getInvitee().equals(player) || invite.getInviter().equals(player))
                .findFirst().map(inv -> {
                    invites.remove(inv);
                    return true;
                }).orElse(false);
    }

    public boolean remove(PartyInvite p)
    {
        return invites.remove(p);
    }
}
