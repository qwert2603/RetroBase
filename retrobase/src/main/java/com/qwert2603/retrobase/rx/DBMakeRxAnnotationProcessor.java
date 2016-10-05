package com.qwert2603.retrobase.rx;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

import rx.Observable;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(value = {
        "com.qwert2603.retrobase.rx.DBMakeRx",
        "com.qwert2603.retrobase.rx.DBInterfaceRx"})
public class DBMakeRxAnnotationProcessor extends AbstractProcessor {

    private static final String GENERATED_PACKAGE = "com.qwert2603.retrobase_rx.generated";
    private static final String GENERATED_FILENAME_SUFFIX = "Rx";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element dbInterfaceRxClass : roundEnv.getElementsAnnotatedWith(DBInterfaceRx.class)) {

            // create new type that contains methods-wrappers.
            TypeSpec.Builder newTypeBuilder = TypeSpec.classBuilder(dbInterfaceRxClass.getSimpleName() + GENERATED_FILENAME_SUFFIX)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            TypeName dbInterfaceTypeName = TypeName.get(dbInterfaceRxClass.asType());

            // field in new class that represents annotated class.
            // we will call his methods in created methods-wrappers.
            FieldSpec mDB = FieldSpec.builder(dbInterfaceTypeName, "mDB")
                    .addModifiers(Modifier.PRIVATE)
                    .build();

            // constructor for new class, to get object of annotated class.
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(dbInterfaceTypeName, "db")
                    .addStatement("this.$N = db", mDB)
                    .build();

            // add field and constructor.
            newTypeBuilder = newTypeBuilder
                    .addField(mDB)
                    .addMethod(constructor);

            // there are troubles with generic return type in JavaPoet =(
            // so we will replace return types later.
            // we will do that just replacing CharSequences in the text of generated class =)
            //
            // we will replace return type "void" with rx.Observable<Object>.
            // and we will replace return type "float" with rx.Observable<*classname*>,
            // where *classname* is @DBMakeRx#modelClassName().
            // so let's save *classname* for every method in this map =)
            Map<String, String> map = new HashMap<>();

            // for every method, annotated with @DBQueryRx.
            for (Element enclosedElement : dbInterfaceRxClass.getEnclosedElements()) {
                DBMakeRx dbMakeRx = enclosedElement.getAnnotation(DBMakeRx.class);
                if (dbMakeRx == null) {
                    continue;
                }

                // creating method-wrapper.
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(enclosedElement.getSimpleName().toString())
                        .addModifiers(Modifier.PUBLIC);

                ExecutableElement executableElement = (ExecutableElement) enclosedElement;

                // checking return type of annotated method.
                TypeName returnTypeName = TypeName.get(executableElement.getReturnType());

                boolean isVoid;
                if (returnTypeName.equals(TypeName.VOID)) {
                    methodBuilder = methodBuilder.returns(void.class);
                    isVoid = true;
                } else if (returnTypeName.equals(TypeName.get(ResultSet.class))) {
                    methodBuilder = methodBuilder.returns(float.class);
                    map.put(enclosedElement.getSimpleName().toString(), dbMakeRx.modelClassName());
                    isVoid = false;
                } else {
                    throw new RuntimeException("method should return void or java.sql.ResultSet, but "
                            + returnTypeName + " found");
                }

                // body of method-wrapper.
                methodBuilder = methodBuilder
                        .beginControlFlow("return $T.create(subscriber -> ", Observable.class)
                        .beginControlFlow("try");

                // adding parameters to method-wrapper.
                List<? extends VariableElement> parameters = executableElement.getParameters();
                StringBuilder stringBuilder = new StringBuilder();
                for (VariableElement parameter : parameters) {
                    methodBuilder = methodBuilder
                            .addParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString());
                    stringBuilder.append(parameter.getSimpleName().toString()).append(", ");
                }

                if (stringBuilder.length() > 0) {
                    stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                }
                String paramsToMethod = stringBuilder.toString();

                if (isVoid) {
                    methodBuilder = methodBuilder
                            .addStatement("mDB.$L($L)", enclosedElement.getSimpleName(), paramsToMethod)
                            .beginControlFlow("if (!subscriber.isUnsubscribed())")
                            .addStatement("subscriber.onNext(new $T())", Object.class)
                            .endControlFlow();
                } else {
                    methodBuilder = methodBuilder
                            .addStatement("$T resultSet = mDB.$L($L)", ResultSet.class, enclosedElement.getSimpleName(), paramsToMethod)
                            .beginControlFlow("while (resultSet.next() && !subscriber.isUnsubscribed())")
                            .addStatement("subscriber.onNext(new $L(resultSet))", dbMakeRx.modelClassName())
                            .endControlFlow();
                }

                methodBuilder = methodBuilder
                        .beginControlFlow("if (!subscriber.isUnsubscribed())")
                        .addStatement("subscriber.onCompleted()")
                        .endControlFlow()
                        .endControlFlow() // end of try-block
                        .beginControlFlow("catch ($T e)", Exception.class)
                        .beginControlFlow("if (!subscriber.isUnsubscribed())")
                        .addStatement("subscriber.onError(e)")
                        .endControlFlow()
                        .endControlFlow() // end of "subscriber's void call(T t);"
                        .endControlFlow(")");

                newTypeBuilder = newTypeBuilder.addMethod(methodBuilder.build());
            }

            JavaFile javaFile = JavaFile.builder(GENERATED_PACKAGE, newTypeBuilder.build()).build();

            // now we replace return types as was said before in comments.
            String newFile = javaFile.toString();
            newFile = newFile.replace("public void ", "public rx.Observable<Object> ");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                newFile = newFile.replace("public float " + entry.getKey(),
                        "public rx.Observable<" + entry.getValue() + "> " + entry.getKey());
            }

            // write generated class to file.
            try {
                String filename = GENERATED_PACKAGE + "." + dbInterfaceRxClass.getSimpleName() + GENERATED_FILENAME_SUFFIX;
                JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(filename);
                Writer writer = sourceFile.openWriter();
                writer.write(newFile);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return true;
    }
}
