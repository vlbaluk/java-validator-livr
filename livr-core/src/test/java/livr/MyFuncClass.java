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
package livr;

import java.util.List;
import java.util.function.Function;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by vladislavbaluk on 10/19/2017.
 */
public final class MyFuncClass {

	public static Function<List<Object>, Function> my_trim = objects -> (Function<FunctionKeeper, Object>) (
			wrapper) -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class)
			return "";
		wrapper.getFieldResultArr().add((wrapper.getValue() + "").trim());

		return "";
	};

	public static Function<List<Object>, Function> my_lc = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class)
			return "";
		wrapper.getFieldResultArr().add((wrapper.getValue() + "").toLowerCase());

		return "";
	};

	public static Function<List<Object>, Function> my_ucfirst = objects -> (Function<FunctionKeeper, Object>) (
			wrapper) -> {
		if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class)
			return "";
		wrapper.getFieldResultArr()
				.add((wrapper.getValue() + "").substring(0, 1).toUpperCase() + (wrapper.getValue() + "").substring(1));

		return "";
	};

	public static Function patchRule(String ruleName, Function ruleBuilder) {

		return arguments -> {
			Function ruleValidator = (Function) ruleBuilder.apply(arguments);
			Object ruleArgs1 = null;
			if (((List<Object>) arguments).size() == 2)
				ruleArgs1 = ((List<Object>) arguments).get(0);
			JSONArray ruleArgs = new JSONArray();
			if (ruleArgs1 != null)
				ruleArgs.add(ruleArgs1);

			return (Function<FunctionKeeper, Object>) q -> {
				Object errorCode = ruleValidator.apply(q);
				if (errorCode != null) {
					JSONObject rule = new JSONObject();
					rule.put(ruleName, ruleArgs);
					JSONObject json = new JSONObject();
					json.put("code", errorCode);
					json.put("rule", rule);
					return json;
				}
				return "";
			};
		};
	}
}
