# livr-validator

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=gaborkolarovics_livr-validator&metric=alert_status)](https://sonarcloud.io/dashboard?id=gaborkolarovics_livr-validator) [![Maven Central](https://img.shields.io/maven-central/v/com.github.gaborkolarovics/livr-validator.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.gaborkolarovics%22%20AND%20a:%22livr-validator%22)

# NAME
Lightweight validator supporting Language Independent Validation Rules Specification (LIVR)

# SYNOPSIS
Common usage:

```java
@LivrSchema(schema = "{\"name\": \"required\", \"email\": \"required\"}")
public class SamplePOJO{
    private String name;
    private String email;
    // Getter.. Setter..
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

#### maven
```xml
<dependency>
	<groupId>com.github.gaborkolarovics</groupId>
	<artifactId>livr-validator</artifactId>
	<version>1.1.0</version>
</dependency>
```

# AUTHOR

* Java (LIVR 2.0), maintainer vlbaluk (Vladislav Baluk)
* javax annotation, maintainer Gábor Kolárovics