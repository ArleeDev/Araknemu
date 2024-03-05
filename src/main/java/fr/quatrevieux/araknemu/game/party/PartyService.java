package fr.quatrevieux.araknemu.game.party;

import fr.quatrevieux.araknemu.game.GameConfiguration;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.exploration.map.FlagType;
import fr.quatrevieux.araknemu.game.listener.party.LeavePartyOnDisconnect;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import fr.quatrevieux.araknemu.network.game.out.info.PlayerCoordinateHighlight;
import fr.quatrevieux.araknemu.network.game.out.party.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PartyService {
	private static class Party {
		private final Set<GamePlayer> playersInParty;
		private GamePlayer leader;

		private Party(GamePlayer inviter, GamePlayer invitee)
		{
			playersInParty = new HashSet<>();
			playersInParty.add(inviter);
			playersInParty.add(invitee);
			leader = inviter;
		}
	}


	private final PlayerService playerService;
	private final PartyInviteService partyInviteService;
	private final ExplorationMapService explorationMapService;
	private final GameConfiguration.PartyConfiguration configuration;
	private final Set<Party> parties = new HashSet<>();

	public PartyService(PlayerService playerService, PartyInviteService partyInviteService, ExplorationMapService explorationMapService, GameConfiguration configuration)
	{
		this.playerService = playerService;
		this.partyInviteService = partyInviteService;
		this.explorationMapService = explorationMapService;
		this.configuration = configuration.party();
	}

	private Party create(GamePlayer inviter, GamePlayer invitee)
	{
		partyInviteService.removeIfContains(inviter);
		Party party = new Party(inviter, invitee);
		party.leader = inviter;
		parties.add(party);
		registerListeners(inviter);
		registerListeners(invitee);
		return party;
	}

	private boolean contains(GamePlayer player)
	{
		return getIfContains(player).isPresent();
	}

	private Optional<Party> getIfContains(GamePlayer player)
	{
		return parties.stream().filter(party -> party.playersInParty.contains(player)).findFirst();
	}

	private boolean isFull(Party party)
	{
		return party.playersInParty.size()==configuration.maxSize();
	}

	private boolean addMember(Party party, GamePlayer member)
	{
		partyInviteService.removeIfContains(member);
		boolean success = party.playersInParty.add(member);
		if(success)
			registerListeners(member);
		return success;
	}

	private boolean setLeader(Party party, GamePlayer player)
	{
		if(party.playersInParty.contains(player))
		{
			party.leader = player;
			return true;
		}

		return false;
	}

	private boolean removePlayerIfContains(Party party, GamePlayer player)
	{
		boolean success = party.playersInParty.remove(player);
		if(success)
		{
			if(party.playersInParty.isEmpty()) //remove party
				removeIfContains(party);
		}
		return success;
	}

	private Optional<GamePlayer> getPlayerIfContains(Party party, int playerid)
	{
		return party.playersInParty.stream().filter(player -> player.id()==playerid).findFirst();
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
			if(!party.leader.equals(player))
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

			if(party.playersInParty.size()==1)    //one player left, disband party
			{
				GamePlayer lastPlayer = party.playersInParty.iterator().next();
				leaveParty(party, lastPlayer, lastPlayer);
			}
			else
			{
				if(party.leader.equals(leaver))
				{
					GamePlayer newLeader = party.playersInParty.iterator().next();
					if(setLeader(party, newLeader))
						party.playersInParty.forEach(pl -> pl.send(new PartyLeaderResponse(newLeader.id())));
				}
				party.playersInParty.forEach(pl -> pl.send(new PartyUpdatedResponse.PlayerRemoved(leaver.id()))); //updates partyUI for remaining party members
			}
		}

		return success;
	}

	public void partyPositions(GamePlayer requestor)
	{
		int requestorSuperarea = explorationMapService.load(requestor.position().map()).subArea().area().superarea();

		getIfContains(requestor).ifPresentOrElse(party -> {
			Set<GamePlayer> players = party.playersInParty.stream().filter(p -> !p.equals(requestor))
				.filter(p -> explorationMapService.load(p.position().map())
				.subArea().area()
				.superarea()==requestorSuperarea)
				.collect(Collectors.toSet());
			requestor.send(new PlayerCoordinateHighlight(players, explorationMapService, FlagType.FLAG_MAP_GROUP));
		}, () -> {throw new RuntimeException("no party found containing player");});
	}

	public boolean inviteAsk(GamePlayer requestor, String inviteeName)
	{
		return playerService.online().stream()
			.filter(p -> p.name().equals(inviteeName))
			.findFirst().map(invitee ->
			{
				if(invitee.isFighting())
				{
					requestor.send(new InviteResponse.Ask.FailedBusy());
					return false;
				}
				if(getIfContains(invitee).isPresent())
				{
					requestor.send(new InviteResponse.Ask.FailedAlreadyGrouped());
					return false;
				}
				return getIfContains(requestor).map(party -> {
					if(isFull(party))
					{
						requestor.send(new InviteResponse.Ask.FailedPartyFull());
						return false;
					}

					sendInvite(requestor, invitee);
					return true;
				}).orElseGet(() -> {
					sendInvite(requestor,invitee);
					return true;
				});
			}).orElseGet(() -> {
				requestor.send(new InviteResponse.Ask.FailedCantFind(inviteeName));
				return false;
			});
	}

	private void sendInvite(GamePlayer inviter, GamePlayer invitee)
	{
		partyInviteService.create(inviter, invitee);
		InviteResponse.Ask.Invited outPacket = new InviteResponse.Ask.Invited(inviter, invitee);
		inviter.send(outPacket);
		invitee.send(outPacket);
	}

	public boolean inviteAccept(GamePlayer invitee)
	{
		if(contains(invitee))
			if(partyInviteService.contains(invitee)) //invite pending but already in party, should be unreachable unless desync happens
			{
				invitee.send(new PartyCreatedResponse.FailedAlreadyGrouped());
				return false;
			}

		return partyInviteService.getInviterByInvitee(invitee).map(inviter ->
			getIfContains(inviter).map(party ->
			{
				if(isFull(party))
				{
					invitee.send(new PartyCreatedResponse.FailedFull());
					return false;
				}

				party.playersInParty.forEach(player -> sendJoinedParty(player, invitee)); //adds the invitee to UI for party
				addMember(party, invitee);
				sendJoinParty(party, invitee, inviter); //invited into existing party
				inviter.send(new InviteResponse.Accept());
				return true;
			})
			.orElseGet(() -> {
				Party party = create(inviter, invitee); //creating new party with both inviter and invitee
				sendJoinParty(party, inviter, inviter);
				sendJoinParty(party, invitee, inviter);
				inviter.send(new InviteResponse.Accept());
				return true;
			})).orElse(false);
	}


	/**
	 * Packet that party members receive when a new player joins the party
	 * @param partyMember
	 * @param invitee
	 */
	private void sendJoinedParty(GamePlayer partyMember, GamePlayer invitee) //party members receiving new player
	{
		partyMember.send(new PartyUpdatedResponse.PlayerAdded(invitee));
	}

	/**
	 * Packets that a player receives when they join a new party
	 * @param party
	 * @param invitee
	 * @param inviter
	 */
	private void sendJoinParty(Party party, GamePlayer invitee, GamePlayer inviter) //player joining party
	{
		invitee.send(new PartyCreatedResponse.Created(inviter.name()));
		invitee.send(new PartyLeaderResponse(party.leader.id()));
		invitee.send(new PartyUpdatedResponse.PlayerAdded(party.playersInParty));
	}
}
