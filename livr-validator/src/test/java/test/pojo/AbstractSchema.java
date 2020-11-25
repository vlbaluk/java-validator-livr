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

    private String password;

    public String getEmail() {
	return email;
    }

    public String getName() {
	return name;
    }

    public String getPassword() {
	return password;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public void setName(String name) {
	this.name = name;
    }

    public void setPassword(String password) {
	this.password = password;
    }

}
