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

package fr.quatrevieux.araknemu.game.admin.executor.argument;

import fr.quatrevieux.araknemu.game.admin.Command;
import fr.quatrevieux.araknemu.game.admin.CommandParser;

/**
 * Factory for Command argument object
 */
public interface ArgumentsHydrator {
    /**
     * Hydrate the command arguments object with parsed arguments
     *
     * This method must be called only after check with {@link ArgumentsHydrator#supports(Command, Object)}
     *
     * @param commandArguments Arguments object for the command
     * @param parsedArguments Parsed arguments
     * @param <A> The command arguments type
     *
     * @return The filled command arguments
     */
    public <A> A hydrate(Command<A> command, A commandArguments, CommandParser.Arguments parsedArguments) throws Exception;

    /**
     * Check if the argument object is supported
     * May check the command type or the arguments type
     */
    public <A> boolean supports(Command<A> command, A commandArguments);
}
