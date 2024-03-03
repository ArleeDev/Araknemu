package fr.quatrevieux.araknemu.game.party;

import fr.quatrevieux.araknemu.core.event.DefaultListenerAggregate;
import fr.quatrevieux.araknemu.core.event.Dispatcher;
import fr.quatrevieux.araknemu.core.event.ListenerAggregate;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class PartyInvite
{
    private GamePlayer inviter;
    private GamePlayer invitee;
    private Logger logger;

    public PartyInvite(GamePlayer inviter, GamePlayer invitee, Logger logger) {
        this.inviter = inviter;
        this.invitee = invitee;
        this.logger=logger;
    }

    public GamePlayer getInviter() {
        return inviter;
    }

    public GamePlayer getInvitee() {
        return invitee;
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartyInvite that = (PartyInvite) o;
        return Objects.equals(inviter, that.inviter) && Objects.equals(invitee, that.invitee);
    }

    public Logger getLogger() {
        return logger;
    }
}
