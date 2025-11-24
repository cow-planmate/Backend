package com.sharedsync.framework.generator;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import com.sharedsync.framework.generator.Generator.CacheInformation;

public class ControllerGenerator {
	private static final String OBJECT_NAME = "controller";

	public static boolean process(CacheInformation cacheInfo, ProcessingEnvironment processingEnv) {
		String entityName = cacheInfo.getEntityName();
		String controllerName = "Shared" + entityName + Generator.capitalizeFirst(OBJECT_NAME);
		String serviceName = "Shared" + entityName + "Service";
		String dtoRequestName = "W" + entityName + "Request";
		String dtoResponseName = "W" + entityName + "Response";
		String packageName = cacheInfo.getBasicPackagePath() + "." + OBJECT_NAME;
		String servicePath = cacheInfo.getBasicPackagePath() + ".service." + serviceName;
		String dtoRequestPath = cacheInfo.getBasicPackagePath() + ".dto." + dtoRequestName;
		String dtoResponsePath = cacheInfo.getBasicPackagePath() + ".dto." + dtoResponseName;
		String baseController = "com.sharedsync.framework.shared.framework.contoller.SharedContoller";

		StringBuilder source = new StringBuilder();
		source.append("package ").append(packageName).append(";\n\n");
		source.append("import org.springframework.messaging.handler.annotation.*;\n");
		source.append("import org.springframework.stereotype.Controller;\n\n");
		source.append("import ").append(dtoRequestPath).append(";\n");
		source.append("import ").append(dtoResponsePath).append(";\n");
		source.append("import ").append(baseController).append(";\n");
		source.append("import ").append(servicePath).append(";\n\n");

		source.append("@Controller\n");
		source.append("public class ").append(controllerName)
			.append(" extends SharedContoller<")
			.append(dtoRequestName).append(", ")
			.append(dtoResponseName).append(", ")
			.append(serviceName).append("> {\n\n");

		source.append("    public ").append(controllerName).append("(")
			.append(serviceName).append(" service) {\n")
			.append("        super(service);\n")
			.append("    }\n\n");

		source.append(writeCrudMethods(entityName, dtoRequestName, dtoResponseName));

		source.append("}\n");

		// 파일 생성
		try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + controllerName);
            Writer writer = file.openWriter();
            writer.write(source.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return true;
	}

	private static String writeCrudMethods(String entityName, String dtoRequestName, String dtoResponseName) {
		StringBuilder sb = new StringBuilder();
		String lowerEntity = entityName.toLowerCase();

		sb.append(generateCrudMethod("create", lowerEntity, dtoRequestName, dtoResponseName));
		sb.append("\n");
		sb.append(generateCrudMethod("read", lowerEntity, dtoRequestName, dtoResponseName));
		sb.append("\n");
		sb.append(generateCrudMethod("update", lowerEntity, dtoRequestName, dtoResponseName));
		sb.append("\n");
		sb.append(generateCrudMethod("delete", lowerEntity, dtoRequestName, dtoResponseName));

		return sb.toString();
	}

	private static String generateCrudMethod(String action, String lowerEntity, String dtoRequestName, String dtoResponseName) {
		return "    @MessageMapping(\"/{roomId}/" + action + "/" + lowerEntity + "\")\n" +
			   "    @SendTo(\"/topic/{roomId}/" + action + "/" + lowerEntity + "\")\n" +
			   "    public " + dtoResponseName + " " + action + "(@DestinationVariable int roomId, @Payload " + dtoRequestName + " request) {\n" +
			   "        return handle" + capitalizeFirst(action) + "(roomId, request);\n" +
			   "    }\n";
	}

	private static String capitalizeFirst(String str) {
		if (str == null || str.isEmpty()) return "";
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
