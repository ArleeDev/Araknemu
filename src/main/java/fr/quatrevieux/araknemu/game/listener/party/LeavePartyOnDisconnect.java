package fr.quatrevieux.araknemu.game.listener.party;

import fr.quatrevieux.araknemu.core.event.Listener;
import fr.quatrevieux.araknemu.game.handler.event.Disconnected;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;

/**
 * Leave the party when disconnect
 */
public final class LeavePartyOnDisconnect implements Listener<Disconnected> {
	private final GamePlayer player;
	private final PartyService service;
	public LeavePartyOnDisconnect(GamePlayer player, PartyService service)
	{
		this.player = player;
		this.service = service;
	}

	@Override
	public void on(Disconnected event)
	{
		service.leavePlayer(player);
		service.unregisterListeners(player);
	}

	@Override
	public Class<Disconnected> event()
	{
		return Disconnected.class;
	}
}