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

import fr.quatrevieux.araknemu.data.value.Geolocation;
import fr.quatrevieux.araknemu.game.GameConfiguration;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.exploration.map.FlagType;
import fr.quatrevieux.araknemu.game.listener.party.LeavePartyOnDisconnect;
import fr.quatrevieux.araknemu.game.listener.party.UpdateFollowersOnMapLoaded;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.out.info.InfoCompassResponse;
import fr.quatrevieux.araknemu.network.game.out.info.InfoCoordinateHighlightPlayerResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyCreatedResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyFollowResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyLeaderResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyLeaveResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyUpdatedResponse;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class PartyService { //TODO: return errorpackets in some cases, simplify functionchains, resolve warnings
    private final ExplorationMapService explorationMapService;
    private final GameConfiguration.PartyConfiguration configuration;
    private final Set<Party> parties = new HashSet<>();

    public PartyService(ExplorationMapService explorationMapService, GameConfiguration configuration) {
        this.explorationMapService = explorationMapService;
        this.configuration = configuration.party();
    }

    /**
     * WARNING: UNSAFE TO CALL DIRECTLY: validation happens in partyinviteservice and that should be the sole entrypoint
     * inviter creates new party and invitee joins it
     */
    void joinNew(GamePlayer inviter, GamePlayer invitee) {
        final PartyService.Party party = new Party(inviter, invitee, configuration);
        parties.add(party);
        registerListeners(inviter);
        registerListeners(invitee);
    }

    /**
     * WARNING: UNSAFE TO CALL DIRECTLY: validation happens in partyinviteservice and that should be the sole entrypoint
     * invitee joins inviter's party
     */
    void joinExisting(GamePlayer inviter, GamePlayer invitee) {
        final Party party = getIfContains(inviter).get();
        party.add(invitee, inviter);
        registerListeners(invitee);
    }

    /**
     * Validates PartyLeave. If validated: player leaves party
     */
    public void leave(GamePlayer player) {
        if (!getIfContains(player).isPresent()) {
            throw new RuntimeException("Player(" + player.name() + ") attempting to leave party but not in party");
        }

        getIfContains(player).get().removeIfContains(player, player);
        unregisterListeners(player);
    }

    /**
     * Validates PartyLeave. If validated: kicker kicks leaver from party
     */
    public void kick(GamePlayer kicker, int leaveId) throws RuntimeException {
        if (!getIfContains(kicker).isPresent()) {
            throw new RuntimeException("Player(" + kicker.name() + ") attempting to leave party but not in party");
        }

        final Party party = getIfContains(kicker).get();
        if (!party.leader.equals(kicker)) {
            throw new RuntimeException("Player(" + kicker.name() + ") attempting to kick is not the party's leader");
        }

        party.getIfContains(leaveId).ifPresentOrElse(leaver -> party.removeIfContains(leaver, kicker),
                () -> {
                    throw new RuntimeException("target not found in requestor's party");
                });
    }

    @SuppressWarnings({"assignment", "dereference.of.nullable"})
    public void startFollow(GamePlayer requestor, int targetId) {
        validateFollow(requestor, targetId, false, "startFollow");

        final Party party = getIfContains(requestor).get();
        final GamePlayer target = party.getIfContains(targetId).get();
        final Set<GamePlayer> followers = party.partyPlayers.get(target);
        followers.add(requestor);
        if (followers.size() == 1) {
            registerFollowListener(target);
        }
        requestor.send(new PartyFollowResponse.Start(target.id()));
    }

    private void validateFollow(GamePlayer requestor, int targetId, boolean requiresLeader, String label) {
        if (!getIfContains(requestor).isPresent()) {
            throw new RuntimeException("Player(" + requestor.name() + ") attempting " + label + " but not member of a party");
        }

        final Party party = getIfContains(requestor).get();
        if (requiresLeader && !party.leader.equals(requestor)) {
            throw new RuntimeException("Player(" + requestor.name() + ") attempting " + label + " is not the party's leader");
        }

        if (!party.getIfContains(targetId).isPresent()) {
            requestor.send(new PartyFollowResponse.FailedNotGrouped());
            throw new RuntimeException("Player(" + requestor.name() + ") attempting " + label + " but target is not in their party");
        }
    }

    @SuppressWarnings("dereference.of.nullable")
    public void stopFollow(GamePlayer requestor, int targetId) {
        validateFollow(requestor, targetId, false, "stopFollow");

        final Party party = getIfContains(requestor).get();
        final GamePlayer target = party.getIfContains(targetId).get();
        final Set<GamePlayer> followers = party.partyPlayers.get(target);
        followers.remove(requestor);
        if (followers.isEmpty()) {
            unregisterFollowListenerIfHas(target);
        }
        requestor.send(new PartyFollowResponse.Stop());
    }

    @SuppressWarnings("dereference.of.nullable")
    public void startFollowAll(GamePlayer requestor, int targetId) {
        validateFollow(requestor, targetId, true, "startFollowAll");

        final Party party = getIfContains(requestor).get();
        final GamePlayer target = party.getIfContains(targetId).get();
        party.partyPlayers.keySet().stream().filter(player -> !player.equals(target)).forEach(player -> startFollow(player, targetId));
    }

    @SuppressWarnings({"dereference.of.nullable", "argument"})
    public void stopFollowAll(GamePlayer requestor, int targetId) {
        validateFollow(requestor, targetId, true, "stopFollowAll");

        final Party party = getIfContains(requestor).get();
        final GamePlayer target = party.getIfContains(targetId).get();

        Set.copyOf(party.partyPlayers.get(target)).forEach(gp -> stopFollow(gp, targetId));
    }

    /**
     * displays all party member (except the requestor) positions on the map
     */
    public void partyPositions(GamePlayer requestor) {
        final int requestorSuperarea = explorationMapService.load(requestor.position().map()).subArea().area().superarea();

        getIfContains(requestor).ifPresentOrElse(party -> {
            final Set<GamePlayer> players = party.partyPlayers.keySet().stream().filter(p -> !p.equals(requestor)).filter(p -> explorationMapService.load(p.position().map()).subArea().area().superarea() == requestorSuperarea).collect(Collectors.toSet());
            requestor.send(new InfoCoordinateHighlightPlayerResponse(players, explorationMapService, FlagType.FLAG_MAP_GROUP));
        }, () -> {
            throw new RuntimeException("Player(" + requestor.name() + ") attempting to get party positions but not in party");
        });
    }

    boolean isPartied(GamePlayer player) {
        return getIfContains(player).isPresent();
    }

    Optional<Boolean> isFull(GamePlayer player) {
        return getIfContains(player).map(Party::isFull);
    }

    private Optional<Party> getIfContains(GamePlayer player) {
        return parties.stream().filter(party -> party.partyPlayers.containsKey(player)).findFirst();
    }

    private void registerListeners(GamePlayer player) {
        player.dispatcher().add(new LeavePartyOnDisconnect(player, this));
    }

    private void unregisterListeners(GamePlayer player) {
        player.dispatcher().remove(UpdateFollowersOnMapLoaded.class);
        player.dispatcher().remove(LeavePartyOnDisconnect.class);
    }

    private void registerFollowListener(GamePlayer player) {
        if (!hasFollowListener(player)) {
            player.dispatcher().add(new UpdateFollowersOnMapLoaded(player, this));
        }
    }

    private boolean hasFollowListener(GamePlayer player) {
        return player.dispatcher().get(UpdateFollowersOnMapLoaded.class) != null;
    }

    private void unregisterFollowListenerIfHas(GamePlayer player) {
        if (hasFollowListener(player)) {
            player.dispatcher().remove(UpdateFollowersOnMapLoaded.class);
        }
    }

    @SuppressWarnings("dereference.of.nullable")
    public boolean updateFollowers(GamePlayer updater) {
        return getIfContains(updater).map(party -> {
            if (!party.partyPlayers.get(updater).isEmpty()) {
                final Geolocation location = explorationMapService.load(updater.position().map()).geolocation();
                party.partyPlayers.get(updater).forEach(follower -> follower.send(new InfoCompassResponse(location.x(), location.y())));
                return true;
            }
            return false; //has no followers but asked to update them
        }).orElse(false);
    }

    private static final class Party {
        private final Map<GamePlayer, Set<GamePlayer>> partyPlayers;
        private GameConfiguration.PartyConfiguration configuration;
        private GamePlayer leader;

        private Party(GamePlayer inviter, GamePlayer invitee, GameConfiguration.PartyConfiguration configuration) {
            this.configuration = configuration;
            partyPlayers = new HashMap<>();
            leader = inviter;
            add(inviter, inviter);
            add(invitee, inviter);
        }

        private void add(GamePlayer invitee, GamePlayer inviter) {
            partyPlayers.keySet().forEach(player -> player.send(new PartyUpdatedResponse.PlayerAdded(invitee)));
            partyPlayers.put(invitee, new HashSet<>());
            sendOnJoin(invitee, inviter);
        }

        private void sendOnJoin(GamePlayer invitee, GamePlayer inviter) {
            invitee.send(new PartyCreatedResponse.Created(inviter.name()));
            invitee.send(new PartyLeaderResponse(leader.id()));
            invitee.send(new PartyUpdatedResponse.PlayerAdded(Set.copyOf(partyPlayers.keySet())));
        }

        private boolean isFull() {
            return partyPlayers.keySet().size() == configuration.maxSize();
        }

        private boolean removeIfContains(GamePlayer leaver, GamePlayer kicker) {
            if (!partyPlayers.containsKey(leaver)) {
                return false;
            }
            partyPlayers.remove(leaver);
            if (!kicker.equals(leaver)) {
                leaver.send(new PartyLeaveResponse.Kicked(kicker.name()));
            } else {
                leaver.send(new PartyLeaveResponse.Leave());
            }

            if (partyPlayers.size() == 1) { //leave but now we disband
                final GamePlayer lastPlayer = partyPlayers.keySet().iterator().next();
                partyPlayers.remove(lastPlayer);
                lastPlayer.send(new PartyLeaveResponse.Leave());
            } else if (leaver.equals(leader) && partyPlayers.size() > 1) { //leave but it was the leader
                leader = partyPlayers.keySet().iterator().next();
                partyPlayers.keySet().forEach(pl -> pl.send(new PartyLeaderResponse(leader.id())));
                partyPlayers.keySet().forEach(pl -> pl.send(new PartyUpdatedResponse.PlayerRemoved(leaver.id())));
            } else { //normal leave
                partyPlayers.keySet().forEach(pl -> pl.send(new PartyUpdatedResponse.PlayerRemoved(leaver.id())));
            }

            return true;
        }

        private Optional<GamePlayer> getIfContains(int playerid) {
            return partyPlayers.keySet().stream().filter(player -> player.id() == playerid).findFirst();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Party party = (Party) o;
            return Objects.equals(partyPlayers, party.partyPlayers) && Objects.equals(leader, party.leader);
        }
    }
}
