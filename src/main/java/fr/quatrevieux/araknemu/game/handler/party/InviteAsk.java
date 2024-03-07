package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.exception.ErrorPacket;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.InviteRequest;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class InviteAsk implements PacketHandler<GameSession, InviteRequest>
{

	private final PartyService partyService;

	public InviteAsk(PartyService partyService)
	{
		this.partyService = partyService;
	}

	@Override
	public void handle(GameSession session, InviteRequest inPacket) throws ErrorPacket
	{
		try
		{
			GamePlayer inviter = NullnessUtil.castNonNull(session.player());
			partyService.inviteAsk(inviter, inPacket.inviteeName);
		}
		catch(Exception e)
		{
			throw new ErrorPacket(inPacket, e);
		}
	}

	@Override
	public Class<InviteRequest> packet()
	{
		return InviteRequest.class;
	}
}