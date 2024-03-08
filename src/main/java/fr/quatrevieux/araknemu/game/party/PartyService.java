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
import fr.quatrevieux.araknemu.game.listener.party.UpdatePartyOnLifeChanged;
import fr.quatrevieux.araknemu.game.listener.party.UpdatePartyOnStatsChanged;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.out.info.InfoCompassResponse;
import fr.quatrevieux.araknemu.network.game.out.info.InfoCoordinateHighlightPlayerResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyCreatedResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyFollowResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyLeaderResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyLeaveResponse;
import fr.quatrevieux.araknemu.network.game.out.party.PartyUpdatedResponse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.util.NullnessUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
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
        final Party party = getIfContainsKey(inviter).get();
        party.add(invitee, inviter);
        registerListeners(invitee);
    }

    /**
     * Validates PartyLeave. If validated: player leaves party
     */
    public void leave(GamePlayer player) {
        if (getIfContainsKey(player).isEmpty()) {
            throw new RuntimeException("Player(" + player.name() + ") attempting to leave party but not in party");
        }

        final Party party = getIfContainsKey(player).get();
        party.removeIfContains(player, player).get().ifEmpty(() -> parties.remove(party));
    }

    /**
     * Validates PartyLeave. If validated: kicker kicks leaver from party
     */
    public void kick(GamePlayer kicker, int leaveId) throws RuntimeException {
        if (getIfContainsKey(kicker).isEmpty()) {
            throw new RuntimeException("Player(" + kicker.name() + ") attempting to leave party but not in party");
        }
        final Party party = getIfContainsKey(kicker).get();
        if (!party.leader.equals(kicker)) {
            throw new RuntimeException("Player(" + kicker.name() + ") attempting to kick is not the party's leader");
        }
        if (party.getIfContains(leaveId).isEmpty()) {
            throw new RuntimeException("target not found in requestor's party");
        }

        final GamePlayer leaver = party.getIfContains(leaveId).get();
        party.removeIfContains(leaver, kicker).get().ifEmpty(() -> parties.remove(party));
    }

    /**
     * Validates PartyFollow. If validated: creates a listener that updates the requestor with the target's position whenever they load a new map
     */
    public void startFollow(GamePlayer requestor, int targetId) {
        validateFollow(requestor, targetId, false, "startFollow");

        final Party party = getIfContainsKey(requestor).get();
        final GamePlayer target = party.getIfContains(targetId).get();
        final Set<GamePlayer> followers = party.partyPlayers.get(target);

        if (followers != null) {
            followers.add(requestor);
            if (followers.size() == 1) {
                registerFollowListener(target);
            }
            requestor.send(new PartyFollowResponse.Start(target.id()));
        }
    }

    /**
     * Validates follow requests
     */
    private void validateFollow(GamePlayer requestor, int targetId, boolean requiresLeader, String label) throws IllegalStateException {
        if (getIfContainsKey(requestor).isEmpty()) {
            throw new IllegalStateException("Player(" + requestor.name() + ") attempting " + label + " but not member of a party");
        }
        final Party party = getIfContainsKey(requestor).get();
        if (requiresLeader && !party.leader.equals(requestor)) {
            throw new IllegalStateException("Player(" + requestor.name() + ") attempting " + label + " is not the party's leader");
        }
        if (party.getIfContains(targetId).isEmpty()) {
            requestor.send(new PartyFollowResponse.FailedNotGrouped());
            throw new IllegalStateException("Player(" + requestor.name() + ") attempting " + label + " but target is not in their party");
        }
    }

    /**
     * Validates PartyFollow. If validated: requestor stops following target. If target does not have followers anymore, unregisters the listener
     */
    public void stopFollow(GamePlayer requestor, int targetId) {
        validateFollow(requestor, targetId, false, "stopFollow");

        final Party party = getIfContainsKey(requestor).get();
        final GamePlayer target = party.getIfContains(targetId).get();
        final Set<GamePlayer> followers = NullnessUtil.castNonNull(party.partyPlayers.get(target));

        followers.remove(requestor);
        if (followers.isEmpty()) {
            unregisterFollowListenerIfHas(target);
        }
        requestor.send(new PartyFollowResponse.Stop());
    }

    /**
     * Validates PartyFollowAll. If validated: makes all partymembers (except the target) follow the target
     */
    public void startFollowAll(GamePlayer requestor, int targetId) {
        validateFollow(requestor, targetId, true, "startFollowAll");

        final Party party = getIfContainsKey(requestor).get();
        final GamePlayer target = party.getIfContains(targetId).get();
        party.partyPlayers.keySet().stream().filter(player -> !player.equals(target)).forEach(player -> startFollow(player, targetId));
    }

    /**
     * Validates PartyFollowAll. If validated: makes all partymembers (except the target) stop following the target
     */
    public void stopFollowAll(GamePlayer requestor, int targetId) {
        validateFollow(requestor, targetId, true, "stopFollowAll");

        final Party party = getIfContainsKey(requestor).get();
        final GamePlayer target = party.getIfContains(targetId).get();
        final Set<GamePlayer> followers = NullnessUtil.castNonNull(party.partyPlayers.get(target));

        Set.copyOf(followers).forEach(gp -> stopFollow(gp, targetId));
    }

    /**
     * displays all party member (except the requestor) positions on the map
     */
    public void partyPositions(GamePlayer requestor) {
        final int requestorSuperarea = explorationMapService.load(requestor.position().map()).subArea().area().superarea();

        getIfContainsKey(requestor).ifPresentOrElse(party -> {
            final Set<GamePlayer> players = party.partyPlayers.keySet().stream().filter(p -> !p.equals(requestor)).filter(p -> explorationMapService.load(p.position().map()).subArea().area().superarea() == requestorSuperarea).collect(Collectors.toSet());
            requestor.send(new InfoCoordinateHighlightPlayerResponse(players, explorationMapService, FlagType.FLAG_MAP_GROUP));
        }, () -> {
            throw new RuntimeException("Player(" + requestor.name() + ") attempting to get party positions but not in party");
        });
    }

    boolean isPartied(GamePlayer player) {
        return getIfContainsKey(player).isPresent();
    }

    Optional<Boolean> isFull(GamePlayer player) {
        return getIfContainsKey(player).map(Party::isFull);
    }

    private Optional<Party> getIfContainsKey(GamePlayer player) {
        return parties.stream().filter(party -> party.partyPlayers.containsKey(player)).findFirst();
    }

    private void registerListeners(GamePlayer player) {
        player.dispatcher().add(new LeavePartyOnDisconnect(player, this));
        player.dispatcher().add(new UpdatePartyOnStatsChanged(player, this));
        player.dispatcher().add(new UpdatePartyOnLifeChanged(player, this));
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

    /**
     * Called by followListener. When updater loads a new map, updates the (mini)maps of all followers with a flag of the updater's position
     *
     * @see FlagType
     */
    public void updateFollowers(GamePlayer updater) {
        if (getIfContainsKey(updater).isEmpty()) {
            throw new RuntimeException("Player(" + updater.name() + ") has updatefollowers listener but is not in party");
        }

        final Party party = getIfContainsKey(updater).get();
        final GamePlayer partyPlayer = party.partyPlayers.keySet().stream().filter(gp -> gp.equals(updater)).findFirst().get(); //getIfContainsKey implies updater is part of the keyset
        if (party.partyPlayers.get(partyPlayer).isEmpty()) {
            throw new RuntimeException("Player(" + updater.name() + ") has updatefollowers listener but no followers");
        }

        final Geolocation location = explorationMapService.load(partyPlayer.position().map()).geolocation();
        party.partyPlayers.get(partyPlayer).forEach(follower -> follower.send(new InfoCompassResponse(location.x(), location.y())));
    }

    /**
     * Validates request. If validated: updates the UI of all members of trigger's party
     */
    public void updatePartyUI(GamePlayer trigger) {
        if (getIfContainsKey(trigger).isEmpty()) {
            throw new RuntimeException("Player(" + trigger.name() + ") triggered updateUI but is not in a party");
        }

        final Party party = getIfContainsKey(trigger).get();
        final Set<GamePlayer> partyMembers = party.partyPlayers.keySet();

        partyMembers.forEach(pl -> pl.send(new PartyUpdatedResponse.StatsChanged(partyMembers)));
    }

    private static final class Party {
        private final Map<GamePlayer, Set<GamePlayer>> partyPlayers;
        private final GameConfiguration.PartyConfiguration configuration;
        private GamePlayer leader;

        private Party(GamePlayer inviter, GamePlayer invitee, GameConfiguration.PartyConfiguration configuration) {
            this.configuration = configuration;
            partyPlayers = new Hashtable<>();
            leader = inviter;
            add(inviter, inviter);
            add(invitee, inviter);
        }

        private void add(GamePlayer invitee, GamePlayer inviter) {
            partyPlayers.keySet().forEach(player -> player.send(new PartyUpdatedResponse.PlayerAdded(invitee)));
            partyPlayers.put(invitee, Collections.checkedSet(new HashSet<>(), GamePlayer.class));
            sendOnJoin(invitee, inviter);
        }

        private void sendOnJoin(GamePlayer invitee, GamePlayer inviter) {
            invitee.send(new PartyCreatedResponse.Created(inviter.name()));
            invitee.send(new PartyLeaderResponse(leader.id()));
            invitee.send(new PartyUpdatedResponse.PlayerAdded(Set.copyOf(partyPlayers.keySet())));
        }

        public void ifEmpty(Runnable run) {
            if (this.partyPlayers.isEmpty()) {
                run.run();
            }
        }

        private boolean isFull() {
            return partyPlayers.keySet().size() == configuration.maxSize();
        }

        private Optional<Party> removeIfContains(GamePlayer leaver, GamePlayer kicker) {
            if (!partyPlayers.containsKey(leaver)) {
                return java.util.Optional.empty();
            }
            partyPlayers.remove(leaver);
            unregisterListeners(leaver);

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

            return Optional.of(this);
        }

        private void unregisterListeners(GamePlayer player) {
            player.dispatcher().remove(UpdateFollowersOnMapLoaded.class);
            player.dispatcher().remove(LeavePartyOnDisconnect.class);
            player.dispatcher().remove(UpdatePartyOnLifeChanged.class);
            player.dispatcher().remove(UpdatePartyOnStatsChanged.class);
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
