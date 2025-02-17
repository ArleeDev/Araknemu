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

package fr.quatrevieux.araknemu.game.handler.loader;

import fr.quatrevieux.araknemu.core.di.Container;
import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.chat.ChatService;
import fr.quatrevieux.araknemu.game.exploration.ExplorationService;
import fr.quatrevieux.araknemu.game.handler.EnsureFightingOrSpectator;
import fr.quatrevieux.araknemu.game.handler.EnsureInactiveFight;
import fr.quatrevieux.araknemu.game.handler.EnsurePlaying;
import fr.quatrevieux.araknemu.game.handler.account.BoostCharacteristic;
import fr.quatrevieux.araknemu.game.handler.chat.SaveSubscription;
import fr.quatrevieux.araknemu.game.handler.chat.SendMessage;
import fr.quatrevieux.araknemu.game.handler.chat.SpamCheckAttachment;
import fr.quatrevieux.araknemu.game.handler.fight.LeaveFight;
import fr.quatrevieux.araknemu.game.handler.fight.LeaveSpectatorFight;
import fr.quatrevieux.araknemu.game.handler.game.CreateGame;
import fr.quatrevieux.araknemu.game.handler.object.MoveObject;
import fr.quatrevieux.araknemu.game.handler.object.RemoveObject;
import fr.quatrevieux.araknemu.game.handler.party.InviteAccept;
import fr.quatrevieux.araknemu.game.handler.party.InviteAsk;
import fr.quatrevieux.araknemu.game.handler.party.InviteRefuse;
import fr.quatrevieux.araknemu.game.handler.party.PartyFollow;
import fr.quatrevieux.araknemu.game.handler.party.PartyFollowAll;
import fr.quatrevieux.araknemu.game.handler.party.PartyLeave;
import fr.quatrevieux.araknemu.game.handler.party.PartyPositions;
import fr.quatrevieux.araknemu.game.handler.spell.MoveSpell;
import fr.quatrevieux.araknemu.game.handler.spell.UpgradeSpell;
import fr.quatrevieux.araknemu.game.party.PartyInviteService;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.network.game.GameSession;

/**
 * Loader for playing packets
 */
public final class PlayingLoader extends AbstractLoader {
    public PlayingLoader() {
        super(EnsurePlaying::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected PacketHandler<GameSession, ?>[] handlers(Container container) throws ContainerException {
        return new PacketHandler[] {new CreateGame(container.get(ExplorationService.class)),
                new SendMessage(container.get(ChatService.class), container.get(SpamCheckAttachment.Key.class)),
                new SaveSubscription(),
                new EnsureInactiveFight(new MoveObject()),
                new EnsureInactiveFight(new RemoveObject()),
                new EnsureInactiveFight(new BoostCharacteristic()),
                new EnsureInactiveFight(new UpgradeSpell()),
                new MoveSpell(),
                new EnsureFightingOrSpectator(new LeaveFight(), new LeaveSpectatorFight()),
                new InviteAsk(container.get(PartyInviteService.class)),
                new InviteRefuse(container.get(PartyInviteService.class)),
                new EnsureInactiveFight(new InviteAccept(container.get(PartyInviteService.class))),
                new PartyLeave(container.get(PartyService.class)),
                new PartyPositions(container.get(PartyService.class)),
                new PartyFollow(container.get(PartyService.class)),
                new PartyFollowAll(container.get(PartyService.class)), };
    }
}
