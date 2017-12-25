package com.carrotgarden.maven.scalor;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Pattern;

public interface Util {

	static File[] fileListByRegex(File[] rootList, String regex) {
		Pattern pattern = Pattern.compile(regex);
		File[] fileList = Arrays.stream(rootList) //
				.map(root -> root.toPath()) //
				.map(root -> {
					try {
						return Files.walk(root, FileVisitOption.FOLLOW_LINKS);
					} catch (Exception e) {
						throw new RuntimeException("File system problem", e);
					}
				}) //
				.flatMap(Function.identity()) //
				.filter(path -> Files.isRegularFile(path)) //
				.filter(path -> pattern.matcher(path.toAbsolutePath().toString()).matches()) //
				.map(path -> path.toFile()) //
				.toArray(File[]::new);
		return fileList;
	}

}
