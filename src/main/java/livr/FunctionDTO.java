package livr;

import org.json.simple.JSONObject;

import java.util.List;

/**
 * Created by vladislavbaluk on 10/6/2017.
 */
public class FunctionDTO {
    Object value;
    List<JSONObject> args;
    List<String> fieldResultArr;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<JSONObject> getArgs() {
        return args;
    }

    public void setArgs(List<JSONObject> args) {
        this.args = args;
    }

    public List<String> getFieldResultArr() {
        return fieldResultArr;
    }

    public void setFieldResultArr(List<String> fieldResultArr) {
        this.fieldResultArr = fieldResultArr;
    }
}
