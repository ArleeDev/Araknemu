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

package fr.quatrevieux.araknemu.game.handler.account.heroes;

import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.account.CharactersService;
import fr.quatrevieux.araknemu.game.account.GameAccount;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.account.heroes.AskHeroFillMenu;
import fr.quatrevieux.araknemu.network.game.out.account.heroes.HeroFillMenu;
import org.checkerframework.checker.nullness.util.NullnessUtil;

/**
 * Handle character creation {@link AskHeroFillMenu}
 */
public final class HandleHeroFillMenu implements PacketHandler<GameSession, AskHeroFillMenu>
{
    private final CharactersService service;

    public HandleHeroFillMenu(CharactersService service) {
        this.service = service;
    }

    @Override
    public void handle(GameSession session, AskHeroFillMenu packet) throws Exception
    {
        final GameAccount account = NullnessUtil.castNonNull(session.account());
        session.send(new HeroFillMenu(session, service.list(account)));
    }

    @Override
    public Class<AskHeroFillMenu> packet() {
        return AskHeroFillMenu.class;
    }
}
