package com.sharedsync.framework.generator;

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

import com.sharedsync.framework.shared.presence.annotation.PresenceUser;

import lombok.Getter;
import lombok.Setter;

@SupportedAnnotationTypes("com.sharedsync.framework.shared.presence.annotation.PresenceUser")
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

            // Repository 탐색
            findRepository(info, roundEnv);

            // app 기준으로 통일
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

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(info.getProviderFullPath());
            try (Writer writer = file.openWriter()) {

                String getter = "get" + capitalizeFirst(info.getNameFieldName()) + "()";

                writer.write("""
                        package %s;

                        import %s;
                        import com.sharedsync.framework.shared.presence.core.UserProvider;
                        import lombok.RequiredArgsConstructor;
                        import org.springframework.stereotype.Component;

                        @Component
                        @RequiredArgsConstructor
                        public class %s implements UserProvider {

                            private final %s userRepository;

                            @Override
                            public String findNicknameByUserId(int userId) {
                                return userRepository.findById(userId)
                                        .map(user -> user.%s)
                                        .orElse("Unknown");
                            }
                        }
                        """.formatted(
                        info.getProviderPackage(),
                        info.getRepositoryPath(),
                        info.getProviderClassName(),
                        info.getRepositoryName(),
                        getter
                ));
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR,
                    "Failed to generate AppUserProvider: " + e.getMessage());
        }
    }

    // =======================
    //       Util 함수들
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
}
