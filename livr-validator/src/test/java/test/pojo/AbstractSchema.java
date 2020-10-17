package test.pojo;

/**
 * Base test schema
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/17
 */
public abstract class AbstractSchema {

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
