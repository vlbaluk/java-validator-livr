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
package livr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONObject;

/**
 * Created by vladislavbaluk on 10/6/2017.
 */
public class IterationMethods {

    public static List<JSONObject> listFilesForFolder(String value) throws IOException {
	String basePath = "src/test/resources/" + value;
	List<JSONObject> jsons = new ArrayList<>();
	try (Stream<Path> paths = Files.walk(Paths.get(basePath))) {
	    List<Path> pathes = paths.filter(Files::isDirectory).collect(Collectors.toList());
	    pathes.remove(0);

	    pathes.forEach((Path dir) -> {
		try {
		    List<Path> files = Files.walk(Paths.get(basePath + "/" + dir.getFileName()))
			    .collect(Collectors.toList());
		    JSONObject jsonObject = new JSONObject();
		    jsonObject.put("testCase", dir.getFileName().toString());
		    for (Path path : files) {
			if (path.toFile().isFile()) {
			    String fileName = path.getFileName().toString().replace(".json", "");
			    jsonObject.put(fileName, new String(Files.readAllBytes(path)));
			}
		    }
		    jsons.add(jsonObject);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    });
	}
	return jsons;
    }
}
