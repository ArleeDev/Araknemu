package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyInviteService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.InviteRefuseRequest;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class InviteRefuse implements PacketHandler<GameSession, InviteRefuseRequest> {

    private final PartyInviteService partyInviteService;

    public InviteRefuse(PartyInviteService partyInviteService) {
        this.partyInviteService = partyInviteService;
    }

    @Override
    public void handle(GameSession session, InviteRefuseRequest inPacket) throws Exception
    {
        GamePlayer player = NullnessUtil.castNonNull(session.player());
        partyInviteService.inviteRefuse(player);
    }


    @Override
    public Class<InviteRefuseRequest> packet() {
        return InviteRefuseRequest.class;
    }
}