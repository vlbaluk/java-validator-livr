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

import org.junit.Test;

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
