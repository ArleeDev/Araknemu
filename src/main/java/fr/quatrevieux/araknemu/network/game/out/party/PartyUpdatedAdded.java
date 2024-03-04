package fr.quatrevieux.araknemu.network.game.out.party;

import fr.quatrevieux.araknemu.game.player.GamePlayer;

import java.util.HashSet;
import java.util.Set;

/**
 * Constructs the party menu UIElement upon adding a new party member
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L198
 */
public class PartyUpdatedAdded
{
    private Set<GamePlayer> party;

    public PartyUpdatedAdded(Set<GamePlayer> players)
    {
        this.party = players;
    }

    public PartyUpdatedAdded(GamePlayer player)
    {
        this.party = new HashSet<>();
        party.add(player);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("PM+");
        party
                .forEach(p -> builder
                        .append(p.id()).append(";")
                        .append(p.name()).append(";")
                        .append(p.race().race().ordinal()*10 + p.spriteInfo().gender().ordinal()).append(";") //maps to sprites: clientfolder/clips/sprites/(n).swf
                        .append(p.spriteInfo().colors().color1()).append(";")
                        .append(p.spriteInfo().colors().color2()).append(";")
                        .append(p.spriteInfo().colors().color3()).append(";")
                        .append(p.spriteInfo().accessories().toString()).append(";")
                        .append(p.properties().life().current()).append(";")
                        .append(p.properties().experience().level()).append(";")
                        .append(p.properties().characteristics().initiative()).append(";")
                        .append(p.properties().characteristics().discernment()).append(";") //prospecting
                        .append("0;") //"side", number, perhaps alignment?
                        .append("\\|")
                );

        if(builder.length()>1&&builder.charAt(builder.length()-1)=='|')
            builder.deleteCharAt(builder.length()-1);

        return builder.toString();
    }
}
