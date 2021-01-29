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
 * Copyright (c) 2017-2020 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.data.world.repository.implementation.sql;

import fr.arakne.utils.value.Colors;
import fr.quatrevieux.araknemu.core.dbal.executor.QueryExecutor;
import fr.quatrevieux.araknemu.core.dbal.repository.EntityNotFoundException;
import fr.quatrevieux.araknemu.core.dbal.repository.RepositoryException;
import fr.quatrevieux.araknemu.core.dbal.repository.RepositoryUtils;
import fr.quatrevieux.araknemu.data.transformer.Transformer;
import fr.quatrevieux.araknemu.data.transformer.TransformerException;
import fr.quatrevieux.araknemu.data.world.entity.monster.MonsterTemplate;
import fr.quatrevieux.araknemu.data.world.repository.monster.MonsterTemplateRepository;
import fr.quatrevieux.araknemu.game.world.creature.characteristics.Characteristics;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL implementation for monster template repository
 */
final class SqlMonsterTemplateRepository implements MonsterTemplateRepository {
    private class Loader implements RepositoryUtils.Loader<MonsterTemplate> {
        @Override
        public MonsterTemplate create(ResultSet rs) throws SQLException {
            return new MonsterTemplate(
                rs.getInt("MONSTER_ID"),
                rs.getString("MONSTER_NAME"),
                rs.getInt("GFXID"),
                colorsTransformer.unserialize(rs.getString("COLORS")),
                rs.getString("AI"),
                parseGrades(
                    StringUtils.splitPreserveAllTokens(rs.getString("CHARACTERISTICS"), "|"),
                    StringUtils.splitPreserveAllTokens(rs.getString("LIFE_POINTS"), "|"),
                    StringUtils.splitPreserveAllTokens(rs.getString("INITIATIVES"), "|"),
                    StringUtils.splitPreserveAllTokens(rs.getString("SPELLS"), "|")
                )
            );
        }

        @Override
        public MonsterTemplate fillKeys(MonsterTemplate entity, ResultSet keys) {
            throw new RepositoryException("Read-only entity");
        }

        private MonsterTemplate.Grade[] parseGrades(String[] characteristics, String[] lifePoints, String[] initiatives, String[] spells) {
            MonsterTemplate.Grade[] grades = new MonsterTemplate.Grade[characteristics.length];

            for (int i = 0; i < characteristics.length; ++i) {
                String[] grade = StringUtils.splitPreserveAllTokens(characteristics[i], "@", 2);

                if (grade.length != 2) {
                    throw new TransformerException("Invalid grade '" + grades[i] + "'");
                }

                Map<Integer, Integer> gradeSpells = new HashMap<>();

                for (String spell : StringUtils.split(spells[i], ";")) {
                    String[] data = StringUtils.split(spell, "@", 2);

                    if (data.length != 2) {
                        throw new TransformerException("Invalid spell list '" + spells[i] + "'");
                    }

                    gradeSpells.put(
                        Integer.parseInt(data[0]),
                        Integer.parseInt(data[1])
                    );
                }

                grades[i] = new MonsterTemplate.Grade(
                    Integer.parseInt(grade[0]),
                    Integer.parseInt(lifePoints[i]),
                    Integer.parseInt(initiatives[i]),
                    characteristicsTransformer.unserialize(grade[1]),
                    gradeSpells
                );
            }

            return grades;
        }
    }

    final private QueryExecutor executor;
    final private RepositoryUtils<MonsterTemplate> utils;
    final private Transformer<Colors> colorsTransformer;
    final private Transformer<Characteristics> characteristicsTransformer;

    public SqlMonsterTemplateRepository(QueryExecutor executor, Transformer<Colors> colorsTransformer, Transformer<Characteristics> characteristicsTransformer) {
        this.executor = executor;

        this.colorsTransformer = colorsTransformer;
        this.characteristicsTransformer = characteristicsTransformer;

        utils = new RepositoryUtils<>(this.executor, new SqlMonsterTemplateRepository.Loader());
    }

    @Override
    public void initialize() throws RepositoryException {
        try {
            executor.query(
                "CREATE TABLE `MONSTER_TEMPLATE` (" +
                    "  `MONSTER_ID` INTEGER PRIMARY KEY," +
                    "  `MONSTER_NAME` VARCHAR(100)," +
                    "  `GFXID` INTEGER," +
                    "  `COLORS` VARCHAR(30)," +
                    "  `AI` VARCHAR(12)," +
                    "  `CHARACTERISTICS` TEXT," +
                    "  `LIFE_POINTS` VARCHAR(200)," +
                    "  `INITIATIVES` VARCHAR(200)," +
                    "  `SPELLS` TEXT" +
                ")"
            );
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void destroy() throws RepositoryException {
        try {
            executor.query("DROP TABLE MONSTER_TEMPLATE");
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public MonsterTemplate get(MonsterTemplate entity) throws RepositoryException {
        return get(entity.id());
    }

    @Override
    public MonsterTemplate get(int id) {
        try {
            return utils.findOne(
                "SELECT * FROM MONSTER_TEMPLATE WHERE MONSTER_ID = ?",
                stmt -> stmt.setInt(1, id)
            );
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Monster " + id + " is not found");
        }
    }

    @Override
    public boolean has(MonsterTemplate entity) throws RepositoryException {
        return utils.aggregate(
            "SELECT COUNT(*) FROM MONSTER_TEMPLATE WHERE MONSTER_ID = ?",
            stmt -> stmt.setInt(1, entity.id())
        ) > 0;
    }

    @Override
    public List<MonsterTemplate> all() {
        return utils.findAll("SELECT * FROM MONSTER_TEMPLATE");
    }
}
