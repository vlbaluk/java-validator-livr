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
package test.rule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import livr.FunctionKeeper;
import livr.validation.api.Rule;

/**
 * Custom test rule
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/11/17
 */
public class MyLength implements Rule {

    @Override
    public Function<List<Object>, Function<FunctionKeeper, Object>> func() {

	return ruleDefinition -> {
	    final Long maxLength = Long.valueOf(ruleDefinition.get(0) + "");

	    return wrapper -> {
		if ((wrapper.getValue() == null) || (wrapper.getValue() + "").equals("")) {
		    return "";
		}

		final String value = wrapper.getValue() + "";
		if (value.length() > maxLength) {
		    return "MY_TOO_LONG";
		}
		wrapper.getFieldResultArr().add(value);
		return "";
	    };
	};
    }

    @Override
    public String rule() {
	return "my_length";
    }

}
