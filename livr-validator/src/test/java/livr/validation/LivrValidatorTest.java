package livr.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Test;

import test.pojo.SimpleSchema;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * LivrValidator test cases
 *
 * @author gkolarovics
 * @since 2020/10/15
 */
public class LivrValidatorTest {

	@Test
	public void testSuccess() {
		SimpleSchema pojo = new SimpleSchema();
		pojo.setEmail("test@yahoo.com");
		pojo.setName("Test");

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<SimpleSchema>> violations = validator.validate(pojo);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testFailRequired() {
		SimpleSchema pojo = new SimpleSchema();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<SimpleSchema>> violations = validator.validate(pojo);
		assertFalse(violations.isEmpty());
	}

}
