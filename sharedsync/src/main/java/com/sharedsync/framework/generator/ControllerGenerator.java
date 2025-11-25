package com.sharedsync.framework.generator;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import com.sharedsync.framework.generator.Generator.CacheInformation;

public class ControllerGenerator {
	private static final String OBJECT_NAME = "controller";

	public static void initialize(CacheInformation cacheInfo) {
		cacheInfo.setControllerClassName("Shared" + cacheInfo.getEntityName() + Generator.capitalizeFirst(OBJECT_NAME));
		cacheInfo.setControllerPath(cacheInfo.getBasicPackagePath() + "." + OBJECT_NAME);
    }

	public static boolean process(CacheInformation cacheInfo, ProcessingEnvironment processingEnv) {
		String entityName = cacheInfo.getEntityName();

		StringBuilder source = new StringBuilder();
		source.append("package ").append(cacheInfo.getControllerPath()).append(";\n\n");
		source.append("import org.springframework.messaging.handler.annotation.*;\n");
		source.append("import org.springframework.stereotype.Controller;\n\n");
		source.append("import com.sharedsync.framework.shared.framework.contoller.SharedContoller;\n\n");
		source.append("import ").append(cacheInfo.getRequestPath()).append(".").append(cacheInfo.getRequestClassName()).append(";\n");
		source.append("import ").append(cacheInfo.getResponsePath()).append(".").append(cacheInfo.getResponseClassName()).append(";\n");
		source.append("import ").append(cacheInfo.getControllerPath()).append(".").append(cacheInfo.getControllerClassName()).append(";\n");
		source.append("import ").append(cacheInfo.getServicePath()).append(".").append(cacheInfo.getServiceClassName()).append(";\n\n");

		source.append("@Controller\n");
		source.append("public class ").append(cacheInfo.getControllerClassName())
			.append(" extends SharedContoller<")
			.append(cacheInfo.getRequestClassName()).append(", ")
			.append(cacheInfo.getResponseClassName()).append(", ")
			.append(cacheInfo.getServiceClassName()).append("> {\n\n");

		source.append("    public ").append(cacheInfo.getControllerClassName()).append("(")
			.append(cacheInfo.getServiceClassName()).append(" service) {\n")
			.append("        super(service);\n")
			.append("    }\n\n");

		source.append(writeCrudMethods(entityName, cacheInfo.getRequestClassName(), cacheInfo.getResponseClassName()));
		source.append("}\n");

		// 파일 생성
		try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(cacheInfo.getControllerPath() + "." + cacheInfo.getControllerClassName());
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
