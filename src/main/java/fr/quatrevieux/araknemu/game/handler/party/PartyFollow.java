package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.PartyFollowRequest;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class PartyFollow implements PacketHandler<GameSession, PartyFollowRequest>
{
	private final PartyService partyService;

	public PartyFollow(PartyService partyService)
	{
		this.partyService = partyService;
	}

	@Override
	public void handle(GameSession session, PartyFollowRequest packet) throws Exception
	{
		GamePlayer requestor = NullnessUtil.castNonNull(session.player());
		if(packet.startStop=='+')
			partyService.startFollow(requestor,packet.targetId);
		else if(packet.startStop=='-')
			partyService.stopFollow(requestor,packet.targetId);
	}

	@Override
	public Class<PartyFollowRequest> packet()
	{
		return PartyFollowRequest.class;
	}
}