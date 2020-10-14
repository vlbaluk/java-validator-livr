package livr;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Auto trim test case
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/14
 */
public class AutoTrimTest {

	@Test
	public void testAutoTrim() throws Exception {

		Validator validator = LIVR.validator()
				.init("{ \"code\": \"required\", \"password\": [\"required\", { \"min_length\": 3 }], "
						+ "\"address\":  { \"nested_object\": {\"street\": { \"min_length\": 5 }}}}", true);
		validator.validate("{\"code\": \"  \", \"password\": \" 12  \", \"address\": {\"street\": \"  hell \"}}");

		assertEquals("{\"password\":\"TOO_SHORT\",\"code\":\"REQUIRED\",\"address\":{\"street\":\"TOO_SHORT\"}}",
				validator.getErrors().toJSONString());
	}

}
