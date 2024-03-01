package fr.quatrevieux.araknemu.game.handler.account;

import fr.quatrevieux.araknemu.core.network.exception.ErrorPacket;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.account.AskRestat;
import fr.quatrevieux.araknemu.network.game.out.basic.Noop;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public class RestatCharacter implements PacketHandler<GameSession, AskRestat> {

    @Override
    public void handle(GameSession session, AskRestat packet) throws Exception {
        try {
            validate(session,packet);
            NullnessUtil.castNonNull(session.player())
                    .properties()
                    .characteristics()
                    .restat();
            ;
        } catch (RuntimeException e) {
            throw new ErrorPacket(new Noop(), e);
        }
    }

    @Override
    public Class<AskRestat> packet() {
        return AskRestat.class;
    }

    @SuppressWarnings("dereference.of.nullable")
    private boolean validate(GameSession session, AskRestat packet) throws Exception
    {
        if(session.player()==null)
            throw new RuntimeException("current session does not contain an active player");
        else if(session.player().id()!=packet.playerId())
            throw new RuntimeException("current session playerId does not match packet's playerId");

        return true;
    }
}

