package fr.quatrevieux.araknemu.game.handler.party;

import fr.quatrevieux.araknemu.core.network.exception.ErrorPacket;
import fr.quatrevieux.araknemu.core.network.parser.PacketHandler;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import fr.quatrevieux.araknemu.game.party.PartyInviteService;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.party.InviteRequest;
import fr.quatrevieux.araknemu.network.game.out.party.invite.InviteFailedAlreadyGrouped;
import fr.quatrevieux.araknemu.network.game.out.party.invite.InviteFailedCantFind;
import fr.quatrevieux.araknemu.network.game.out.party.invite.InviteFailedPartyFull;
import fr.quatrevieux.araknemu.network.game.out.party.invite.Invited;
import org.checkerframework.checker.nullness.util.NullnessUtil;

public final class InviteAsk implements PacketHandler<GameSession, InviteRequest>
{

	private final PlayerService playerService;
	private final PartyInviteService partyInviteService;
	private final PartyService partyService;

	public InviteAsk(PlayerService playerService, PartyInviteService partyInviteService, PartyService partyService)
	{
		this.playerService = playerService;
		this.partyInviteService = partyInviteService;
		this.partyService = partyService;
	}

	@Override
	public void handle(GameSession session, InviteRequest inPacket) throws ErrorPacket
	{
		GamePlayer inviter = NullnessUtil.castNonNull(session.player());
		playerService.online()
				.stream()
				.filter(p -> p.name().equals(inPacket.inviteeName()))
				.findFirst()
				.ifPresentOrElse(invitee ->
				{
					if (partyService.getIfContains(invitee).isPresent())
						inviter.send(new InviteFailedAlreadyGrouped());
					else
					{
						partyService.getIfContains(inviter)
								.ifPresentOrElse(party -> {
									if (partyService.isFull(party))
										inviter.send(new InviteFailedPartyFull()); //failed attempt: inviting into existing party but the party is full
									else
									sendInvite(inviter,invitee);
								},
								() -> sendInvite(inviter,invitee));
					}
				}, () -> inviter.send(new InviteFailedCantFind(inPacket.inviteeName())));
	}

	private void sendInvite(GamePlayer inviter, GamePlayer invitee)
	{
		partyInviteService.create(inviter, invitee);
		Invited outPacket = new Invited(inviter, invitee);
		inviter.send(outPacket);
		invitee.send(outPacket);
	}

	@Override
	public Class<InviteRequest> packet()
	{
		return InviteRequest.class;
	}
}