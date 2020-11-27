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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.json.simple.JSONObject;
import org.junit.Test;

/**
 * Rules replacement test case
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/14
 */
public class RulesReplacementTest {

	@Test
	public void testRuleChange() throws Exception {

		Validator validator = LIVR.validator();

		Map<String, Function> defaultRules = validator.getDefaultRules();
		Map<String, Function> originalRules = new HashMap<>();
		Map<String, Function> newRules = new HashMap<>();

		for (String key : defaultRules.keySet()) {
			Function ruleBuilder = defaultRules.get(key);
			originalRules.put(key, ruleBuilder);
			newRules.put(key, MyFuncClass.patchRule(key, ruleBuilder));
		}

		validator.registerDefaultRules(newRules);

		validator = validator.init("{\"name\": [\"required\"], \"phone\": { \"max_length\": 10 }}", true);
		JSONObject result = validator.validate("{\"phone\": \"123456789123456\"}");

		assertNull(result);

		assertNotNull(validator.getErrors());

		String err = "{\"phone\":{\"code\":\"TOO_LONG\",\"rule\":{\"max_length\":[10]}},\"name\":{\"code\":\"REQUIRED\",\"rule\":{\"required\":[]}}}";
		String res = JSONObject.toJSONString(validator.getErrors());

		assertEquals(err, res);

	}
}