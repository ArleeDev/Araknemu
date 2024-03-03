package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyInviteService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.InviteRefuseRequest;
import fr.quatrevieux.araknemu.network.game.out.party.invite.InviteRefused;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class InviteRefuse implements PacketHandler<GameSession, InviteRefuseRequest> {

    private final PlayerService playerService;
    private final PartyInviteService partyInviteService;

    public InviteRefuse(PlayerService playerService, PartyInviteService partyInviteService) {
        this.playerService = playerService;
        this.partyInviteService = partyInviteService;
    }

    @Override
    public void handle(GameSession session, InviteRefuseRequest inPacket) throws Exception
    {
        GamePlayer player = NullnessUtil.castNonNull(session.player());
        partyInviteService.getIfContains(player).ifPresent(inv -> {
            inv.getInviter().send(new InviteRefused());
            inv.getInvitee().send(new InviteRefused());
            partyInviteService.remove(inv);
        });
    }


    @Override
    public Class<InviteRefuseRequest> packet() {
        return InviteRefuseRequest.class;
    }
}