package fr.quatrevieux.araknemu.network.game.out.info;

/**
 * Displays the provided coordinates on the (mini)map
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/DataProcessor.as#L673">...</a>
 * <a href="https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Infos.as#L50">...</a>
 */
public class InfoCompassResponse
{
	private final int x;
	private final int y;

	public InfoCompassResponse(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString()
	{
		return "IC"+x+'|'+y;
	}
}