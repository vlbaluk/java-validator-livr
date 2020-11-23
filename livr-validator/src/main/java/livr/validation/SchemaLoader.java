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
package livr.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load schema from file or classpath
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/16
 */
public final class SchemaLoader {

    private static Logger log = LoggerFactory.getLogger(SchemaLoader.class);

    public static final String CLASSPATH_URL_PREFIX = "classpath:";

    public static final String FILE_URL_PREFIX = "file:";

    public static final String JSON_PREFIX = "{";

    public static String load(@NotNull final String schema) {
	if (schema.startsWith(JSON_PREFIX)) {
	    return schema;
	} else if (schema.startsWith(CLASSPATH_URL_PREFIX)) {

	    InputStream stream = ClassLoader
		    .getSystemResourceAsStream(schema.substring(CLASSPATH_URL_PREFIX.length(), schema.length()));
	    final InputStreamReader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
	    return new BufferedReader(in).lines().collect(Collectors.joining("\n"));
	} else if (schema.startsWith(FILE_URL_PREFIX)) {
	    try {
		return new String(
			Files.readAllBytes(Paths.get(schema.substring(FILE_URL_PREFIX.length(), schema.length()))),
			StandardCharsets.UTF_8);
	    } catch (IOException e) {
		log.warn(e.getMessage(), e.getCause());
		return "{}";
	    }
	}
	log.warn("LIVR Schema format can not recognize! Use 'classpath:', 'file:' or json object open character.");
	return "{}";
    }

    private SchemaLoader() {
	throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
