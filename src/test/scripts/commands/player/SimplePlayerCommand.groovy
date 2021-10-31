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
 * Copyright (c) 2017-2021 Vincent Quatrevieux
 */

import fr.quatrevieux.araknemu.game.admin.AbstractCommand
import fr.quatrevieux.araknemu.game.admin.AdminPerformer
import fr.quatrevieux.araknemu.game.admin.exception.AdminException
import fr.quatrevieux.araknemu.game.player.GamePlayer

class SimplePlayerCommand extends AbstractCommand<Void> {
    public GamePlayer player

    SimplePlayerCommand(GamePlayer player) {
        this.player = player
    }

    @Override
    protected void build(Builder builder) {}

    @Override
    String name() { "sp" }

    @Override
    void execute(AdminPerformer performer, Void arguments) throws AdminException {

    }
}
