# java-validator-livr

# NAME
LIVR.validator - Lightweight validator supporting Language Independent Validation Rules Specification (LIVR)

# INSTALL
Published on Maven Central (Java 8+):

```
io.github.vlbaluk:validator-livr
```

Latest version: [central.sonatype.com/artifact/io.github.vlbaluk/validator-livr](https://central.sonatype.com/artifact/io.github.vlbaluk/validator-livr)

To build from source, run `gradle build` (Gradle 7.x). The jars end up in `build/libs`.

# SYNOPSIS
Common usage:

```java
import livr.LIVR;
import livr.Validator;
import org.json.simple.JSONObject;

public class Common {
    public static void main(String[] args) throws Exception {
        Validator validator = LIVR.validator().init("{"
                + "\"name\": \"required\","
                + "\"email\": [\"required\", \"email\"],"
                + "\"gender\": {\"one_of\": [\"male\", \"female\"]},"
                + "\"phone\": {\"max_length\": 10},"
                + "\"password\": [\"required\", {\"min_length\": 10}],"
                + "\"password2\": {\"equal_to_field\": \"password\"}"
                + "}", false);

        JSONObject validData = validator.validate("{"
                + "\"name\": \"John\","
                + "\"email\": \"john@example.com\","
                + "\"gender\": \"male\","
                + "\"password\": \"0123456789\","
                + "\"password2\": \"0123456789\""
                + "}");

        if (validData != null) {
            System.out.println(validData);
        } else {
            System.out.println(validator.getErrors());
        }
    }
}
```


You can use modifiers separately or can combine them with validation:

```java
Validator validator = LIVR.validator().init(
        "{\"email\": [\"required\", \"trim\", \"email\", \"to_lc\"]}", false);
```


Feel free to register your own rules:

You can use aliases(prefferable, syntax covered by the specification) for a lot of cases:

```java
Validator validator = LIVR.validator().init(
        "{\"password\": [\"required\", \"strong_password\"]}", false);

validator.registerAliasedRule(
        "{\"name\": \"strong_password\", \"rules\": {\"min_length\": 6}, \"error\": \"WEAK_PASSWORD\"}");
```

Or you can write more sophisticated rules directly. A rule builder gets the rule arguments and returns a function. That function returns `""` when the value is valid or an error code when it is not, and it can put a replacement value into `getFieldResultArr()`:

```java
import livr.FunctionKeeper;
import livr.LIVR;
import livr.LIVRUtils;
import livr.Validator;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CustomFunc {
    public static Function<List<Object>, Function> my_trim = objects -> (Function<FunctionKeeper, Object>) wrapper -> {
        if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue() instanceof JSONObject) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").trim());
        return "";
    };

    public static void main(String[] args) throws Exception {
        Validator validator = LIVR.validator().init("{\"name\": [\"required\", \"my_trim\"]}", false);

        Map<String, Function> rules = new HashMap<>();
        rules.put("my_trim", my_trim);
        validator.registerRules(rules);

        System.out.println(validator.validate("{\"name\": \"  John  \"}")); // {"name":"John"}
    }
}
```

# DESCRIPTION
See ['LIVR Specification'](http://livr-spec.org) for detailed documentation and list of supported rules.

Features:

 * Rules are declarative and language independent
 * Any number of rules for each field
 * Return together errors for all fields
 * Excludes all fields that do not have validation rules described
 * Has possibility to validatate complex hierarchical structures
 * Easy to describe and undersand rules
 * Returns understandable error codes(not error messages)
 * Easy to add own rules
 * Rules are be able to change results output ("trim", "nested\_object", for example)
 * Multipurpose (user input validation, configs validation, contracts programming etc)

# CLASS METHODS

## LIVR.validator().init(livr, isAutoTrim);
Contructor creates validator objects.
livr - validation rules, as a JSON string or a `JSONObject`. Rules description is available here - https://github.com/koorchik/LIVR


## LIVR.validator().registerAliasedDefaultRule(alias)
alias - is a `JSONObject` that contains: name, rules, error (optional).

```java
Validator validator = LIVR.validator();
validator.registerAliasedDefaultRule((JSONObject) new JSONParser().parse("{"
        + "\"name\": \"valid_address\","
        + "\"rules\": {\"nested_object\": {"
        + "\"country\": \"required\","
        + "\"city\": \"required\","
        + "\"zip\": \"positive_integer\""
        + "}}"
        + "}"));
validator.init("{\"address\": \"valid_address\"}", false);
```

Then you can use "valid\_address" for validation:

```json
{
    "address": "valid_address"
}
```

You can register aliases with own errors:

```json
{
    "name": "adult_age",
    "rules": ["positive_integer", {"min_number": 18}],
    "error": "WRONG_AGE"
}
```

All rules/aliases for the validator are equal. The validator does not distinguish "required", "list\_of\_different\_objects" and "trim" rules. So, you can extend validator with any rules/alias you like.

## LIVR.validator().registerDefaultRules({"rule\_name": ruleBuilder })
ruleBuilder - is a function reference which will be called for building single rule validator.

```java
Validator validator = LIVR.validator();
Map<String, Function> rules = new HashMap<>();
rules.put("my_rule", CustomFunc.my_trim);
validator.registerDefaultRules(rules);
validator.init("{\"name\": \"my_rule\"}", false);
```

Then you can use "my\_rule" for validation:

```javascript
{
    "name1": "my_rule",                                 // Call without parameters
    "name2": { "my_rule": "arg1" },                     // Call with one parameter
    "name3": { "my_rule": ["arg1"] },                   // Call with one parameter
    "name4": { "my_rule": ["arg1", "arg2", "arg3"] }    // Call with many parameters
}
```

The rule builder gets the argument as `objects.get(0)` (a `JSONArray` when there are several), and the map of registered rules as the last element.

All rules for the validator are equal. The validator does not distinguish "required", "list\_of\_different\_objects" and "trim" rules. So, you can extend validator with any rules you like.

## LIVR.validator().getDefaultRules();
returns object containing all default ruleBuilders for the validator. You can register new rule or update existing one with "registerRules" method.

## LIVRUtils

List of usefull utils for writing your rules (see source code)

# OBJECT METHODS

## validator.validate(input)
Validates user input, given as a JSON string or a `JSONObject`. On success returns validData (contains only data that has described validation rules). On error returns null.

```java
JSONObject validData = validator.validate(input);

if (validData != null) {
    // use validData
} else {
    JSONObject errors = validator.getErrors();
}
```

## validator.getErrors()
Returns errors object.

```javascript
{
    "field1": {
        "code": "ERROR_CODE",
        "fieldValue": "INVALID_VALUE"
    },
    "field2": {
        "code": "ERROR_CODE",
        "fieldValue": "INVALID_VALUE"
    },
    ...
}
```

For example:

```json
{
    "country": {
        "code": "NOT_ALLOWED_VALUE",
        "fieldValue": "12345"
    },
    "zip": {
        "code": "NOT_POSITIVE_INTEGER",
        "fieldValue": "-45"
    },
    "street":   {
        "code": "REQUIRED",
        "fieldValue": ""
    },
    "building": {
        "code": "NOT_POSITIVE_INTEGER",
        "fieldValue": "-123"
    }
}
```

## validator.registerRules({"rule_name": ruleBuilder})

ruleBuilder - is a function reference which will be called for building single rule validator.

See "LIVR.validator().registerDefaultRules()" for rules examples.

## validator.registerAliasedRule(alias)

alias - is a composite validation rule.

See "LIVR.validator().registerAliasedDefaultRule" for rules examples.

## validator.getRules()
returns object containing all ruleBuilders for the validator. You can register new rule or update existing one with "registerRules" method.

# AUTHOR
vlbaluk (Vladislav Baluk)

# BUGS
Please report any bugs or feature requests to Github https://github.com/vlbaluk/java-validator-livr

# LICENSE AND COPYRIGHT

Copyright 2017 Vladislav Baluk.

This program is free software; you can redistribute it and/or modify it under the terms of the the Artistic License (2.0). You may obtain a copy of the full license at:

http://www.perlfoundation.org/artistic_license_2_0

Any use, modification, and distribution of the Standard or Modified Versions is governed by this Artistic License. By using, modifying or distributing the Package, you accept this license. Do not use, modify, or distribute the Package, if you do not accept this license.

If your Modified Version has been derived from a Modified Version made by someone other than you, you are nevertheless required to ensure that your Modified Version complies with the requirements of this license.

This license does not grant you the right to use any trademark, service mark, tradename, or logo of the Copyright Holder.

This license includes the non-exclusive, worldwide, free-of-charge patent license to make, have made, use, offer to sell, sell, import and otherwise transfer the Package with respect to any patent claims licensable by the Copyright Holder that are necessarily infringed by the Package. If you institute patent litigation (including a cross-claim or counterclaim) against any party alleging that the Package constitutes direct or contributory patent infringement, then this Artistic License to you shall terminate on the date that such litigation is filed.

Disclaimer of Warranty: THE PACKAGE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS' AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES. THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT ARE DISCLAIMED TO THE EXTENT PERMITTED BY YOUR LOCAL LAW. UNLESS REQUIRED BY LAW, NO COPYRIGHT HOLDER OR CONTRIBUTOR WILL BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING IN ANY WAY OUT OF THE USE OF THE PACKAGE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Copyright 2019 Vladislav Baluk.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
