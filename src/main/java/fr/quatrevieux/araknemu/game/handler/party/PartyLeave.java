package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.exception.ErrorPacket;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.Party;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.PartyLeaveRequest;
import fr.quatrevieux.araknemu.network.game.out.party.PartyLeaderAssigned;
import fr.quatrevieux.araknemu.network.game.out.party.PartyLeft;
import fr.quatrevieux.araknemu.network.game.out.party.PartyUpdatedRemoved;
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
		GamePlayer player = NullnessUtil.castNonNull(session.player());

		if(inPacket.leaveId().isPresent()) //player kicked
		{
			partyService.getIfContains(player)
				.ifPresentOrElse(party -> {
					if(!party.getLeader().equals(session))
						throw new ErrorPacket(inPacket, new Throwable("Player attempting to kick is not the party's leader"));
					partyService.getPlayerIfContains(party,inPacket.leaveId().get())
						.ifPresentOrElse(target ->
							leaveParty(party,player,target),
							() -> {throw new ErrorPacket(inPacket, new Throwable("target not found in requestor's party"));});},
					() -> {throw new ErrorPacket(inPacket, new Throwable("requestor is not in a party"));});

		}
		else if(inPacket.leaveId().isEmpty()) //player requested self-leave
		{
			partyService.getIfContains(player)
				.ifPresentOrElse(party ->
					leaveParty(party, player, player),
					() -> {throw new ErrorPacket(inPacket, new Throwable("no party found containing player"));});
		}
	}

	private boolean leaveParty(Party party, GamePlayer kicker, GamePlayer leaver)
	{
		boolean success = leaveParty(party,leaver);

		if(success)
		{
			if (!kicker.equals(leaver))
				leaver.send(new PartyLeft(kicker.name()));
			else
				leaver.send(new PartyLeft());

			if(party.getPlayersInParty().size()==1)	//one player left, disband party
			{
				GamePlayer lastPlayer = party.getPlayersInParty().iterator().next();
				leaveParty(party,lastPlayer,lastPlayer);
			}
			else
			{
				if(party.getLeader().equals(leaver))
				{
					GamePlayer newLeader = party.getPlayersInParty().iterator().next();
					if(partyService.setLeader(party, newLeader))
						party.getPlayersInParty().forEach(pl -> pl.send(new PartyLeaderAssigned(newLeader.id())));
				}
				party.getPlayersInParty()
						.forEach(pl -> pl.send(new PartyUpdatedRemoved(leaver.id()))); //updates partyUI for remaining party members
			}
		}

		return success;
	}

	private boolean leaveParty(Party party, GamePlayer leaver)
	{
		return partyService.removePlayerIfContains(party, leaver);
	}


	@Override
	public Class<PartyLeaveRequest> packet()
	{
		return PartyLeaveRequest.class;
	}
}