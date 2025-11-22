package com.sharedsync.framework.generator;

import java.lang.reflect.Field;
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

        private List<String> relatedEntitieNames;
        private String parentEntityName;
        private String parentId;

        private String repositoryName;
        private List<String> relatedRepositoryNames;

        private List<Field> entityFields;

        // dto
        private String dtoClassName;
        private String dtoPath;
        // cache
        private String cacheClassName;
        private String cachePath;
        

        public CacheInformation() {
            basicPackagePath = "sharedsync";
            relatedEntitieNames = new ArrayList<>();
            relatedRepositoryNames = new ArrayList<>();
            entityFields = new ArrayList<>();
        }

        public void addRelatedEntityName(String entityName) {
            this.relatedEntitieNames.add(entityName);
        }
        public void setRelatedRepositoryName(String repositoryName, int index) {
            // 리스트 크기 맞추기
            while (relatedRepositoryNames.size() <= index) {
                relatedRepositoryNames.add("");
            }
            relatedRepositoryNames.set(index, repositoryName);
        }
        public void setEntityFields(List<Field> entityFields) {
            this.entityFields = entityFields;
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
            

            // 모든 Repository 인터페이스를 탐색하여 해당 엔티티와 PK 타입을 관리하는 Repository를 찾음
            for (Element repoElement : roundEnv.getRootElements()) {
                if (repoElement instanceof TypeElement typeElement) {
                    // 인터페이스만 처리
                    if (typeElement.getKind().isInterface()) {
                        for (javax.lang.model.type.TypeMirror iface : typeElement.getInterfaces()) {
                            if (iface instanceof DeclaredType declaredType) {
                                Element ifaceElement = declaredType.asElement();
                                if (ifaceElement.getSimpleName().toString().equals("JpaRepository")) {
                                    List<? extends javax.lang.model.type.TypeMirror> typeArgs = declaredType.getTypeArguments();
                                    if (typeArgs.size() == 2) {
                                        String repoEntityType = typeArgs.get(0).toString();
                                        // 현재 entity와 PK 타입이 일치하는 Repository라면
                                        if (repoEntityType.equals(element.asType().toString())) {
                                            cacheInfo.setRepositoryName(typeElement.getQualifiedName().toString());
                                        }
                                        // relatedEntitieNames와 순서 맞춰서 관련 Repository 이름 저장
                                        for (int i = 0; i < cacheInfo.getRelatedEntitieNames().size(); i++) {
                                            if (repoEntityType.equals(cacheInfo.getRelatedEntitieNames().get(i))) {
                                                cacheInfo.setRelatedRepositoryName(typeElement.getQualifiedName().toString(), i);
                                            }
                                        }
                                    }
                                }
                            }
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

    // 앞글자만 대문자로 바꿔주는 static 메서드
    public static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
