package livr.validation.annotation;

import java.util.function.Function;

/**
 * Custom rule holder
 * <p>
 * Attributes:
 * <ul>
 * <li>name - Rule definition name</li>
 * <li>func - custom rule {@link Function}</li>
 * </ul>
 * <p>
 * <br>
 * Usage:<br>
 * <code>
 * &#64;LivrSchema(<br>
 * 			schema = "{\"name\": {\"my_length\": 10 } }",<br>
 * 			rules = { &#64;LivrRule(name = "my_length", func = MyLength.class) })
 * public class LivrObj {<br>
 *     private String name;<br>
 *     // Getter.. Setter..<br>
 * }<br>
 * <br>
 * public class MyLength implements Function&#60;List&#60;Object&#62;, Function&#62; {<br>
 * <br>
 *     &#64;Override<br>
 *     public Function apply(List&#60;Object&#62; objects) {<br>
 *		final Long maxLength = Long.valueOf(objects.get(0) + "");<br>
 * <br>
 *		return (Function&#60;FunctionKeeper, Object&#62;) (FunctionKeeper wrapper) -&#62; {<br>
 *			if (wrapper.getValue() == null || (wrapper.getValue() + "").equals(""))<br>
 *				return "";<br>
 *			String value = wrapper.getValue() + "";<br>
 *			if (value.length() &#62; maxLength)<br>
 *				return "MY_TOO_LONG";<br>
 *			wrapper.getFieldResultArr().add(value);<br>
 *			return "";<br>
 *		};<br>
 *	}<br>
 * <br>
 * }<br>
 * </code>
 * <p>
 * <br>
 * @author Gábor KOLÁROVICS
 * @since 2020/10/20
 */
public @interface LivrRule {

	String name();

	@SuppressWarnings("rawtypes")
	Class<? extends Function> func();

}
