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
package livr.validation.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import livr.validation.LivrValidator;
import livr.validation.api.Rule;

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
 * 
 * @author Gábor KOLÁROVICS
 * @since 2020/10/09
 */
@Constraint(validatedBy = LivrValidator.class)
@Target({ TYPE })
@Retention(RUNTIME)
public @interface LivrSchema {

    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
	LivrSchema[] value();
    }

    boolean autotrim() default false;

    Class<?>[] groups() default {};

    String message() default "Validation failed!";

    Class<? extends Payload>[] payload() default {};

    /**
     * Custom rules definitions
     *
     * @since 1.4.0
     */
    Class<? extends Rule>[] rules() default {};

    /**
     * Type-safe alternative to {@link #scanRulePackages} for specifying the
     * packages to scan for annotated rules. The package of each class specified
     * will be scanned.
     * <p>
     * Consider creating a special no-op marker class or interface in each package
     * that serves no purpose other than being referenced by this attribute.
     *
     * @return rule packages to scan
     * @since 1.4.0
     */
    Class<?>[] scanRulePackageClasses() default {};

    /**
     * Base packages to scan for annotated rules. Use
     * {@link #scanRulePackageClasses} for a type-safe alternative to String-based
     * package names.
     *
     * @return rule packages to scan
     * @since 1.4.0
     */
    String[] scanRulePackages() default {};

    String schema();

}
