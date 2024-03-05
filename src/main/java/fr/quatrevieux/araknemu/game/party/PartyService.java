package fr.quatrevieux.araknemu.game.party;

import fr.quatrevieux.araknemu.core.event.DefaultListenerAggregate;
import fr.quatrevieux.araknemu.core.event.ListenerAggregate;
import fr.quatrevieux.araknemu.game.GameConfiguration;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.exploration.map.FlagType;
import fr.quatrevieux.araknemu.game.listener.party.LeavePartyOnDisconnect;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import fr.quatrevieux.araknemu.network.game.out.info.PlayerCoordinateHighlight;
import fr.quatrevieux.araknemu.network.game.out.party.*;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PartyService {
	private final PlayerService playerService;
	private final PartyInviteService partyInviteService;
	private final ExplorationMapService explorationMapService;
	private final GameConfiguration.PartyConfiguration configuration;
	private final Logger logger;
	private final Set<Party> parties = new HashSet<>();
	private final ListenerAggregate dispatcher = new DefaultListenerAggregate();

	public PartyService(PlayerService playerService, PartyInviteService partyInviteService, ExplorationMapService explorationMapService, GameConfiguration configuration, Logger logger)
	{
		this.playerService = playerService;
		this.partyInviteService = partyInviteService;
		this.explorationMapService = explorationMapService;
		this.configuration = configuration.party();
		this.logger = logger;
	}

	public Party create(GamePlayer inviter, GamePlayer invitee)
	{
		partyInviteService.removeIfContains(inviter);
		Party party = new Party(inviter, invitee);
		party.setLeader(inviter);
		parties.add(party);
		registerListeners(inviter);
		registerListeners(invitee);
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
		boolean success = party.getPlayersInParty().add(member);
		if(success)
			registerListeners(member);
		return success;
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
		return parties.stream().filter(partyInList -> partyInList.equals(party)).findFirst().map(found -> {
			parties.remove(found);
			return true;
		}).orElse(false);
	}

	private void registerListeners(GamePlayer player)
	{
		player.dispatcher().add(new LeavePartyOnDisconnect(player, this));
	}

	public void unregisterListeners(GamePlayer player)
	{
		player.dispatcher().remove(LeavePartyOnDisconnect.class);
	}

	public boolean kickPlayer(GamePlayer player, int leaveId) throws RuntimeException
	{
		return getIfContains(player).map(party -> {
			if(!party.getLeader().equals(player))
				throw new RuntimeException("Player attempting to kick is not the party's leader");

			return getPlayerIfContains(party, leaveId).map(target -> leaveParty(party, player, target))
													  .orElseThrow(() -> new RuntimeException("target not found in requestor's party"));
		}).orElseThrow(() -> new RuntimeException("requestor is not in a party"));
	}

	public boolean leavePlayer(GamePlayer player)
	{
		return getIfContains(player).map(party -> leaveParty(party, player, player))
									.orElseThrow(() -> new RuntimeException("no party found containing player"));
	}

	private boolean leaveParty(Party party, GamePlayer kicker, GamePlayer leaver)
	{
		boolean success = removePlayerIfContains(party, leaver);

		if(success)
		{
			if(!kicker.equals(leaver))
				leaver.send(new PartyLeaveResponse.Kicked(kicker.name()));
			else
				leaver.send(new PartyLeaveResponse.Leave());

			if(party.getPlayersInParty().size()==1)    //one player left, disband party
			{
				GamePlayer lastPlayer = party.getPlayersInParty().iterator().next();
				leaveParty(party, lastPlayer, lastPlayer);
			}
			else
			{
				if(party.getLeader().equals(leaver))
				{
					GamePlayer newLeader = party.getPlayersInParty().iterator().next();
					if(setLeader(party, newLeader))
						party.getPlayersInParty().forEach(pl -> pl.send(new PartyLeaderResponse(newLeader.id())));
				}
				party.getPlayersInParty()
					 .forEach(pl -> pl.send(new PartyUpdatedResponse.PlayerRemoved(leaver.id()))); //updates partyUI for remaining party members
			}
		}

		return success;
	}

	public void partyPositions(GamePlayer requestor)
	{
		int requestorSuperarea = explorationMapService.load(requestor.position().map()).subArea().area().superarea();

		getIfContains(requestor).ifPresentOrElse(party -> {
			Set<GamePlayer> players = party.getPlayersInParty().stream().filter(p -> !p.equals(requestor))
										   .filter(p -> explorationMapService.load(p.position().map()).subArea().area()
																			 .superarea()==requestorSuperarea)
										   .collect(Collectors.toSet());
			requestor.send(new PlayerCoordinateHighlight(players, explorationMapService, FlagType.FLAG_MAP_GROUP));
		}, () -> {throw new RuntimeException("no party found containing player");});
	}

	public void inviteAsk(GamePlayer requestor, String inviteeName)
	{
		playerService.online().stream().filter(p -> p.name().equals(inviteeName)).findFirst()
					 .ifPresentOrElse(invitee -> {
						 if(invitee.isFighting())
							 requestor.send(new InviteResponse.Ask.FailedBusy());

						 else
						 {
							 if(getIfContains(invitee).isPresent())
								 requestor.send(new InviteResponse.Ask.FailedAlreadyGrouped());
							 else
							 {
								 getIfContains(requestor).ifPresentOrElse(party -> {
									 if(isFull(party))
										 requestor.send(new InviteResponse.Ask.FailedPartyFull());
									 else
										 sendInvite(requestor, invitee);
								 }, () -> sendInvite(requestor, invitee));
							 }
						 }
					 }, () -> requestor.send(new InviteResponse.Ask.FailedCantFind(inviteeName)));
	}

	private void sendInvite(GamePlayer inviter, GamePlayer invitee)
	{
		partyInviteService.create(inviter, invitee);
		InviteResponse.Ask.Invited outPacket = new InviteResponse.Ask.Invited(inviter, invitee);
		inviter.send(outPacket);
		invitee.send(outPacket);
	}
}
