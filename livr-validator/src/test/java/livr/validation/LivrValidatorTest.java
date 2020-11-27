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
package livr.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Test;

import livr.validation.annotation.LivrSchema;
import test.pojo.AbstractSchema;
import test.rule.MyLength;

/**
 * LivrValidator test cases
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/15
 */
public class LivrValidatorTest {

	@LivrSchema(schema = "classpath:schema.json")
	public class ClasspathSchema extends AbstractSchema {
	}

	@LivrSchema(schema = "{\"name\": \"required\", \"email\": {\"my_length\": 1 } }", rules = { MyLength.class })
	public class CustomValidatorSchema extends AbstractSchema {
	}

	@LivrSchema(schema = "file:/this-is-invalid-schema.json")
	public class FileSchema extends AbstractSchema {
	}

	@LivrSchema(schema = "This is invalid schema")
	public class InvalidSchema extends AbstractSchema {
	}

	@LivrSchema(schema = "{\"name\": \"required\", \"email\": {\"required_if\": {\"name\":\"MyName\"}}, \"password\": [\"required\", \"strong_password\"] }", scanRulePackages = {
			"test.rule", "livr.validation.rules" }, scanRulePackageClasses = { MyLength.class }, aliases = {
					"{\"name\": \"strong_password\", \"rules\": {\"min_length\": 6}, \"error\": \"WEAK_PASSWORD\"}" })
	public class StringSchema extends AbstractSchema {
	}

	/**
	 * Change annotation value
	 *
	 * @see https://stackoverflow.com/questions/14268981/modify-a-class-definitions-annotation-string-parameter-at-runtime
	 * @author Balder
	 * @since 2015/01/23
	 */
	@SuppressWarnings("unchecked")
	public static Object changeAnnotationValue(final Annotation annotation, final String key, final Object newValue) {
		final Object handler = Proxy.getInvocationHandler(annotation);
		Field f;
		try {
			f = handler.getClass().getDeclaredField("memberValues");
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException(e);
		}
		f.setAccessible(true);
		Map<String, Object> memberValues;
		try {
			memberValues = (Map<String, Object>) f.get(handler);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		final Object oldValue = memberValues.get(key);
		if ((oldValue == null) || (oldValue.getClass() != newValue.getClass())) {
			throw new IllegalArgumentException();
		}
		memberValues.put(key, newValue);
		return oldValue;
	}

	@Test
	public void testClasspathSchemaFail() {
		final ClasspathSchema pojo = new ClasspathSchema();

		final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		final Validator validator = factory.getValidator();
		final Set<ConstraintViolation<ClasspathSchema>> violations = validator.validate(pojo);
		assertFalse(violations.isEmpty());
		assertEquals(2, violations.size());
		assertEquals("REQUIRED", violations.iterator().next().getMessage());
	}

	@Test
	public void testFail_CustomRule() {
		final CustomValidatorSchema pojo = new CustomValidatorSchema();
		pojo.setEmail("test@yahoo.com");
		pojo.setName("Test");

		final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		final Validator validator = factory.getValidator();
		final Set<ConstraintViolation<CustomValidatorSchema>> violations = validator.validate(pojo);
		assertFalse(violations.isEmpty());
		assertEquals(1, violations.size());
		assertEquals("MY_TOO_LONG", violations.iterator().next().getMessage());
	}

	@Test
	public void testFailRequired() {
		final StringSchema pojo = new StringSchema();
		pojo.setPassword("1");
		pojo.setName("MyName");

		final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		final Validator validator = factory.getValidator();
		final Set<ConstraintViolation<StringSchema>> violations = validator.validate(pojo);
		assertFalse(violations.isEmpty());
		assertEquals(2, violations.size());
		Iterator<ConstraintViolation<StringSchema>> errors = violations.iterator();
		while (errors.hasNext()) {
			ConstraintViolation<StringSchema> e = errors.next();
			if ("password".equals(e.getPropertyPath().toString())) {
				assertEquals("WEAK_PASSWORD", e.getMessage());
			} else {
				assertEquals("REQUIRED", e.getMessage());
			}
		}
	}

	@Test
	public void testFileSchemaFail() throws IOException {
		final String toWrite = "{\"name\": \"required\", \"email\": \"required\"}";
		final File tmpFile = File.createTempFile("test", ".tmp");
		final FileWriter writer = new FileWriter(tmpFile);
		writer.write(toWrite);
		writer.close();

		final FileSchema pojo = new FileSchema();
		final LivrSchema classAnnotation = pojo.getClass().getAnnotation(LivrSchema.class);
		changeAnnotationValue(classAnnotation, "schema", "file:" + tmpFile.getAbsolutePath());

		final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		final Validator validator = factory.getValidator();
		final Set<ConstraintViolation<FileSchema>> violations = validator.validate(pojo);
		assertFalse(violations.isEmpty());
		assertEquals(2, violations.size());
		assertEquals("REQUIRED", violations.iterator().next().getMessage());
	}

	@Test
	public void testInvalidSchemaIsSuccess() {
		final InvalidSchema pojo = new InvalidSchema();

		final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		final Validator validator = factory.getValidator();
		final Set<ConstraintViolation<InvalidSchema>> violations = validator.validate(pojo);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testSuccess() {
		final StringSchema pojo = new StringSchema();
		pojo.setEmail("test@yahoo.com");
		pojo.setName("Test");
		pojo.setPassword("password");

		final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		final Validator validator = factory.getValidator();
		final Set<ConstraintViolation<StringSchema>> violations = validator.validate(pojo);
		assertTrue(violations.isEmpty());
	}

}
