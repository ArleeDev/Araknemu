package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.Party;
import fr.quatrevieux.araknemu.game.party.PartyInviteService;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.InviteAcceptRequest;
import fr.quatrevieux.araknemu.network.game.out.party.*;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class InviteAccept implements PacketHandler<GameSession, InviteAcceptRequest>
{

	private final PartyInviteService partyInviteService;
	private final PartyService partyService;

	public InviteAccept(PartyInviteService partyInviteService, PartyService partyService)
	{
		this.partyInviteService = partyInviteService;
		this.partyService = partyService;
	}

	@Override
	public void handle(GameSession session, InviteAcceptRequest inPacket) throws Exception
	{
		GamePlayer invitee = NullnessUtil.castNonNull(session.player());

		if(partyService.getIfContains(invitee).isPresent())	//should be unreachable unless some desync happens
			partyInviteService.getIfContains(invitee)
					.ifPresent(inv -> inv.getInviter().send(new PartyCreatedResponse.FailedAlreadyGrouped()));
		else
		{
			partyInviteService.getIfContains(invitee)
					.ifPresent(invite ->
					{
						GamePlayer inviter = invite.getInviter();
						inviter.send(new InviteResponse.Accept());

						partyService.getIfContains(inviter).ifPresentOrElse(party ->
								{
									if(partyService.isFull(party))
										invitee.send(new PartyCreatedResponse.FailedFull());
									else //inviter already in party, adding only invitee to the existing party
									{
										party.getPlayersInParty().forEach(player -> sendJoinedParty(player,invitee)); //adds the invitee to UI for party

										partyService.addMember(party, invitee);
										sendJoinParty(party, invitee, inviter);
									}
								},
								() -> //creating new party with both inviter and invitee
								{
									Party party = partyService.create(invite.getInviter(), invitee);

									sendJoinParty(party,inviter,inviter);
									sendJoinParty(party,invitee,inviter);
								}
						);
					});
		}

	}

	private void sendJoinedParty(GamePlayer partyMember, GamePlayer invitee) //party members receiving new player
	{
		partyMember.send(new PartyUpdatedResponse.PlayerAdded(invitee));
	}

	private void sendJoinParty(Party party, GamePlayer invitee, GamePlayer inviter) //player joining party
	{
		invitee.send(new PartyCreatedResponse.Created(inviter.name()));
		invitee.send(new PartyLeaderResponse(party.getLeader().id()));
		invitee.send(new PartyUpdatedResponse.PlayerAdded(party.getPlayersInParty()));
	}

	@Override
	public Class<InviteAcceptRequest> packet()
	{
		return InviteAcceptRequest.class;
	}
}