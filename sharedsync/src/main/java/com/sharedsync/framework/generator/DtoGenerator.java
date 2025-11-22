package com.sharedsync.framework.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import com.sharedsync.framework.generator.Generator.CacheInformation;
import com.sharedsync.framework.generator.Generator.FieldInfo;

public class DtoGenerator {
    private static final String OBJECT_NAME = "dto";
    public static boolean process(CacheInformation cacheInfo, ProcessingEnvironment processingEnv) {
        String dtoClassName = cacheInfo.getEntityName() + Generator.capitalizeFirst(OBJECT_NAME);
        cacheInfo.setDtoClassName(dtoClassName);
        String packageName = cacheInfo.getBasicPackagePath() + "." + OBJECT_NAME;
        cacheInfo.setDtoPath(packageName);

        String source = "package " + packageName + ";\n"
            + "import com.sharedsync.framework.shared.framework.annotation.*;\n"
            + "import lombok.*;\n"
            + "import com.sharedsync.framework.shared.framework.dto.CacheDto;\n"
            + entityPath(cacheInfo) 
            + "@Cache\n"
            + "@AllArgsConstructor\n"
            + "@NoArgsConstructor\n"
            + "@Getter\n"
            + "@Setter\n"
            + writeAutoDatabaseLoader(cacheInfo)
            + writeAutoEntityConverter(cacheInfo)
            + "public class " + dtoClassName + " extends CacheDto<" + cacheInfo.getIdType() + "> {\n\n"
            + writeDtoFields(cacheInfo)
            + writeFromEntityMethod(cacheInfo)
            + writeToEntityMethod(cacheInfo)
            + "}";


        // 파일 생성
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + dtoClassName);
            Writer writer = file.openWriter();
            writer.write(source);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return true;
    }

    private static String entityPath(CacheInformation cacheInfo){
        String path = "import com.example.planmate.domain.plan.entity." + cacheInfo.getEntityName() + ";\n";
        for (String entityName : cacheInfo.getRelatedEntitieNames()) {
            path += "import com.example.planmate.domain.plan.entity." + entityName + ";\n";
        }
        return path;
    }

    private static String writeAutoDatabaseLoader (CacheInformation cacheInfo) {
        if(cacheInfo.getParentEntityName() == null || cacheInfo.getParentId() == null) {
            return "";
        } else {
        String loader = "@AutoDatabaseLoader(repository = \"" + cacheInfo.getRepositoryName() + "\", method = \"findBy" + cacheInfo.getParentEntityName() + Generator.capitalizeFirst(cacheInfo.getParentId()) + "\")\n";
        return loader;
        }
    }

    private static String writeAutoEntityConverter(CacheInformation cacheInfo) {
        if (cacheInfo.getRelatedRepositoryNames() == null || cacheInfo.getRelatedRepositoryNames().isEmpty()) {
            return "";
        } else {
            StringBuilder repositories = new StringBuilder();
            List<String> repoList = cacheInfo.getRelatedRepositoryNames();
            for (int i = 0; i < repoList.size(); i++) {
                repositories.append("\"").append(repoList.get(i)).append("\"");
                if (i < repoList.size() - 1) {
                    repositories.append(", ");
                }
            }
            String converter = "@AutoEntityConverter(repositories = {" + repositories.toString() + "})\n";
            return converter;
        }
    }

    private static String writeDtoFields(CacheInformation cacheInfo) {
        StringBuilder fields = new StringBuilder();
        fields.append("@CacheId\n");
        fields.append("    private ").append(cacheInfo.getIdType()).append(" ").append(cacheInfo.getIdName()).append(";\n");
        for (FieldInfo fieldInfo : cacheInfo.getEntityFields()) {
            if(fieldInfo.getName().equals(cacheInfo.getParentId())){
                fields.append("    @ParentId(").append(cacheInfo.getParentEntityName()).append(".class)\n");
            }
            if (!fieldInfo.getName().equals(cacheInfo.getIdName())) {
                fields.append("    private ").append(fieldInfo.getType()).append(" ").append(fieldInfo.getName()).append(";\n");
            }
        }
        fields.append("\n");
        return fields.toString();
    }

    private static String writeFromEntityMethod(CacheInformation cacheInfo) {
        StringBuilder method = new StringBuilder();
        method.append("    public static ").append(cacheInfo.getDtoClassName()).append(" fromEntity(")
            .append(cacheInfo.getEntityName()).append(" ").append(Generator.decapitalizeFirst(cacheInfo.getEntityName())).append(") {\n");
        method.append("        return new ").append(cacheInfo.getDtoClassName()).append("(\n");
        List<FieldInfo> fields = cacheInfo.getEntityFields();
        for (int i = 0; i < fields.size(); i++) {
            FieldInfo field = fields.get(i);
            method.append("                ").append(Generator.decapitalizeFirst(cacheInfo.getEntityName()))
                .append(".get").append(Generator.capitalizeFirst(field.getName())).append("()");
            if (i < fields.size() - 1) {
                method.append(",\n");
            } else {
                method.append("\n");
            }
        }
        method.append("        );\n");
        method.append("    }\n\n");
        return method.toString();
    }

    private static String writeToEntityMethod(CacheInformation cacheInfo) {
        StringBuilder method = new StringBuilder();
        method.append("    @EntityConverter\n");
        method.append("    public ").append(cacheInfo.getEntityName()).append(" toEntity(");
        List<FieldInfo> fields = cacheInfo.getEntityFields();
        boolean first = true;
        for (FieldInfo field : fields) {
            if (field.isManyToOne()) {
                if (!first) method.append(", ");
                method.append(field.getType()).append(" ").append(field.getName());
                first = false;
            }
        }
        method.append(") {\n");
        method.append("        return ").append(cacheInfo.getEntityName()).append(".builder()\n");
        for (FieldInfo field : fields) {
            if (field.isManyToOne()) {
                method.append("                .").append(field.getName()).append("(").append(field.getName()).append(")\n");
            } else {
                method.append("                .").append(field.getName()).append("(this.").append(field.getName()).append(")\n");
            }
        }
        method.append("                .build();\n");
        method.append("    }\n");
        return method.toString();
    }
}
