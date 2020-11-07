package livr.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import livr.validation.LivrValidator;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * LIVR Schema
 * <p>
 * Attributes:
 * <ul>
 * <li>schema - LIVR validation schema</li>
 * <li>autotrim - trim values</li>
 * <li>rules - custom {@link LivrRule} array</li>
 * </ul>
 * <p>
 * <br>
 * Usage:<br>
 * <code>
 * &#64;LivrSchema(schema = "{\"name\": \"required\", \"email\": \"required\"}")<br>
 * public class LivrObj {<br>
 *     private String name;<br>
 *     private String email;<br>
 *     // Getter.. Setter..<br>
 * }
 * </code>
 * <p>
 * <br>
 * @author Gábor KOLÁROVICS
 * @since 2020/10/09
 */
@Constraint(validatedBy = LivrValidator.class)
@Target({ TYPE })
@Retention(RUNTIME)
public @interface LivrSchema {

	String message() default "Validation failed!";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String schema();

	boolean autotrim() default false;

	LivrRule[] rules() default {};

	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@interface List {
		LivrSchema[] value();
	}

}
