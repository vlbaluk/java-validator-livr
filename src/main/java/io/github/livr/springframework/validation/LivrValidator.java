package io.github.livr.springframework.validation;

import java.io.IOException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.livr.LIVR;
import io.github.livr.Validator;
import io.github.livr.springframework.validation.annotation.LivrSchema;

/**
 * LIVR Validator
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/09
 */

public class LivrValidator implements ConstraintValidator<LivrSchema, Object> {

	private String schema;

	public void initialize(LivrSchema constraintAnnotation) {
		this.schema = constraintAnnotation.schema();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {

		ObjectMapper objectMapper = new ObjectMapper();

		try {
			Validator validator = LIVR.validator().init(schema, false);

			JSONObject validData = validator.validate(objectMapper.writer().writeValueAsString(value));

			if (validData != null) {
				return true;
			} else {
				context.disableDefaultConstraintViolation();

				validator.getErrors().forEach((k, v) -> {
					context.buildConstraintViolationWithTemplate((String) v).addPropertyNode((String) k)
							.addConstraintViolation();
				});

			}
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

}
