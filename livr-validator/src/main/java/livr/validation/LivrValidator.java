package livr.validation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import livr.LIVR;
import livr.Validator;
import livr.validation.annotation.LivrRule;
import livr.validation.annotation.LivrSchema;

/**
 * LIVR Validator
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/09
 */
public class LivrValidator implements ConstraintValidator<LivrSchema, Object> {

	private Logger log = LoggerFactory.getLogger(LivrValidator.class);

	private Validator validator;

	private ObjectMapper objectMapper = new ObjectMapper();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void initialize(LivrSchema constraintAnnotation) {
		try {
			String schema = SchemaLoader.load(constraintAnnotation.schema());
			LivrRule[] rules = constraintAnnotation.rules();

			Map<String, Function> r = new HashMap<>();
			for (LivrRule livrRule : rules) {
				r.put(livrRule.name(), (Function<List<Object>, Function>) livrRule.func().newInstance());
			}

			validator = LIVR.validator().registerDefaultRules(r).init(schema, constraintAnnotation.autotrim());
		} catch (ParseException | InstantiationException | IllegalAccessException e) {
			log.error(e.getMessage(), e.getCause());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		try {
			JSONObject validData = validator.validate(objectMapper.writer().writeValueAsString(value));

			if (validData != null) {
				return true;
			} else {
				context.disableDefaultConstraintViolation();
				validator.getErrors().forEach((k, v) -> context.buildConstraintViolationWithTemplate((String) v)
						.addPropertyNode((String) k).addConstraintViolation());
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
		}
		return false;
	}

}
