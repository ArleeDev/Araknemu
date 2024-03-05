package fr.quatrevieux.araknemu.game.listener.party;

import fr.quatrevieux.araknemu.core.event.Listener;
import fr.quatrevieux.araknemu.game.exploration.event.MapJoined;
import fr.quatrevieux.araknemu.game.party.PartyService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;

public class UpdateFollowersOnMapLoaded implements Listener<MapJoined> {

	private final GamePlayer player;
	private final PartyService service;
	public UpdateFollowersOnMapLoaded(GamePlayer player, PartyService service)
	{
		this.player = player;
		this.service = service;
	}

	@Override
	public void on(MapJoined event)
	{
		service.updateFollowers(player);
	}

	@Override
	public Class<MapJoined> event()
	{
		return MapJoined.class;
	}
}