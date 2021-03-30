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
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import livr.rules.ExtraRules;

public class BaseFuncTest {

	private Map extraRules() {
		Map extra = new HashMap();
		extra.put("base64", ExtraRules.base64);
		extra.put("boolean", ExtraRules.boolean_rule);
		extra.put("credit_card", ExtraRules.credit_card);
		extra.put("ipv4", ExtraRules.ipv4);
		extra.put("is", ExtraRules.is);
		extra.put("iso_date", ExtraRules.iso_date);
		extra.put("list_items_unique", ExtraRules.list_items_unique);
		extra.put("list_length", ExtraRules.list_length);
		extra.put("md5", ExtraRules.md5);
		extra.put("mongo_id", ExtraRules.mongo_id);
		extra.put("required_if", ExtraRules.required_if);
		extra.put("uuid", ExtraRules.uuid);
		return extra;
	}

	@Test
	public void testNegative() throws Exception {

		List<JSONObject> jsons = IterationMethods.listFilesForFolder("negative");
		JSONParser parser = new JSONParser();

		for (JSONObject json : jsons) {

			JSONObject rules = (JSONObject) parser.parse((String) json.get("rules"));
			JSONObject input = (JSONObject) parser.parse((String) json.get("input"));

			Validator validator = LIVR.validator().registerDefaultRules(extraRules()).init(rules, false);
			validator.validate(input);

			assertNotNull(validator.getErrors());

			String err = JSONObject.toJSONString((JSONObject) parser.parse((String) json.get("errors")));
			String res = JSONObject.toJSONString(validator.getErrors());

			assertEquals(err, res);
		}

	}

	@Test
	public void testPositive() throws Exception {

		List<JSONObject> jsons = IterationMethods.listFilesForFolder("positive");
		JSONParser parser = new JSONParser();

		for (JSONObject json : jsons) {
			JSONObject rules = (JSONObject) parser.parse((String) json.get("rules"));
			JSONObject input = (JSONObject) parser.parse((String) json.get("input"));

			Validator validator = LIVR.validator().registerDefaultRules(extraRules()).init(rules, false);
			JSONObject result = validator.validate(input);

			String out = JSONObject.toJSONString((JSONObject) parser.parse((String) json.get("output")));
			String res = JSONObject.toJSONString(result);

			assertNull(validator.getErrors());
			assertEquals(out, res);
		}

	}

}
