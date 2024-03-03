package fr.quatrevieux.araknemu.network.game.out.party;

import fr.quatrevieux.araknemu.game.party.Party;

/**
 * Constructs the party menu based on the party members' stats and graphics
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Party.as#L185
 */
public class PartyUpdatedAdded
{
    private Party party;

    public PartyUpdatedAdded(Party party)
    {
        this.party = party;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("PM+");
        party.getPlayersInParty()
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
