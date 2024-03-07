package fr.quatrevieux.araknemu.network.game.out.info;

import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMap;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.exploration.map.FlagType;
import fr.quatrevieux.araknemu.game.player.GamePlayer;

import java.util.Set;

/**
 * Displays players with coordinates on (mini)map
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/DataProcessor.as#L678">...</a>
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Infos.as#L69">...</a>
 */
public class InfoCoordinateHighlightPlayerResponse {
	private final Set<GamePlayer> players;
	private final ExplorationMapService service;
	private final FlagType type;

	public InfoCoordinateHighlightPlayerResponse(Set<GamePlayer> players, ExplorationMapService service, FlagType type)
	{
		this.players = players;
		this.service=service;
		this.type = type;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("IH");
		for(GamePlayer player : players)
		{
			ExplorationMap map = service.load(player.position().map());
			sb.append(map.geolocation().x()).append(";")
			  .append(map.geolocation().y()).append(";")
			  .append(map.id()).append(";")
			  .append(type.ordinal()).append(";")
			  .append(player.id()).append(";")
			  .append(player.name()).append("|");
		}

		if(sb.length()>1&&sb.charAt(sb.length()-1)=='|')
			sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
}