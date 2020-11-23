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
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Test;

/**
 * Custom filters test case
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/14
 */
public class CustomFiltersTest {

    @Test
    public void registerRules() throws Exception {
	Map rules = new HashMap();
	rules.put("my_trim", MyFuncClass.my_trim);
	rules.put("my_lc", MyFuncClass.my_lc);
	rules.put("my_ucfirst", MyFuncClass.my_ucfirst);

	Validator validator = LIVR.validator().registerDefaultRules(rules).init(
		"{\"word1\": [\"my_trim\", \"my_lc\", \"my_ucfirst\"], \"word2\": [\"my_trim\", \"my_lc\"], \"word3\": [\"my_ucfirst\"] }",
		false);

	JSONObject result = validator
		.validate("{\"word1\": \" wordOne \", \"word2\": \" wordTwo \", \"word3\": \"wordThree \"}");

	String res = JSONObject.toJSONString(result);

	assertNull(validator.getErrors());
	assertEquals("{\"word1\":\"Wordone\",\"word3\":\"WordThree \",\"word2\":\"wordtwo\"}", res);
    }

}