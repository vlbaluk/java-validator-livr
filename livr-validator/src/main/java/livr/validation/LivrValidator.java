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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import livr.LIVR;
import livr.Validator;
import livr.validation.annotation.LivrSchema;
import livr.validation.api.Rule;

/**
 * LIVR Validator
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/09
 */
public class LivrValidator implements ConstraintValidator<LivrSchema, Object> {

    private Logger log = LoggerFactory.getLogger(LivrValidator.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private Validator validator;

    @SuppressWarnings("rawtypes")
    @Override
    public void initialize(final LivrSchema constraintAnnotation) {

	try {
	    final Map<String, Function> r = new HashMap<>();

	    // scanned packages rules
	    for (final String pck : populatePackageNames(constraintAnnotation)) {
		final Reflections reflections = new Reflections(pck);

		final Set<Class<? extends Rule>> annotated = reflections.getSubTypesOf(Rule.class);

		populateRuleset(r, annotated.iterator());
	    }

	    // Annotation rules
	    populateRuleset(r, Arrays.stream(constraintAnnotation.rules()).iterator());

	    final String schema = SchemaLoader.load(constraintAnnotation.schema());

	    validator = LIVR.validator().registerDefaultRules(r).init(schema, constraintAnnotation.autotrim());
	} catch (ParseException | InstantiationException | IllegalAccessException | IllegalArgumentException
		| InvocationTargetException | NoSuchMethodException | SecurityException e) {
	    log.error(e.getMessage(), e.getCause());
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
	try {
	    final JSONObject validData = validator.validate(objectMapper.writer().writeValueAsString(value));

	    if (validData != null) {
		return true;
	    } else {
		context.disableDefaultConstraintViolation();
		validator.getErrors().forEach((k, v) -> {
		    if (v instanceof String) {
			context.buildConstraintViolationWithTemplate((String) v).addPropertyNode((String) k)
				.addConstraintViolation();
		    } else {
			context.buildConstraintViolationWithTemplate(((JSONObject) v).toJSONString())
				.addPropertyNode((String) k).addConstraintViolation();
		    }
		});
	    }
	} catch (final IOException e) {
	    log.error(e.getMessage(), e.getCause());
	}
	return false;
    }

    /**
     * Get unique package names list from scanRulePackages and
     * scanRulePackageClasses attribute
     *
     * @param constraintAnnotation
     * @return
     */
    private List<String> populatePackageNames(final LivrSchema constraintAnnotation) {
	final List<String> result = new ArrayList<>();

	for (final String packageName : constraintAnnotation.scanRulePackages()) {
	    if (!result.contains(packageName)) {
		result.add(packageName);
	    }
	}
	for (final Class<?> packageClazz : constraintAnnotation.scanRulePackageClasses()) {
	    if (!result.contains(packageClazz.getName())) {
		result.add(packageClazz.getName());
	    }
	}

	return result;
    }

    /**
     * Populate rules from Rule class iterator. Rule name is unique
     * 
     * @param ruleset
     * @param it
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @SuppressWarnings("rawtypes")
    private void populateRuleset(final Map<String, Function> ruleset, Iterator<Class<? extends Rule>> it)
	    throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	while (it.hasNext()) {
	    final Rule newInstance = it.next().getDeclaredConstructor().newInstance();
	    ruleset.putIfAbsent(newInstance.rule(), newInstance.func());
	}
    }

}
