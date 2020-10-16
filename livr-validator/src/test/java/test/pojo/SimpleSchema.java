package test.pojo;

import livr.validation.annotation.LivrSchema;

/**
 * Contact POJO for testing
 *
 * @author gkolarovics
 * @since 2020/10/15
 */
@LivrSchema(schema = "{\"name\": \"required\", \"email\": \"required\"}")
public class SimpleSchema {

	private String name;

	private String email;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
