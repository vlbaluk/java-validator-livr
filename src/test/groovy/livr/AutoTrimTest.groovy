package livr

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

/**
 * Created by vladislavbaluk on 10/19/2017.
 */
class AutoTrimTest extends Specification {
    def "NEGATIVE: Validate data with automatic trim"() {
        when:
        def validator = LIVR.validator().init("{" +
                "                           \"code\":     \"required\"," +
                "                          \"password\": [\"required\", { \"min_length\": 3 }]," +
                "                           \"address\":  { \"nested_object\": {" +
                "                               \"street\": { \"min_length\": 5 }" +
                "                           }}" +
                "                       }", true);

        def output = validator.validate("{" +
                "                        \"code\": \"  \"," +
                "                        \"password\": \" 12  \"," +
                "                       \"address\": {" +
                "                               \"street\": \"  hell \"" +
                "                      }" +
                "                       }");
        then:

        assert output == null;
        JSONAssert.assertEquals(validator.getErrors().toJSONString(), "{" +
                "                \"code\": \"REQUIRED\"," +
                "                \"password\": \"TOO_SHORT\"," +
                "                \"address\":" +
                "                {" +
                "                \"street\": \"TOO_SHORT\"" +
                "                }" +
                "                }", false);

    }

}
