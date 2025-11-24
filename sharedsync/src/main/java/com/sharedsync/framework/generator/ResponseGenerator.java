package com.sharedsync.framework.generator;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import com.sharedsync.framework.generator.Generator.CacheInformation;

public class ResponseGenerator {
	private static final String OBJECT_NAME = "response";

	public static void initialize(CacheInformation cacheInfo) {
		String responseClassName = "W" + cacheInfo.getEntityName() + Generator.capitalizeFirst(OBJECT_NAME);
		cacheInfo.setResponseClassName(responseClassName);
		String packageName = cacheInfo.getBasicPackagePath() + "." + "W" +OBJECT_NAME;
		cacheInfo.setResponsePath(packageName);
	}

	public static boolean process(CacheInformation cacheInfo, ProcessingEnvironment processingEnv) {
		StringBuilder source = new StringBuilder();
		source.append("package ").append(cacheInfo.getResponsePath()).append(";\n\n");
		source.append("import java.util.List;\n");
		source.append("import com.sharedsync.framework.shared.framework.dto.WResponse;\n");
		source.append("import ").append(cacheInfo.getDtoPath()).append(".").append(cacheInfo.getDtoClassName()).append(";\n");
		source.append("import lombok.Getter;\n");
		source.append("import lombok.Setter;\n\n");

		source.append("@Getter\n");
		source.append("@Setter\n");
		source.append("public class ").append(cacheInfo.getResponseClassName()).append(" extends WResponse {\n");
		source.append("    private List<").append(cacheInfo.getDtoClassName()).append("> ").append(Generator.decapitalizeFirst(cacheInfo.getDtoClassName())).append(";\n");
		source.append("}\n");

		// 파일 생성
		try {
			JavaFileObject file = processingEnv.getFiler().createSourceFile(cacheInfo.getResponsePath() + "." + cacheInfo.getResponseClassName());
			Writer writer = file.openWriter();
			writer.write(source.toString());
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Failed to generate response: " + cacheInfo.getResponseClassName(), e);
		}
		return true;
	}
}
