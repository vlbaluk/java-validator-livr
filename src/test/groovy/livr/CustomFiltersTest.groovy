package livr

import org.json.simple.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

/**
 * Created by vladislavbaluk on 10/19/2017.
 */
class CustomFiltersTest extends Specification {

    def "'Validate data with registered rules'"() {
        when:
        Map rules = new HashMap();
        rules.put("my_trim", MyFuncClass.my_trim);
        rules.put("my_lc", MyFuncClass.my_lc);
        rules.put("my_ucfirst", MyFuncClass.my_ucfirst);
        def validator = LIVR.validator().registerDefaultRules(rules).init("{\"word1\": [\"my_trim\", \"my_lc\", \"my_ucfirst\"]," +
                "                \"word2\": [\"my_trim\", \"my_lc\"]," +
                "                \"word3\": [\"my_ucfirst\"] }", false);

        JSONObject output = validator.validate("{" +
                "        \"word1\": \" wordOne \"," +
                "        \"word2\": \" wordTwo \"," +
                "        \"word3\": \"wordThree \"" +
                "    }");

        then:
        JSONAssert.assertEquals(output.toJSONString(), "{\n" +
                "        \"word1\": \"Wordone\",\n" +
                "        \"word2\": \"wordtwo\",\n" +
                "        \"word3\": \"WordThree \"\n" +
                "    }", false);

    }
}
