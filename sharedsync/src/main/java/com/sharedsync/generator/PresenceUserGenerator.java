package com.sharedsync.generator;

import java.io.IOException;
import java.io.Writer;
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
import javax.tools.JavaFileObject;

import com.sharedsync.shared.presence.annotation.PresenceUser;

import lombok.Getter;
import lombok.Setter;

@SupportedAnnotationTypes("com.sharedsync.shared.presence.annotation.PresenceUser")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class PresenceUserGenerator extends AbstractProcessor {

    List<PresenceUserInformation> userInfoList;

    public PresenceUserGenerator() {
        userInfoList = new ArrayList<>();
    }

    // =======================
    //   내부 DTO 클래스 구조
    // =======================

    @Getter
    @Setter
    public class PresenceUserInformation {
        private String entityPath;
        private String entityName;
        private String idFieldName;
        private String idFieldType;    // [추가됨] ID 필드의 타입 (ex: String, Long)
        private String nameFieldName;

        private String repositoryPath;
        private String repositoryName;

        // 생성할 정보
        private String providerClassName;
        private String providerPackage;
        private String providerFullPath;
    }

    // =======================
    //     PROCESS 시작
    // =======================

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(PresenceUser.class)) {

            PresenceUserInformation info = new PresenceUserInformation();

            String entityPath = element.asType().toString();
            String entityName = removePath(entityPath);

            PresenceUser ann = element.getAnnotation(PresenceUser.class);

            info.setEntityPath(entityPath);
            info.setEntityName(entityName);
            info.setIdFieldName(ann.idField());
            info.setNameFieldName(ann.nameField());

            // [추가됨] ID 필드 타입 탐색
            String idFieldType = findIdFieldType(element, ann.idField());
            info.setIdFieldType(idFieldType);

            // Repository 탐색
            findRepository(info, roundEnv);

            // app 기준으로 통일 (필요시 로직 변경 가능)
            info.setProviderClassName("AppUserProvider");
            info.setProviderPackage("sharedsync.presence");
            info.setProviderFullPath(info.getProviderPackage() + "." + info.getProviderClassName());

            // 저장
            userInfoList.add(info);

            // Provider 생성
            generateProvider(info);
        }

        return false;
    }

    // =======================
    //   필드 타입 탐색 [추가됨]
    // =======================

    private String findIdFieldType(Element element, String idFieldName) {
        // 엔티티 내부의 모든 요소(필드, 메서드 등)를 순회
        for (Element enclosed : element.getEnclosedElements()) {
            // 필드(Field)이면서 이름이 idFieldName과 같은지 확인
            if (enclosed.getKind().isField() && enclosed.getSimpleName().toString().equals(idFieldName)) {
                // 타입의 전체 경로에서 패키지명을 떼고 반환 (ex: java.lang.Long -> Long)
                return removePath(enclosed.asType().toString());
            }
        }
        // 찾지 못했을 경우 기본값
        return "String";
    }

    // =======================
    //   Repository 탐색
    // =======================

    private void findRepository(PresenceUserInformation info, RoundEnvironment roundEnv) {

        for (Element repoElement : roundEnv.getRootElements()) {

            if (repoElement instanceof TypeElement typeElement) {

                if (!typeElement.getKind().isInterface()) continue;

                for (javax.lang.model.type.TypeMirror iface : typeElement.getInterfaces()) {

                    if (iface instanceof DeclaredType declaredType) {

                        Element ifaceElement = declaredType.asElement();
                        if (!ifaceElement.getSimpleName().toString().equals("JpaRepository"))
                            continue;

                        List<? extends javax.lang.model.type.TypeMirror> typeArgs = declaredType.getTypeArguments();
                        if (typeArgs.size() != 2) continue;

                        String repoEntityType = typeArgs.get(0).toString();

                        if (repoEntityType.equals(info.getEntityPath())) {
                            info.setRepositoryPath(typeElement.getQualifiedName().toString());
                            info.setRepositoryName(removePath(typeElement.getQualifiedName().toString()));
                        }
                    }
                }
            }
        }
    }

    // =======================
    //   Provider 파일 생성
    // =======================

    private void generateProvider(PresenceUserInformation info) {

        if (info.getRepositoryName() == null) {
            // Repository를 못 찾았으면 생성 스킵 혹은 로그 출력
            processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.WARNING, 
                "Repository not found for entity: " + info.getEntityName());
            return;
        }

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(info.getProviderFullPath());
            try (Writer writer = file.openWriter()) {

                String getter = "get" + capitalizeFirst(info.getNameFieldName()) + "()";
                
                // ID 타입에 따른 변환 로직 (String -> Long 등)
                String idConversion = getChangeType(info.getIdFieldType());

                writer.write("""
                        package %s;

                        import %s;
                        import com.sharedsync.shared.presence.core.UserProvider;
                        import lombok.RequiredArgsConstructor;
                        import org.springframework.stereotype.Component;

                        @Component
                        @RequiredArgsConstructor
                        public class %s implements UserProvider {

                            private final %s userRepository;

                            @Override
                            public String findNicknameByUserId(String userId) {
                                return userRepository.findById(%s)
                                        .map(user -> user.%s)
                                        .orElse("Unknown");
                            }
                        }
                        """.formatted(
                        info.getProviderPackage(),      // 1. package
                        info.getRepositoryPath(),       // 2. import repo
                        info.getProviderClassName(),    // 3. class name
                        info.getRepositoryName(),       // 4. field type
                        idConversion,                   // 5. findById arg
                        getter                          // 6. map getter
                ));
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR,
                    "Failed to generate AppUserProvider: " + e.getMessage());
        }
    }

    // =======================
    //      Util 함수들
    // =======================

    public static String removePath(String fullPath) {
        if (fullPath.contains(".")) {
            return fullPath.split("\\.")[fullPath.split("\\.").length - 1];
        }
        return fullPath;
    }

    public static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String getChangeType(String fieldType) {
        if (fieldType == null) return "userId";
        
        switch (fieldType) {
            case "String":
                return "userId";
            case "Long":
                return "Long.parseLong(userId)";
            case "Integer":
            case "int":
                return "Integer.parseInt(userId)";
            default:
                return "userId"; // 기본적으로 String 취급
        }
    }
}