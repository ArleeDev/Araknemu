package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.InviteAcceptRequest;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class InviteAccept implements PacketHandler<GameSession, InviteAcceptRequest>
{
	private final PartyService partyService;

	public InviteAccept(PartyService partyService)
	{
		this.partyService = partyService;
	}

	@Override
	public void handle(GameSession session, InviteAcceptRequest inPacket) throws Exception
	{
		GamePlayer invitee = NullnessUtil.castNonNull(session.player());
		partyService.inviteAccept(invitee);
	}

	@Override
	public Class<InviteAcceptRequest> packet()
	{
		return InviteAcceptRequest.class;
	}
}