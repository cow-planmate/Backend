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

    List<CacheInformation> cacheInfoList;

    public Generator() {
        cacheInfoList = new ArrayList<>();
    }
    @Getter
    @Setter
    public class FieldInfo {
        private String name;
        private String type;
        private boolean isManyToOne;

        public FieldInfo(String name, String type, boolean isManyToOne) {
            this.name = name;
            this.type = type;
            this.isManyToOne = isManyToOne;
        }
    }
    @Getter
    @Setter
    public class RelatedEntity{
        private String entityPath;
        private String entityIdType;
        private String entityIdName;
        private String repositoryPath;
    }

    @Getter
    @Setter
    public class CacheInformation{

        private String entityName;
        private String idType;
        private String idName;
        private String basicPackagePath;
        private String entityPath;

        private String parentEntityPath;
        private String parentId;

        private String repositoryName;
        private String repositoryPath;
        private List<FieldInfo> entityFields;

        // dto
        private String dtoClassName;
        private String dtoPath;

        // cache
        private String cacheClassName;
        private String cachePath;

        // controller
        private String controllerClassName;
        private String controllerPath;

        // service
        private String serviceClassName;
        private String servicePath;

        // request
        private String requestClassName;
        private String requestPath;

        // response
        private String responseClassName;
        private String responsePath;

        List<RelatedEntity> relatedEntities;


        public CacheInformation() {
            basicPackagePath = "sharedsync";
            relatedEntities = new ArrayList<>();
            entityFields = new ArrayList<>();
        }

        public void addRelatedEntity(RelatedEntity relatedEntity){
            relatedEntities.add(relatedEntity);
        }
        public void addEntityField(FieldInfo fieldInfo) {
            this.entityFields.add(fieldInfo);
        }

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(CacheEntity.class)) {
            CacheInformation cacheInfo = new CacheInformation();
            String entityName = element.getSimpleName().toString();
            cacheInfo.setEntityName(entityName);
            cacheInfo.setEntityPath(element.asType().toString());

            for (Element field : element.getEnclosedElements()) {
                if (field.getKind().isField()) {
                    boolean isManyToOne = field.getAnnotation(jakarta.persistence.ManyToOne.class) != null;
                    cacheInfo.addEntityField(new FieldInfo(field.getSimpleName().toString(), field.asType().toString(), isManyToOne));
                }
                if (field.getAnnotation(jakarta.persistence.Id.class) != null) {
                    cacheInfo.setIdType(removePath(field.asType().toString()));
                    cacheInfo.setIdName(field.getSimpleName().toString());
                }
                if(field.getAnnotation(jakarta.persistence.ManyToOne.class) != null ) {
                    RelatedEntity relatedEntity = new RelatedEntity();
                    relatedEntity.setEntityPath(field.asType().toString());
                    for (Element relatedField : ((TypeElement)((DeclaredType)field.asType()).asElement()).getEnclosedElements()) {
                        if (relatedField.getAnnotation(jakarta.persistence.Id.class) != null) {
                            relatedEntity.setEntityIdType(removePath(relatedField.asType().toString()));
                            relatedEntity.setEntityIdName(relatedField.getSimpleName().toString());
                            break;
                        }
                    }
                    cacheInfo.addRelatedEntity(relatedEntity);
                    if (field.asType() instanceof DeclaredType declaredType) {
                        Element typeElement = declaredType.asElement();
                        if (typeElement.getAnnotation(CacheEntity.class) != null) {
                            cacheInfo.setParentEntityPath(field.asType().toString());
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
                                            cacheInfo.setRepositoryName(removePath(typeElement.getQualifiedName().toString()));
                                            cacheInfo.setRepositoryPath(typeElement.getQualifiedName().toString());
                                        }

                                        for (RelatedEntity relatedEntity : cacheInfo.getRelatedEntities()) {
                                            if (repoEntityType.equals(relatedEntity.getEntityPath())) {
                                                relatedEntity.setRepositoryPath(typeElement.getQualifiedName().toString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            cacheInfoList.add(cacheInfo);
            initialize(cacheInfo);


            CacheEntityGenerator.process(cacheInfo, processingEnv);
            DtoGenerator.process(cacheInfo, processingEnv);
            WebsocketDtoGenerator.process(cacheInfo, processingEnv);
            ControllerGenerator.process(cacheInfo, processingEnv);
            ServiceGenerator.process(cacheInfo, processingEnv);
        }


        return false;
    }

    public static void initialize(CacheInformation cacheInfo) {
        CacheEntityGenerator.initialize(cacheInfo);
        DtoGenerator.initialize(cacheInfo);
        WebsocketDtoGenerator.initialize(cacheInfo);
        ControllerGenerator.initialize(cacheInfo);
        ServiceGenerator.initialize(cacheInfo);
    }

    // 앞글자만 대문자로 바꿔주는 static 메서드
    public static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    // 앞글자만 소문자로 바꿔주는 static 메서드
    public static String decapitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static String removePath(String fullPath){
        if(fullPath.contains(".")){
            return fullPath.split("\\.")[fullPath.split("\\.").length - 1];
        }
        return fullPath;
    }
}
