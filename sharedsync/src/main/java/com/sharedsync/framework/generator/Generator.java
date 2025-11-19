package com.sharedsync.framework.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.sharedsync.framework.shared.framework.annotation.CacheEntity;

import lombok.Getter;
import lombok.Setter;

@SupportedAnnotationTypes("com.sharedsync.framework.shared.framework.annotation.CacheEntity")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class Generator extends AbstractProcessor{
    List<CacheInformation> cacheInfoList = new ArrayList<CacheInformation>();;
    @Getter
    @Setter
    public class CacheInformation{
        private String entityName;
        private String pkType;
        private String basicPackagePath="sharedsync";
        private String entityPath;


        // dto
        private String dtoClassName;
        private String dtoPath;
        // cache
        private String cacheClassName;
        private String cachePath;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(CacheEntity.class)) {
            CacheInformation cacheInfo = new CacheInformation();
            String entityName = element.getSimpleName().toString();
            cacheInfo.setEntityName(entityName);
            String pkType = "";
            cacheInfo.setEntityPath(element.asType().toString());



            for (Element field : element.getEnclosedElements()) {
                if (field.getAnnotation(jakarta.persistence.Id.class) != null) {
                    pkType = field.asType().toString(); // 예: java.lang.Integer
                    // 필요시 마지막 점(.) 뒤만 추출해서 Integer 등으로 쓸 수 있음
                }
            }
            cacheInfo.setPkType(pkType);
            cacheInfoList.add(cacheInfo);
            CacheEntityGenerator.process(cacheInfo, processingEnv);
        }

        
        return false;
    }
}
