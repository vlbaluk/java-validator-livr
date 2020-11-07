package test;

import java.util.List;
import java.util.function.Function;

import livr.FunctionKeeper;

/**
 * Custom max length
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/11/06
 */
@SuppressWarnings("rawtypes")
public class MyLength implements Function<List<Object>, Function> {

	@Override
	public Function apply(List<Object> objects) {
		final Long maxLength = Long.valueOf(objects.get(0) + "");

		return (Function<FunctionKeeper, Object>) (FunctionKeeper wrapper) -> {
			if (wrapper.getValue() == null || (wrapper.getValue() + "").equals(""))
				return "";

			String value = wrapper.getValue() + "";
			if (value.length() > maxLength)
				return "MY_TOO_LONG";
			wrapper.getFieldResultArr().add(value);
			return "";
		};

	}

}
