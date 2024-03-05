package fr.quatrevieux.araknemu.game.party;

import fr.quatrevieux.araknemu.data.value.Geolocation;
import fr.quatrevieux.araknemu.game.GameConfiguration;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.exploration.map.FlagType;
import fr.quatrevieux.araknemu.game.listener.party.LeavePartyOnDisconnect;
import fr.quatrevieux.araknemu.game.listener.party.UpdateFollowersOnMapLoaded;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import fr.quatrevieux.araknemu.network.game.out.info.InfoCompassResponse;
import fr.quatrevieux.araknemu.network.game.out.info.InfoCoordinateHighlightPlayerResponse;
import fr.quatrevieux.araknemu.network.game.out.party.*;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class PartyService {
	private final PlayerService playerService;
	private final PartyInviteService partyInviteService;
	private final ExplorationMapService explorationMapService;
	private final GameConfiguration.PartyConfiguration configuration;
	private final Set<Party> parties = new HashSet<>();

	private static class Party {
		private final Map<GamePlayer, Set<GamePlayer>> partyPlayers;
		private GamePlayer leader;

		private Party(GamePlayer inviter, GamePlayer invitee)
		{
			partyPlayers = new HashMap<>();
			partyPlayers.put(inviter, new HashSet<>());
			partyPlayers.put(invitee, new HashSet<>());
			leader = inviter;
		}

		@Override
		public boolean equals(@Nullable Object o)
		{
			if(this==o)
				return true;
			if(o==null||getClass()!=o.getClass())
				return false;
			Party party = (Party) o;
			return Objects.equals(partyPlayers, party.partyPlayers)&&Objects.equals(leader, party.leader);
		}
	}

	public PartyService(PlayerService playerService, PartyInviteService partyInviteService, ExplorationMapService explorationMapService, GameConfiguration configuration)
	{
		this.playerService = playerService;
		this.partyInviteService = partyInviteService;
		this.explorationMapService = explorationMapService;
		this.configuration = configuration.party();
	}

	@SuppressWarnings({"keyfor", "dereference.of.nullable"})
	public void startFollow(GamePlayer follower, int targetId)
	{
		getIfContains(follower).ifPresentOrElse(party -> {
			 getIfContains(targetId).ifPresentOrElse(target -> {
				 if(party.partyPlayers.containsKey(target))
					 follow(target,follower,party.partyPlayers.get(target)); //happy flow
				 else
					follower.send(new PartyFollowResponse.FailedNotGrouped()); //requestor and target not in same group
			}, () -> follower.send(new PartyFollowResponse.FailedNotGrouped())); //target not grouped
		}, () -> follower.send(new PartyFollowResponse.FailedNotGrouped()));	//requestor not grouped

	}

	private void follow(GamePlayer target, GamePlayer follower, Set<GamePlayer> followers)
	{
		followers.add(follower);
		if(followers.size()==1)
			registerFollowListenerIfNew(target);
		follower.send(new PartyFollowResponse.Start(target.id()));
	}

	@SuppressWarnings({"dereference.of.nullable"})
	public void stopFollow(GamePlayer follower, int targetId)
	{
		getIfContains(follower).ifPresentOrElse(party -> {
			getIfContains(targetId).ifPresentOrElse(target -> {
				if(party.partyPlayers.containsKey(target))
					unfollow(target,follower,party.partyPlayers.get(target));	//happy
				else
					follower.send(new PartyFollowResponse.FailedNotGrouped()); //requestor and target not in same group
			}, () -> follower.send(new PartyFollowResponse.FailedNotGrouped())); //target not grouped
		}, () -> follower.send(new PartyFollowResponse.FailedNotGrouped()));	//requestor not grouped
	}

	private void unfollow(GamePlayer target, GamePlayer follower, Set<GamePlayer> followers)
	{
		followers.remove(follower);
		if(followers.isEmpty())
			unregisterFollowListenerIfHas(target);
		follower.send(new PartyFollowResponse.Stop());
	}

	public void unregisterListeners(GamePlayer player)
	{
		player.dispatcher().remove(UpdateFollowersOnMapLoaded.class);
		player.dispatcher().remove(LeavePartyOnDisconnect.class);
	}

	@SuppressWarnings("dereference.of.nullable")
	public boolean updateFollowers(GamePlayer updater)
	{
		return getIfContains(updater).map(party -> {
			if(!party.partyPlayers.get(updater).isEmpty())
			{
				Geolocation location = explorationMapService.load(updater.position().map()).geolocation();
				party.partyPlayers.get(updater).forEach(follower -> follower.send(new InfoCompassResponse(location.x(), location.y())));
				return true;
			}
			return false; //has no followers but asked to update them
		}).orElse(false);
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

	public void partyPositions(GamePlayer requestor)
	{
		int requestorSuperarea = explorationMapService.load(requestor.position().map()).subArea().area().superarea();

		getIfContains(requestor).ifPresentOrElse(party -> {
			Set<GamePlayer> players = party.partyPlayers.keySet().stream().filter(p -> !p.equals(requestor))
														.filter(p -> explorationMapService.load(p.position().map())
																						  .subArea().area()
																						  .superarea()==requestorSuperarea)
														.collect(Collectors.toSet());
			requestor.send(new InfoCoordinateHighlightPlayerResponse(players, explorationMapService, FlagType.FLAG_MAP_GROUP));
		}, () -> {throw new RuntimeException("no party found containing player");});
	}

	public boolean inviteAsk(GamePlayer requestor, String inviteeName)
	{
		return playerService.online().stream().filter(p -> p.name().equals(inviteeName)).findFirst().map(invitee -> {
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
				sendInvite(requestor, invitee);
				return true;
			});
		}).orElseGet(() -> {
			requestor.send(new InviteResponse.Ask.FailedCantFind(inviteeName));
			return false;
		});
	}

	public boolean leavePlayer(GamePlayer player)
	{
		return getIfContains(player).map(party -> leaveParty(party, player, player))
									.orElseThrow(() -> new RuntimeException("no party found containing player"));
	}

	public boolean inviteAccept(GamePlayer invitee)
	{
		if(contains(invitee))
			if(partyInviteService.contains(invitee)) //invite pending but already in party, should be unreachable unless desync happens
			{
				invitee.send(new PartyCreatedResponse.FailedAlreadyGrouped());
				return false;
			}

		return partyInviteService.getInviterByInvitee(invitee).map(inviter -> getIfContains(inviter).map(party -> {
			if(isFull(party))
			{
				invitee.send(new PartyCreatedResponse.FailedFull());
				return false;
			}

			party.partyPlayers.keySet()
							  .forEach(player -> sendJoinedParty(player, invitee)); //adds the invitee to UI for party
			addMember(party, invitee);
			sendJoinParty(party, invitee, inviter); //invited into existing party
			inviter.send(new InviteResponse.Accept());
			return true;
		}).orElseGet(() -> {
			Party party = create(inviter, invitee); //creating new party with both inviter and invitee
			sendJoinParty(party, inviter, inviter);
			sendJoinParty(party, invitee, inviter);
			inviter.send(new InviteResponse.Accept());
			return true;
		})).orElse(false);
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
		return parties.stream().filter(party -> party.partyPlayers.containsKey(player)).findFirst();
	}

	private Optional<GamePlayer> getIfContains(int targetId)
	{
		return parties.stream().map(party -> new HashSet<>(party.partyPlayers.keySet()))
			.flatMap(Collection::stream)
			.filter(key -> key.id()==targetId)
			.findFirst();
	}

	private boolean isFull(Party party)
	{
		return party.partyPlayers.keySet().size()==configuration.maxSize();
	}

	private void addMember(Party party, GamePlayer member)
	{
		if(partyInviteService.contains(member)&&!contains(member)) //pending invite and not already in partyu
		{
			partyInviteService.removeIfContains(member);
			party.partyPlayers.put(member, new HashSet<>());
			registerListeners(member);
		}
	}

	private boolean setLeader(Party party, GamePlayer player)
	{
		if(party.partyPlayers.containsKey(player))
		{
			party.leader = player;
			return true;
		}

		return false;
	}

	private boolean removePlayerIfContains(Party party, GamePlayer player)
	{
		if(!party.partyPlayers.containsKey(player))
			return false;

		party.partyPlayers.remove(player);
		if(party.partyPlayers.isEmpty())
			removeIfContains(party);
		return true;
	}

	private Optional<GamePlayer> getPlayerIfContains(Party party, int playerid)
	{
		return party.partyPlayers.keySet().stream().filter(player -> player.id()==playerid).findFirst();
	}

	private void removeIfContains(Party party)
	{
		parties.stream().filter(partyInList -> partyInList.equals(party)).findFirst().ifPresent(parties::remove);
	}

	private void registerFollowListenerIfNew(GamePlayer player)
	{
		if(!hasFollowListener(player))
		{
			player.dispatcher().add(new UpdateFollowersOnMapLoaded(player, this));
		}
	}

	private boolean hasFollowListener(GamePlayer player)
	{
		return player.dispatcher().get(UpdateFollowersOnMapLoaded.class)!=null;
	}

	private void unregisterFollowListenerIfHas(GamePlayer player)
	{
		if(hasFollowListener(player))
		{
			player.dispatcher().remove(UpdateFollowersOnMapLoaded.class);
		}
	}

	private void registerListeners(GamePlayer player)
	{
		player.dispatcher().add(new LeavePartyOnDisconnect(player, this));
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

			if(party.partyPlayers.keySet().size()==1)    //one player left, disband party
			{
				GamePlayer lastPlayer = party.partyPlayers.keySet().iterator().next();
				leaveParty(party, lastPlayer, lastPlayer);
			}
			else
			{
				if(party.leader.equals(leaver))
				{
					GamePlayer newLeader = party.partyPlayers.keySet().iterator().next();
					if(setLeader(party, newLeader))
						party.partyPlayers.keySet().forEach(pl -> pl.send(new PartyLeaderResponse(newLeader.id())));
				}
				party.partyPlayers.keySet()
								  .forEach(pl -> pl.send(new PartyUpdatedResponse.PlayerRemoved(leaver.id()))); //updates partyUI for remaining party members
			}
		}

		return success;
	}

	private void sendInvite(GamePlayer inviter, GamePlayer invitee)
	{
		partyInviteService.create(inviter, invitee);
		InviteResponse.Ask.Invited outPacket = new InviteResponse.Ask.Invited(inviter, invitee);
		inviter.send(outPacket);
		invitee.send(outPacket);
	}

	/**
	 * Packet that party members receive when a new player joins the party
	 */
	private void sendJoinedParty(GamePlayer partyMember, GamePlayer invitee) //party members receiving new player
	{
		partyMember.send(new PartyUpdatedResponse.PlayerAdded(invitee));
	}

	/**
	 * Packets that a player receives when they join a new party
	 */
	@SuppressWarnings("keyfor")
	private void sendJoinParty(Party party, GamePlayer invitee, GamePlayer inviter) //player joining party
	{
		invitee.send(new PartyCreatedResponse.Created(inviter.name()));
		invitee.send(new PartyLeaderResponse(party.leader.id()));
		invitee.send(new PartyUpdatedResponse.PlayerAdded(party.partyPlayers.keySet()));
	}
}
