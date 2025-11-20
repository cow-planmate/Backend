package com.sharedsync.framework.generator;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import com.sharedsync.framework.generator.Generator.CacheInformation;

public class DtoGenerator {
    private static final String OBJECT_NAME = "dto";
    public static boolean process(CacheInformation cacheInfo, ProcessingEnvironment processingEnv) {
        String dtoClassName = cacheInfo.getEntityName() + OBJECT_NAME.substring(0, 1).toUpperCase() + OBJECT_NAME.substring(1);
        String packageName = cacheInfo.getBasicPackagePath() + "." + OBJECT_NAME;

        String source = "package " + packageName + ";\n"
            + "import org.springframework.stereotype.Component;\n"
            + "import com.sharedsync.framework.shared.framework.repository.AutoCacheRepository;\n"
            + "import " + cacheInfo.getEntityPath() + ";\n"
            + "import " + cacheInfo.getDtoPath() + ";\n"
            + "@Component\n"
            + "public class " + dtoClassName + " extends AutoCacheRepository<" + cacheInfo.getEntityName() + ", " + cacheInfo.getPkType() + ", " + cacheInfo.getDtoClassName() + "> {}";

        // 파일 생성
        JavaFileObject file;
        try {
            file = processingEnv.getFiler().createSourceFile(packageName + "." + dtoClassName);
            Writer writer = file.openWriter();
            writer.write(source);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return true;
    }
}
