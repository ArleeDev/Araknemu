package fr.quatrevieux.araknemu.game.party;

import fr.quatrevieux.araknemu.game.GameConfiguration;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PartyService
{
    private final PlayerService playerService;
    private final PartyInviteService partyInviteService;
    private final GameConfiguration configuration;
    private final Logger logger;
    private final Set<Party> parties = new HashSet<>();

    public PartyService(PlayerService playerService, PartyInviteService partyInviteService, GameConfiguration configuration, Logger logger) {
        this.playerService = playerService;
        this.partyInviteService = partyInviteService;
        this.configuration = configuration;
        this.logger=logger;
    }

    public Party create(GamePlayer inviter, GamePlayer invitee)
    {
        partyInviteService.removeIfContains(inviter);
        return new Party(inviter,invitee);
    }

    public Optional<Party> getIfContains(GamePlayer player)
    {
        return parties.stream()
                .filter(party -> party.getPlayersInParty().contains(player))
                .findFirst();
    }

    public boolean isFull(Party party)
    {
        return party.getPlayersInParty().size() == configuration.party().maxSize();
    }

    public boolean addMember(Party party, GamePlayer member)
    {
        partyInviteService.removeIfContains(member);
        return party.getPlayersInParty().add(member);
    }

    public boolean setLeader(Party party, GamePlayer player)
    {
        if(party.getPlayersInParty().contains(player))
        {
            party.setLeader(player);
            return true;
        }

        return false;
    }
}
