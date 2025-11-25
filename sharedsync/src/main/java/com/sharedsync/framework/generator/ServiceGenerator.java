package com.sharedsync.framework.generator;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import com.sharedsync.framework.generator.Generator.CacheInformation;

public class ServiceGenerator {
	private static final String OBJECT_NAME = "service";

    public static void initialize(CacheInformation cacheInfo) {
        cacheInfo.setServiceClassName("Shared" + cacheInfo.getEntityName() + Generator.capitalizeFirst(OBJECT_NAME));
        cacheInfo.setServicePath(cacheInfo.getBasicPackagePath() + "." + OBJECT_NAME);
    }

	public static boolean process(CacheInformation cacheInfo, ProcessingEnvironment processingEnv) {

		StringBuilder source = new StringBuilder();
		source.append("package ").append(cacheInfo.getServicePath()).append(";\n\n");
		source.append("import org.springframework.stereotype.Service;\n");
		source.append("import lombok.RequiredArgsConstructor;\n\n");
		source.append("import ").append(cacheInfo.getEntityPath()).append(";\n");
		source.append("import ").append(cacheInfo.getRepositoryPath()).append(";\n");
		source.append("import ").append(cacheInfo.getRequestPath()).append(".").append(cacheInfo.getRequestClassName()).append(";\n");
		source.append("import ").append(cacheInfo.getResponsePath()).append(".").append(cacheInfo.getResponseClassName()).append(";\n");
		source.append("import ").append(cacheInfo.getCachePath()).append(".").append(cacheInfo.getCacheClassName()).append(";\n");
		source.append("import ").append(cacheInfo.getDtoPath()).append(".").append(cacheInfo.getDtoClassName()).append(";\n");
		source.append("import ").append("com.sharedsync.framework.shared.service.SharedService").append(";\n\n");

		source.append("@Service\n");
		source.append("@RequiredArgsConstructor\n");
		source.append("public class ").append(cacheInfo.getServiceClassName())
			.append(" implements SharedService<")
			.append(cacheInfo.getRequestClassName()).append(", ")
			.append(cacheInfo.getResponseClassName()).append("> {\n\n");
		source.append("    private final ").append(cacheInfo.getCacheClassName()).append(" ").append(decapitalizeFirst(cacheInfo.getCacheClassName())).append(";\n\n");

		// create
		source.append("    public ").append(cacheInfo.getResponseClassName()).append(" create(").append(cacheInfo.getRequestClassName()).append(" request) {\n");
		source.append("        ").append(cacheInfo.getResponseClassName()).append(" response = new ").append(cacheInfo.getResponseClassName()).append("();\n");
		source.append("        java.util.List<").append(cacheInfo.getDtoClassName()).append("> payload = request.get").append(cacheInfo.getDtoClassName()).append("() != null ? request.get").append(cacheInfo.getDtoClassName()).append("() : java.util.Collections.emptyList();\n");
		source.append("        if (payload.isEmpty()) {\n");
		source.append("            response.set").append(cacheInfo.getDtoClassName()).append("(java.util.Collections.emptyList());\n");
		source.append("            return response;\n");
		source.append("        }\n");
		source.append("        java.util.List<").append(cacheInfo.getDtoClassName()).append("> sanitized = payload.stream()\n");
		source.append("            .map(dto -> dto.").append("<").append(cacheInfo.getDtoClassName()).append(">").append("changeId(null))\n");
		source.append("            .collect(java.util.stream.Collectors.toList());\n");
		source.append("        java.util.List<").append(cacheInfo.getDtoClassName()).append("> saved = ").append(decapitalizeFirst(cacheInfo.getCacheClassName())).append(".saveAll(sanitized);\n");
		source.append("        response.set").append(cacheInfo.getDtoClassName()).append("(saved);\n");
		source.append("        return response;\n");
		source.append("    }\n\n");

		// read
		source.append("    public ").append(cacheInfo.getResponseClassName()).append(" read(").append(cacheInfo.getRequestClassName()).append(" request) {\n");
		source.append("        ").append(cacheInfo.getResponseClassName()).append(" response = new ").append(cacheInfo.getResponseClassName()).append("();\n");
		source.append("        java.util.List<").append(cacheInfo.getDtoClassName()).append("> payload = request.get").append(cacheInfo.getDtoClassName()).append("() != null ? request.get").append(cacheInfo.getDtoClassName()).append("() : java.util.Collections.emptyList();\n");
		source.append("        if (payload.isEmpty()) {\n");
		source.append("            response.set").append(cacheInfo.getDtoClassName()).append("(java.util.Collections.emptyList());\n");
		source.append("            return response;\n");
		source.append("        }\n");
		source.append("        java.util.List<Integer> ids = payload.stream()\n");
		source.append("            .map(dto -> dto.getId())\n");
		source.append("            .filter(java.util.Objects::nonNull)\n");
		source.append("            .collect(java.util.stream.Collectors.toList());\n");
		source.append("        if (ids.isEmpty()) {\n");
		source.append("            response.set").append(cacheInfo.getDtoClassName()).append("(java.util.Collections.emptyList());\n");
		source.append("            return response;\n");
		source.append("        }\n");
		source.append("        java.util.List<").append(cacheInfo.getEntityName()).append("> entities = ").append(decapitalizeFirst(cacheInfo.getCacheClassName())).append(".findAllById(ids);\n");
		source.append("        java.util.List<").append(cacheInfo.getDtoClassName()).append("> dtos = entities.stream()\n");
		source.append("            .filter(java.util.Objects::nonNull)\n");
		source.append("            .map(").append(cacheInfo.getDtoClassName()).append("::fromEntity)\n");
		source.append("            .collect(java.util.stream.Collectors.toList());\n");
		source.append("        response.set").append(cacheInfo.getDtoClassName()).append("(dtos);\n");
		source.append("        return response;\n");
		source.append("    }\n\n");

		// update
		source.append("    public ").append(cacheInfo.getResponseClassName()).append(" update(").append(cacheInfo.getRequestClassName()).append(" request) {\n");
		source.append("        ").append(cacheInfo.getResponseClassName()).append(" response = new ").append(cacheInfo.getResponseClassName()).append("();\n");
		source.append("        java.util.List<").append(cacheInfo.getDtoClassName()).append("> payload = request.get").append(cacheInfo.getDtoClassName()).append("() != null ? request.get").append(cacheInfo.getDtoClassName()).append("() : java.util.Collections.emptyList();\n");
		source.append("        if (payload.isEmpty()) {\n");
		source.append("            response.set").append(cacheInfo.getDtoClassName()).append("(java.util.Collections.emptyList());\n");
		source.append("            return response;\n");
		source.append("        }\n");
		source.append("        java.util.List<").append(cacheInfo.getDtoClassName()).append("> updated = payload.stream()\n");
		source.append("            .map(").append(decapitalizeFirst(cacheInfo.getCacheClassName())).append("::update)\n");
		source.append("            .collect(java.util.stream.Collectors.toList());\n");
		source.append("        response.set").append(cacheInfo.getDtoClassName()).append("(updated);\n");
		source.append("        return response;\n");
		source.append("    }\n\n");

		// delete
		source.append("    public ").append(cacheInfo.getResponseClassName()).append(" delete(").append(cacheInfo.getRequestClassName()).append(" request) {\n");
		source.append("        ").append(cacheInfo.getResponseClassName()).append(" response = new ").append(cacheInfo.getResponseClassName()).append("();\n");
		source.append("        java.util.List<").append(cacheInfo.getDtoClassName()).append("> payload = request.get").append(cacheInfo.getDtoClassName()).append("() != null ? request.get").append(cacheInfo.getDtoClassName()).append("() : java.util.Collections.emptyList();\n");
		source.append("        if (payload.isEmpty()) {\n");
		source.append("            response.set").append(cacheInfo.getDtoClassName()).append("(java.util.Collections.emptyList());\n");
		source.append("            return response;\n");
		source.append("        }\n");
		source.append("        java.util.List<Integer> ids = payload.stream()\n");
		source.append("            .map(dto -> dto.getId())\n");
		source.append("            .filter(java.util.Objects::nonNull)\n");
		source.append("            .collect(java.util.stream.Collectors.toList());\n");
		source.append("        if (!ids.isEmpty()) {\n");
		source.append("            ").append(decapitalizeFirst(cacheInfo.getCacheClassName())).append(".deleteAllById(ids);\n");
		source.append("        }\n");
		source.append("        response.set").append(cacheInfo.getDtoClassName()).append("(payload);\n");
		source.append("        return response;\n");
		source.append("    }\n\n");

		source.append("}\n");

		// 파일 생성
		try {
			JavaFileObject file = processingEnv.getFiler().createSourceFile(cacheInfo.getServicePath() + "." + cacheInfo.getServiceClassName());
			Writer writer = file.openWriter();
			writer.write(source.toString());
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Failed to generate service: " + cacheInfo.getServiceClassName(), e);
		}
		return true;
	}

	private static String decapitalizeFirst(String str) {
		if (str == null || str.isEmpty()) return "";
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}

	private static String generateCrudMethod(String action, CacheInformation cacheInfo) {
		String requestType = cacheInfo.getRequestClassName();
		String responseType = cacheInfo.getResponseClassName();
		StringBuilder sb = new StringBuilder();
		sb.append("    public ").append(responseType).append(" ").append(action)
		  .append("(").append(requestType).append(" request) {\n");
		sb.append("        // TODO: implement ").append(action).append(" logic\n");
		sb.append("        return new ").append(responseType).append("();\n");
		sb.append("    }\n");
		return sb.toString();
	}
}
