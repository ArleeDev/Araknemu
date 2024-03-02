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

package fr.quatrevieux.araknemu.network.game.out.account.heroes;

import fr.quatrevieux.araknemu.game.account.AccountCharacter;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import org.checkerframework.checker.nullness.util.NullnessUtil;

import java.util.Collection;

public final class HeroToggle
{
    private final GameSession session;
    private final Collection<AccountCharacter> characters;


    public HeroToggle(GameSession session, Collection<AccountCharacter> characters)
    {
        this.session=session;
        this.characters = characters;
    }

    @Override
    public String toString() {
        //output: AEA+


        final StringBuilder sb = new StringBuilder("AEA");
        final GamePlayer player = NullnessUtil.castNonNull(session.player());
        String currentPlayerName = player.name();

        for (AccountCharacter character : characters)
        {
            sb
                    .append(character.id()).append(';')
                    .append(character.spriteInfo().name()).append(';')
                    .append(character.character().race().ordinal()).append(';')
                    .append(character.character().gender().ordinal()).append(';')
                    .append(character.spriteInfo().colors().color1()).append(';')
                    .append(character.spriteInfo().colors().color2()).append(';')
                    .append(character.spriteInfo().colors().color3()).append(';')
                    .append("null,9aa,9a9,null,null;") //spriteaccessories
                    .append(character.level()).append(';')
                    .append(currentPlayerName.equals(character.character().name()) ? "1;" : "0;") //isLeader
                    .append("0;") //hero ipdrop
                    .append("0;0;0;")  //TODO: pp, pods, init: compute if logged in
                    .append("0") //autopass
                    .append('|');
        }

        int cull=sb.length()-1;
        if(cull<0)
            throw new RuntimeException("sb invalid");

        return sb.deleteCharAt(cull).toString();
    }
}
