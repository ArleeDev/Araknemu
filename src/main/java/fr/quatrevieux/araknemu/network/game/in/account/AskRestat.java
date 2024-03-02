package fr.quatrevieux.araknemu.network.game.in.account;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import org.checkerframework.common.value.qual.MinLen;

public class AskRestat implements Packet
{
    private final int playerId;
    public AskRestat(int playerId) {
        this.playerId = playerId;
    }

    public int playerId()
    {
        return playerId;
    }

    public static final class Parser implements SinglePacketParser<AskRestat>
    {
        @Override
        public AskRestat parse(String input) throws ParsePacketException
        {
            int playerId = Integer.parseInt(input);

            return new AskRestat(playerId);
        }

        @Override
        public @MinLen(3) String code() {
            return "Apc";
        }
    }
}
