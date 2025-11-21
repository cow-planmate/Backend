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
import javax.lang.model.type.DeclaredType;

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
        private String basicPackagePath;
        private String entityPath;

        private String repositoryName;

        private List<String> relatedEntitieNames;
        private String parentEntityName;
        private String parentId;

        

        // dto
        private String dtoClassName;
        private String dtoPath;
        // cache
        private String cacheClassName;
        private String cachePath;
        

        public CacheInformation() {
            basicPackagePath = "sharedsync";
            relatedEntitieNames = new ArrayList<String>();
        }

        public void addRelatedEntityName(String entityName) {
            this.relatedEntitieNames.add(entityName);
        }
            
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        
        for (Element element : roundEnv.getElementsAnnotatedWith(CacheEntity.class)) {
            CacheInformation cacheInfo = new CacheInformation();
            String entityName = element.getSimpleName().toString();
            cacheInfo.setEntityName(entityName);
            String pkType = "";
            cacheInfo.setEntityPath(element.asType().toString());

            for(Element repoElement : roundEnv.get(JpaRepository))



            for (Element field : element.getEnclosedElements()) {
                if (field.getAnnotation(jakarta.persistence.Id.class) != null) {
                    pkType = field.asType().toString(); // 예: java.lang.Integer
                    // 필요시 마지막 점(.) 뒤만 추출해서 Integer 등으로 쓸 수 있음
                }

                if(field.getAnnotation(jakarta.persistence.ManyToOne.class) != null ) {
                    cacheInfo.addRelatedEntityName(field.asType().toString());
                    if (field.asType() instanceof DeclaredType declaredType) {
                        Element typeElement = declaredType.asElement();
                        if (typeElement.getAnnotation(CacheEntity.class) != null) {
                            cacheInfo.setParentEntityName(field.asType().toString());

                            // 부모 엔티티의 PK 필드명/타입 찾기
                            String parentId = null;
                            for (Element parentField : typeElement.getEnclosedElements()) {
                                if (parentField.getAnnotation(jakarta.persistence.Id.class) != null) {
                                    parentId = parentField.getSimpleName().toString();
                                    break;
                                }
                            }
                            cacheInfo.setParentId(parentId);
                        }
                    }
                }

            }
            cacheInfo.setPkType(pkType);
            cacheInfoList.add(cacheInfo);
            CacheEntityGenerator.process(cacheInfo, processingEnv);
        }

        
        return false;
    }
}
