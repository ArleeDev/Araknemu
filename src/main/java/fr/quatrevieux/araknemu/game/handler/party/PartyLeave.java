package fr.quatrevieux.araknemu.game.handler.party;


import fr.quatrevieux.araknemu.core.network.exception.ErrorPacket;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.PartyLeaveRequest;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class PartyLeave implements PacketHandler<GameSession, PartyLeaveRequest> {

	private final PartyService partyService;

	public PartyLeave(PartyService partyService)
	{
		this.partyService = partyService;
	}

	@Override
	public void handle(GameSession session, PartyLeaveRequest inPacket) throws Exception
	{
		try
		{
			GamePlayer player = NullnessUtil.castNonNull(session.player());
			if(inPacket.kicked)
				partyService.kickPlayer(player, inPacket.leaveId);
			else
				partyService.leavePlayer(player);
		}
		catch (Exception e)
		{
			throw new ErrorPacket(inPacket, e);
		}
	}

	@Override
	public Class<PartyLeaveRequest> packet()
	{
		return PartyLeaveRequest.class;
	}
}