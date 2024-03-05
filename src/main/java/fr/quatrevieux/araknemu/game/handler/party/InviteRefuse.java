package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyInviteService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.InviteRefuseRequest;
import fr.quatrevieux.araknemu.network.game.out.party.InviteResponse;
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
        partyInviteService.getIfContains(player).ifPresent(inv -> {
            inv.getInviter().send(new InviteResponse.Refuse());
            inv.getInvitee().send(new InviteResponse.Refuse());
            partyInviteService.remove(inv);
        });
    }


    @Override
    public Class<InviteRefuseRequest> packet() {
        return InviteRefuseRequest.class;
    }
}