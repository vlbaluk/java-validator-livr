package livr;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Base functional test cases
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/13
 */
public class BaseFuncTest {

	@Test
	public void testPositive() throws Exception {

		List<JSONObject> jsons = IterationMethods.listFilesForFolder("positive");
		JSONParser parser = new JSONParser();

		for (JSONObject json : jsons) {
			JSONObject rules = (JSONObject) parser.parse((String) json.get("rules"));
			JSONObject input = (JSONObject) parser.parse((String) json.get("input"));

			Validator validator = LIVR.validator().init(rules, false);
			JSONObject result = validator.validate(input);

			String out = JSONObject.toJSONString((JSONObject) parser.parse((String) json.get("output")));
			String res = JSONObject.toJSONString(result);

			assertNull(validator.getErrors());
			assertEquals(out, res);
		}

	}

	@Test
	public void testNegative() throws Exception {

		List<JSONObject> jsons = IterationMethods.listFilesForFolder("negative");
		JSONParser parser = new JSONParser();

		for (JSONObject json : jsons) {
			JSONObject rules = (JSONObject) parser.parse((String) json.get("rules"));
			JSONObject input = (JSONObject) parser.parse((String) json.get("input"));

			Validator validator = LIVR.validator().init(rules, false);
			validator.validate(input);

			assertNotNull(validator.getErrors());

			String err = JSONObject.toJSONString((JSONObject) parser.parse((String) json.get("errors")));
			String res = JSONObject.toJSONString(validator.getErrors());

			assertEquals(err, res);
		}

	}

	@Test
	public void testAliasesPositive() throws Exception {
		List<JSONObject> jsons = IterationMethods.listFilesForFolder("aliases_positive");
		JSONParser parser = new JSONParser();

		for (JSONObject json : jsons) {
			JSONObject rules = (JSONObject) parser.parse((String) json.get("rules"));
			JSONObject input = (JSONObject) parser.parse((String) json.get("input"));

			JSONArray aliases = (JSONArray) parser.parse((String) json.get("aliases"));

			Validator validator = LIVR.validator().init(rules, false);

			aliases.forEach(alias -> {
				try {
					validator.registerAliasedRule((JSONObject) alias);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			JSONObject result = validator.validate(input);
			String out = JSONObject.toJSONString((JSONObject) parser.parse((String) json.get("output")));
			String res = JSONObject.toJSONString(result);

			assertNull(validator.getErrors());
			assertEquals(out, res);

		}
	}

	@Test
	public void testAliasesNegative() throws Exception {
		List<JSONObject> jsons = IterationMethods.listFilesForFolder("aliases_negative");
		JSONParser parser = new JSONParser();

		for (JSONObject json : jsons) {
			JSONObject rules = (JSONObject) parser.parse((String) json.get("rules"));
			JSONObject input = (JSONObject) parser.parse((String) json.get("input"));

			JSONArray aliases = (JSONArray) parser.parse((String) json.get("aliases"));

			Validator validator = LIVR.validator().init(rules, false);

			aliases.forEach(alias -> {
				try {
					validator.registerAliasedRule((JSONObject) alias);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			validator.validate(input);
			assertNotNull(validator.getErrors());

			String err = JSONObject.toJSONString((JSONObject) parser.parse((String) json.get("errors")));
			String res = JSONObject.toJSONString(validator.getErrors());

			assertEquals(err, res);

		}
	}

	@Test
	public void testNullCheck() throws Exception {
		Validator validator = LIVR.validator().prepare();
		JSONObject result = validator.validate(null);
		assertNull(result);
		assertNotNull(validator.getErrors());
		assertEquals("{\"base\":\"FORMAT_ERROR\"}", JSONObject.toJSONString(validator.getErrors()));
	}

}
