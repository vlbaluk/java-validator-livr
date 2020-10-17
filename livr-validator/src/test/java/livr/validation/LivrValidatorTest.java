package livr.validation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Test;

import livr.validation.annotation.LivrSchema;
import test.pojo.AbstractSchema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * LivrValidator test cases
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/15
 */
public class LivrValidatorTest {

	@LivrSchema(schema = "{\"name\": \"required\", \"email\": \"required\"}")
	public class StringSchema extends AbstractSchema {
	}

	@LivrSchema(schema = "classpath:schema.json")
	public class ClasspathSchema extends AbstractSchema {
	}

	@LivrSchema(schema = "file:/this-is-invalid-schema.json")
	public class FileSchema extends AbstractSchema {
	}

	@LivrSchema(schema = "This is invalid schema")
	public class InvalidSchema extends AbstractSchema {
	}

	@Test
	public void testSuccess() {
		StringSchema pojo = new StringSchema();
		pojo.setEmail("test@yahoo.com");
		pojo.setName("Test");

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<StringSchema>> violations = validator.validate(pojo);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testFailRequired() {
		StringSchema pojo = new StringSchema();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<StringSchema>> violations = validator.validate(pojo);
		assertFalse(violations.isEmpty());
		assertEquals(2, violations.size());
		assertEquals("REQUIRED", violations.iterator().next().getMessage());
	}

	@Test
	public void testClasspathSchemaFail() {
		ClasspathSchema pojo = new ClasspathSchema();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<ClasspathSchema>> violations = validator.validate(pojo);
		assertFalse(violations.isEmpty());
		assertEquals(2, violations.size());
		assertEquals("REQUIRED", violations.iterator().next().getMessage());
	}

	@Test
	public void testFileSchemaFail() throws IOException {
		String toWrite = "{\"name\": \"required\", \"email\": \"required\"}";
		File tmpFile = File.createTempFile("test", ".tmp");
		FileWriter writer = new FileWriter(tmpFile);
		writer.write(toWrite);
		writer.close();

		FileSchema pojo = new FileSchema();
		LivrSchema classAnnotation = pojo.getClass().getAnnotation(LivrSchema.class);
		changeAnnotationValue(classAnnotation, "schema", "file:" + tmpFile.getAbsolutePath());

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<FileSchema>> violations = validator.validate(pojo);
		assertFalse(violations.isEmpty());
		assertEquals(2, violations.size());
		assertEquals("REQUIRED", violations.iterator().next().getMessage());
	}

	@Test
	public void testInvalidSchemaIsSuccess() {
		InvalidSchema pojo = new InvalidSchema();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<InvalidSchema>> violations = validator.validate(pojo);
		assertTrue(violations.isEmpty());
	}

	/**
	 * Change annotation value
	 * 
	 * @see https://stackoverflow.com/questions/14268981/modify-a-class-definitions-annotation-string-parameter-at-runtime
	 * @author Balder
	 * @since 2015/01/23
	 */
	@SuppressWarnings("unchecked")
	public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) {
		Object handler = Proxy.getInvocationHandler(annotation);
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
		Object oldValue = memberValues.get(key);
		if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
			throw new IllegalArgumentException();
		}
		memberValues.put(key, newValue);
		return oldValue;
	}

}
