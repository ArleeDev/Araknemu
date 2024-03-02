/*
 * This file is part of Araknemu.
 *
 * Araknemu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Araknemu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Araknemu.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2017-2019 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.network.game.in.account;

import fr.quatrevieux.araknemu.core.network.parser.Packet;
import fr.quatrevieux.araknemu.core.network.parser.ParsePacketException;
import fr.quatrevieux.araknemu.core.network.parser.SinglePacketParser;
import fr.quatrevieux.araknemu.data.constant.Characteristic;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Boost one characteristic
 *
 * https://github.com/Emudofus/Dofus/blob/1.29/dofus/aks/Account.as#L111
 */
public final class AskBoost implements Packet {
    private final int playerId;
    private final Characteristic characteristic;
    private final int amount;

    public AskBoost(int playerId, Characteristic characteristic, int amount) {
        this.playerId = playerId;
        this.characteristic = characteristic;
        this.amount = amount;
    }

    public int playerId()
    {
        return playerId;
    }

    public Characteristic characteristic() {
        return characteristic;
    }

    public int amount()
    {
        return amount;
    }

    public static final class Parser implements SinglePacketParser<AskBoost> {
        @Override
        public AskBoost parse(String input) throws ParsePacketException
        {
            String[] arr = input.split(";");
            if(arr.length!=3)
                throw new RuntimeException("provided input format is invalid: "+input);

            int playerId = Integer.parseInt(arr[0]);
            Characteristic stat = Characteristic.fromId(Integer.parseInt(arr[1]));
            int amount = Integer.parseInt(arr[2]);
            return new AskBoost(playerId,stat,amount);
        }

        @Override
        public @MinLen(2) String code() {
            return "AB";
        }
    }
}
