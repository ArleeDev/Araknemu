package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.PartyFollowAllRequest;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public class PartyFollowAll implements PacketHandler<GameSession, PartyFollowAllRequest>
{
    private final PartyService partyService;

    public PartyFollowAll(PartyService partyService)
    {
        this.partyService = partyService;
    }

    @Override
    public void handle(GameSession session, PartyFollowAllRequest packet) throws Exception
    {
        GamePlayer requestor = NullnessUtil.castNonNull(session.player());
        if(packet.startStop=='+')
            partyService.startFollowAll(requestor,packet.targetId);
        else if(packet.startStop=='-')
            partyService.stopFollowAll(requestor,packet.targetId);
    }

    @Override
    public Class<PartyFollowAllRequest> packet()
    {
        return null;
    }
}