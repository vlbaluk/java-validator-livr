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
package livr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author vladislavbaluk
 *
 * @since 2017/10/03
 */
public class FunctionKeeper {
    private Object value;

    private Map args;
    private Function<FunctionKeeper, Object> function;
    private List<Object> fieldResultArr = new ArrayList<>();
    public FunctionKeeper(Map args, Function<FunctionKeeper, Object> function) {
	this.args = args;
	this.function = function;
    }

    public Map getArgs() {
	return args;
    }

    public List<Object> getFieldResultArr() {
	return fieldResultArr;
    }

    public Function<FunctionKeeper, Object> getFunction() {
	return function;
    }

    public Object getValue() {
	return value;
    }

    public void setArgs(Map args) {
	this.args = args;
    }

    public void setFieldResultArr(List<Object> fieldResultArr) {
	this.fieldResultArr = fieldResultArr;
    }

    public void setFunction(Function<FunctionKeeper, Object> function) {
	this.function = function;
    }

    public void setValue(Object value) {
	this.value = value;
    }
}
