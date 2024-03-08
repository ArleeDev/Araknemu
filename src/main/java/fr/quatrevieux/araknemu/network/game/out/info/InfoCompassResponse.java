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

package fr.quatrevieux.araknemu.network.game.out.info;

/**
 * Displays the provided coordinates on the (mini)map
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/DataProcessor.as#L673">...</a>
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Infos.as#L50">...</a>
 */
public class InfoCompassResponse {
    private final int x;
    private final int y;

    public InfoCompassResponse(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "IC" + x + '|' + y;
    }
}
