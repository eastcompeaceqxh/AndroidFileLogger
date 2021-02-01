package com.xinghai.log.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.xinghai.log.annotation.LogFilesDefine;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MyAnnotationProcessor extends AbstractProcessor {

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 生成代码
        String[] fileNames = null;
        String defaultLogFile = "";
        for (TypeElement element : set) {
            Set<? extends Element> elementAnnotationWith =
                    roundEnvironment.getElementsAnnotatedWith(element);

            for (Element element1 : elementAnnotationWith) {
                LogFilesDefine filesDefine = element1.getAnnotation(LogFilesDefine.class);
                fileNames = filesDefine.value();
                defaultLogFile = filesDefine.defaultLogFile();
                break;
            }
        }
        generateJavaClass(defaultLogFile, fileNames);
        return true;
    }

    private void generateJavaClass(String defaultLogFile, String[] files) {
        Set<MethodSpec> methodSpecs = new HashSet<>();

        ClassName appLogger = ClassName.get("com.xinghai.log.lib", "AppLogger");

        ClassName stringClass = ClassName.get("java.lang", "String");

        ClassName hashMap = ClassName.get("java.util", "HashMap");

        TypeName hashMapWithType = ParameterizedTypeName.get(hashMap, stringClass, appLogger);

        FieldSpec fieldMap = FieldSpec.builder(hashMapWithType, "loggerHashMap")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T($L)", hashMapWithType, files == null ? 1 : files.length + 1)
                .build();

        MethodSpec getLoggerByFileName = MethodSpec.methodBuilder("getLoggerByFileName")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(String.class, "fileName")
                .addStatement("$T logger = loggerHashMap.get(fileName)", appLogger)
                .beginControlFlow("if (logger == null)")
                .beginControlFlow("synchronized(AppLoggerUtil.class)")
                .addStatement("logger = loggerHashMap.get(fileName)")
                .beginControlFlow("if (logger == null)")
                .addStatement("logger = new $T(fileName)", appLogger)
                .addStatement("loggerHashMap.put(fileName, logger)")
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addStatement("return logger")
                .returns(appLogger)
                .build();

        MethodSpec defaultLogger = MethodSpec.methodBuilder("defaultLogger")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addStatement("return getLoggerByFileName($S)", defaultLogFile)
                .returns(appLogger)
                .build();

        MethodSpec methodInfo = MethodSpec.methodBuilder("info")
                .addParameter(String.class, "tag")
                .addParameter(String.class, "msg")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("defaultLogger().info(tag, msg)")
                .returns(void.class)
                .build();

        MethodSpec methodDebug = MethodSpec.methodBuilder("debug")
                .addParameter(String.class, "tag")
                .addParameter(String.class, "msg")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("defaultLogger().debug(tag, msg)")
                .returns(void.class)
                .build();

        MethodSpec methodWarn = MethodSpec.methodBuilder("warn")
                .addParameter(String.class, "tag")
                .addParameter(String.class, "msg")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("defaultLogger().warn(tag, msg)")
                .returns(void.class)
                .build();

        MethodSpec methodError = MethodSpec.methodBuilder("error")
                .addParameter(String.class, "tag")
                .addParameter(String.class, "msg")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("defaultLogger().error(tag, msg)")
                .returns(void.class)
                .build();

        if (files != null && files.length > 0) {
            for (String fileName : files) {
                MethodSpec methodSpec = MethodSpec.methodBuilder(fileName + "Logger")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addStatement("return getLoggerByFileName($S)", fileName)
                        .returns(appLogger)
                        .build();
                methodSpecs.add(methodSpec);
            }
        }

        TypeSpec typeSpec = TypeSpec.classBuilder("AppLoggerUtil")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(fieldMap)
                .addMethod(defaultLogger)
                .addMethod(getLoggerByFileName)
                .addMethod(methodInfo)
                .addMethod(methodDebug)
                .addMethod(methodWarn)
                .addMethod(methodError)
                .addMethods(methodSpecs)
                .build();

        try {
            JavaFile javaFile = JavaFile.builder("com.xinghai.log.lib", typeSpec)
                    .addFileComment(" This codes are generated automatically. Do not modify!")
                    .build();
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(LogFilesDefine.class.getCanonicalName());
    }
}