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

package fr.quatrevieux.araknemu.game.exploration.interaction.action.move.validator;

import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.exploration.ExplorationPlayer;
import fr.quatrevieux.araknemu.game.exploration.interaction.action.move.Move;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMap;
import fr.quatrevieux.araknemu.game.exploration.map.cell.ExplorationMapCell;
import fr.quatrevieux.araknemu.game.world.map.Direction;
import fr.quatrevieux.araknemu.game.world.map.path.Decoder;
import fr.quatrevieux.araknemu.game.world.map.path.Path;
import fr.quatrevieux.araknemu.game.world.map.path.PathException;
import fr.quatrevieux.araknemu.game.world.map.path.PathStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidateWalkableTest extends GameBaseCase {
    private ValidateWalkable validator;
    private ExplorationMap map;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushMaps().pushSubAreas().pushAreas();
        map = explorationPlayer().map();

        validator = new ValidateWalkable();
    }

    @Test
    void onPlayerMovingWithNonWalkableCell() throws SQLException, ContainerException {
        ExplorationPlayer player = explorationPlayer();

        Move move = new Move(
            player,
            new Path<>(
                new Decoder<>(player.map()),
                Arrays.asList(
                    new PathStep(map.get(279), Direction.WEST),
                    new PathStep(map.get(278), Direction.WEST)
                )
            ),
            new PathValidator[] {new ValidateWalkable()}
        );

        Path<ExplorationMapCell> path = validator.validate(move, move.path());

        assertEquals(1, path.size());
    }

    @Test
    void onPlayerMovingWithValidPath() throws SQLException, ContainerException, PathException {
        ExplorationPlayer player = explorationPlayer();

        Move move = new Move(
            player,
            new Decoder<>(map).decode("bftdgl", map.get(279)),
            new PathValidator[] {new ValidateWalkable()}
        );

        Path<ExplorationMapCell> path = validator.validate(move, move.path());

        assertEquals("aexbftdgl", new Decoder<>(map).encode(path));
        assertEquals(map.get(395), path.get(path.size() - 1).cell());
    }
}
