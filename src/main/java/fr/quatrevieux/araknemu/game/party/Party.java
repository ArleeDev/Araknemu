package fr.quatrevieux.araknemu.game.party;

import fr.quatrevieux.araknemu.core.event.DefaultListenerAggregate;
import fr.quatrevieux.araknemu.core.event.ListenerAggregate;
import fr.quatrevieux.araknemu.game.player.GamePlayer;

import java.util.HashSet;
import java.util.Set;

public class Party
{
    private Set<GamePlayer> playersInParty;
    private GamePlayer leader;


    public Party(GamePlayer inviter, GamePlayer invitee)
    {
        playersInParty=new HashSet<>();
        playersInParty.add(inviter);
        playersInParty.add(invitee);
        leader=inviter;
    }

    public Set<GamePlayer> getPlayersInParty() {
        return playersInParty;
    }

    public GamePlayer getLeader() {
        return leader;
    }

    public void setLeader(GamePlayer leader)
    {
        this.leader=leader;
    }
}