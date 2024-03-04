package fr.quatrevieux.araknemu.game.party;

import fr.quatrevieux.araknemu.game.GameConfiguration;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PartyService {
	private final PlayerService playerService;
	private final PartyInviteService partyInviteService;
	private final GameConfiguration.PartyConfiguration configuration;
	private final Logger logger;
	private final Set<Party> parties = new HashSet<>();

	public PartyService(PlayerService playerService, PartyInviteService partyInviteService, GameConfiguration configuration, Logger logger)
	{
		this.playerService = playerService;
		this.partyInviteService = partyInviteService;
		this.configuration = configuration.party();
		this.logger = logger;
	}

	public Party create(GamePlayer inviter, GamePlayer invitee)
	{
		partyInviteService.removeIfContains(inviter);
		Party party = new Party(inviter, invitee);
		party.setLeader(inviter);
		parties.add(party);
		return party;
	}

	public Optional<Party> getIfContains(GamePlayer player)
	{
		return parties.stream().filter(party -> party.getPlayersInParty().contains(player)).findFirst();
	}

	public boolean isFull(Party party)
	{
		return party.getPlayersInParty().size()==configuration.maxSize();
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

	public boolean removePlayerIfContains(Party party, GamePlayer player)
	{
		boolean success = party.getPlayersInParty().remove(player);
		if(success)
		{
			if(party.getPlayersInParty().isEmpty()) //remove party
				removeIfContains(party);
		}
		return success;
	}

	public Optional<GamePlayer> getPlayerIfContains(Party party, int playerid)
	{
		return party.getPlayersInParty().stream().filter(player -> player.id()==playerid).findFirst();
	}

	private boolean removeIfContains(Party party)
	{
		return parties.stream().filter(partyInList -> partyInList.equals(party)).findFirst().map(found ->
		{
			parties.remove(found);
			return true;
		}).orElse(false);
	}
}
