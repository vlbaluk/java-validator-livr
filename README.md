# java-validator-livr

# NAME
LIVR.validator - Lightweight validator supporting Language Independent Validation Rules Specification (LIVR)

# SYNOPSIS
Common usage:

```java
public class Common{
    public static void main(String[] args){
        Validator validator = LIVR.validator().init("{" +
                                              "name:      'required'," +
                                              "email:     [ 'required', 'email' ]," +
                                              "gender:    { one_of : ['male', 'female'] }," +
                                              "phone:     { max_length : 10 }," +
                                              "password:  [ 'required', {min_length : 10} ]," +
                                              "password2: { equal_to_field : 'password' }" +
                                              "}",false);
        
        JSONObject validData = validator.validate(userData);
        
        if (validData!=null) {
            saveUser(validData);
        } else {
            Logger.error('errors :' + validator.getErrors());
        }
    }
}
```


You can use modifiers separately or can combine them with validation:

```java
class Alias{
    public static void main(String[] args){
        Validator validator = LIVR.validator().init("{" +
        "email: [ 'required', 'trim', 'email', 'to_lc' ]" +
        "}",false);
    }
}
```


Feel free to register your own rules:

You can use aliases(prefferable, syntax covered by the specification) for a lot of cases:

```java
class Alias{
    public static void main(String[] args){
    Validator validator = LIVR.validator().init("{"+
                                                "password: ['required', 'strong_password']"+
                                                "}",true);
    
    validator.registerAliasedRule("{" +
                                  "name: 'strong_password'," +
                                  "rules: {min_length: 6}," +
                                  "error: 'WEAK_PASSWORD'" +
                                  "}");
    }
}
```

Or you can write more sophisticated rules directly:

```java
public class CustomFunc {
    public static void main(String[] args) {
        Validator validator = LIVR.validator().init("{" +
                "password: ['required', 'strong_password']" +
                "}", true);
        Map rules = new HashMap<>();
        rules.put("my_trim", my_trim);
        validator.registerRules(rules);
    }
    
    public static Function<List<Object>, Function> my_trim = objects -> (Function<FunctionKeeper, Object>) (wrapper) -> {
        if (LIVRUtils.isNoValue(wrapper.getValue()) || wrapper.getValue().getClass() == JSONObject.class) return "";
        wrapper.getFieldResultArr().add((wrapper.getValue() + "").trim());

        return "";
    };
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

# INSTALL

#### gradle
```bash 
git clone https://github.com/vlbaluk/java-validator-livr.git
cd root_dir
gradle -Dfile.encoding=UTF-8 build
see root/build/lib
```
####
add LIVR-lib.jar to classpath of your application

# CLASS METHODS

## LIVR.validator().init(livr, isAutoTrim);
Contructor creates validator objects.
livr - validations rules. Rules description is available here - https://github.com/koorchik/LIVR


## LIVR.validator().registerAliasedDefaultRule(alias)
alias - is a plain javascript object that contains: name, rules, error (optional).

```java
public class CustomFunc {
    public static void main(String[] args) {
        LIVR.Validator.registerAliasedDefaultRule("{" +
                                                  "    name: 'valid_address'," +
                                                  "    rules: { nested_object: {" +
                                                  "        country: 'required'," +
                                                  "        city: 'required'," +
                                                  "        zip: 'positive_integer'" +
                                                  "    }}" +
                                                  "}"
                                                  );
    }
}
```

Then you can use "valid\_address" for validation:

```javascript
{
    address: 'valid_address'
}
```

You can register aliases with own errors:

```javascript
{
    name: 'adult_age'
    rules: [ 'positive_integer', { min_number: 18 } ]
    error: 'WRONG_AGE'
}
```

All rules/aliases for the validator are equal. The validator does not distinguish "required", "list\_of\_different\_objects" and "trim" rules. So, you can extend validator with any rules/alias you like.

## LIVR.validator().registerDefaultRules({"rule\_name": ruleBuilder })
ruleBuilder - is a function reference which will be called for building single rule validator.

```java
class Main {
    public static void main(String[] args) {
        Validator validator = new Validator();
        validator.registerAliasedDefaultRule("{" +
                "    name: 'valid_address'," +
                "    rules: { nested_object: {" +
                "        country: 'required'," +
                "        city: 'required'," +
                "        zip: 'positive_integer'" +
                "    }}" +
                "}"
        );
        validator.init();
    }
}
```

Then you can use "my\_rule" for validation:

```javascript
{
    name1: 'my_rule' // Call without parameters
    name2: { 'my_rule': arg1 } // Call with one parameter.
    name3: { 'my_rule': [arg1] } // Call with one parameter.
    name4: { 'my_rule': [ arg1, arg2, arg3 ] } // Call with many parameters.
}
```



All rules for the validator are equal. The validator does not distinguish "required", "list\_of\_different\_objects" and "trim" rules. So, you can extend validator with any rules you like.

## LIVR.validator().getDefaultRules();
returns object containing all default ruleBuilders for the validator. You can register new rule or update existing one with "registerRules" method.

## LIVR.util

List of usefull utils for writing your rules (see source code)

# OBJECT METHODS

## validator.validate(input)
Validates user input. On success returns validData (contains only data that has described validation rules). On error return false.

```java
class Main {
    public static void main(String[] args) {
        JSONObject validaData = validator.validate(input)
        
        if (validData!=null) {
            // use validData
        } else {
            JSONArray errors = validator.getErrors();
        }
    }
}
```

## validator.getErrors()
Returns errors object.

```javascript
{
    "field1": "ERROR_CODE",
    "field2": "ERROR_CODE",
    ...
}
```

For example:

```javascript
{
    "country":  "NOT_ALLOWED_VALUE",
    "zip":      "NOT_POSITIVE_INTEGER",
    "street":   "REQUIRED",
    "building": "NOT_POSITIVE_INTEGER"
}
```

## validator.validator().registerRules({"rule_name": ruleBuilder})

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