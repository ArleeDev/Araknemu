package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.exception.ErrorPacket;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.PartyPositionsRequest;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class PartyPositions implements PacketHandler<GameSession, PartyPositionsRequest> {

	private final PartyService partyService;

	public PartyPositions(PartyService partyService)
	{
		this.partyService = partyService;
	}

	@Override
	public void handle(GameSession session, PartyPositionsRequest inPacket) throws Exception
	{
		try
		{
			GamePlayer requestor = NullnessUtil.castNonNull(session.player());
			partyService.partyPositions(requestor);
		}
		catch (Exception e)
		{
			throw new ErrorPacket(inPacket, e);
		}
	}

	@Override
	public Class<PartyPositionsRequest> packet()
	{
		return PartyPositionsRequest.class;
	}
}