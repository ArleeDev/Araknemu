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
 * Copyright (c) 2017-2020 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.core.network;

import java.net.InetSocketAddress;

/**
 * Base interface for handle low level IO on socket
 */
public interface Channel {
    /**
     * Get the channel id
     */
    public Object id();

    /**
     * Write message to the channel
     *
     * @param message Message to send
     */
    public void write(Object message);

    /**
     * Close the channel
     */
    public void close();

    /**
     * Check if the channel is alive
     */
    public boolean isAlive();

    /**
     * Get the client address
     */
    public InetSocketAddress address();
}
