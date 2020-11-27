/*
 * Copyright (C) 2020 Gábor KOLÁROVICS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package livr.validation.api;

import java.util.List;
import java.util.function.Function;

import livr.FunctionKeeper;

/**
 * LIVR rule interface. Implement this interface to define own rules to package
 * scan
 *
 * @author Gábor KOÁROVICSs
 * @since 2020/11/14
 */
public interface Rule {

	/**
	 * Implementation of custom rule
	 *
	 * @return Rule {@link Function}
	 */
	Function<List<Object>, Function<FunctionKeeper, Object>> func();

	/**
	 * Name of custom rule function
	 *
	 * @return name
	 */
	String rule();

}
