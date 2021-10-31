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
 * Copyright (c) 2017-2021 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.admin.executor.argument.handler;

import inet.ipaddr.IPAddressString;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;

/**
 * Parse {@link IPAddressString} value
 */
public final class IpAddressStringHandler extends OneArgumentOptionHandler<IPAddressString> {
    public IpAddressStringHandler(CmdLineParser parser, OptionDef option, Setter<? super IPAddressString> setter) {
        super(parser, option, setter);
    }

    @Override
    protected IPAddressString parse(String argument) throws NumberFormatException, CmdLineException {
        final IPAddressString ipAddress = new IPAddressString(argument);

        if (!ipAddress.isValid()) {
            throw new CmdLineException(owner, "Invalid IP address given");
        }

        return ipAddress;
    }
}
