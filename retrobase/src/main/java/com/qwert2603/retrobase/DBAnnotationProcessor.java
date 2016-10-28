package com.qwert2603.retrobase;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(value = {
        "com.qwert2603.retrobase.DBInterface",
        "com.qwert2603.retrobase.DBQuery"})
public class DBAnnotationProcessor extends AbstractProcessor {

    private static final String GENERATED_PACKAGE = "com.qwert2603.retrobase.generated";
    private static final String GENERATED_FILENAME_SUFFIX = "Impl";
    private static final String PREPARED_STATEMENT_SUFFIX = "_PreparedStatement";

    /**
     * Add realization for method "executableElement" from interface.
     */
    private TypeSpec.Builder addMethodImpl(TypeSpec.Builder newTypeBuilder,
                                           FieldSpec mConnection, ExecutableElement executableElement) {

        final TypeName RESULT_SET_TYPE_NAME = TypeName.get(ResultSet.class);

        // prepared statement for query, than can be done with implementing method.
        FieldSpec fieldSpec = FieldSpec
                .builder(PreparedStatement.class, executableElement.getSimpleName().toString() + PREPARED_STATEMENT_SUFFIX)
                .addModifiers(Modifier.PRIVATE)
                .initializer("null")
                .build();

       List<? extends TypeMirror> thrownTypes = executableElement.getThrownTypes();

        // check throws declaration.
        boolean catchExceptions;
        if (thrownTypes.isEmpty()) {
            catchExceptions = true;
        } else if (thrownTypes.size() == 1 && TypeName.get(thrownTypes.get(0)).equals(TypeName.get(SQLException.class))) {
            catchExceptions = false;
        } else {
            throw new RuntimeException("method should throws SQLException or don't throw at all");
        }

        // check return type.
        TypeName returnTypeName = TypeName.get(executableElement.getReturnType());
        if (returnTypeName != TypeName.VOID && !returnTypeName.equals(RESULT_SET_TYPE_NAME)) {
            throw new RuntimeException("method should return ResultSet or void");
        }

        // parameters of query and method.
        List<? extends VariableElement> parameters = executableElement.getParameters();

        // query to DB itself.
        String query = executableElement.getAnnotation(DBQuery.class).value();

        // create method.
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnTypeName);


        // starting "try" block, if need; else -- declare "throws SQLException"
        if (catchExceptions) {
            methodBuilder = methodBuilder.beginControlFlow("try");
        } else {
            methodBuilder = methodBuilder.addException(SQLException.class);
        }

        // create PreparedStatement if it was not done before.
        methodBuilder = methodBuilder
                .addStatement("initIfNotYet()")
                .beginControlFlow("if ($N == $L)", fieldSpec, null)
                .addStatement("$N = $N.prepareStatement($S)", fieldSpec, mConnection, query)
                .endControlFlow();

        // set method params to PreparedStatement.
        for (int i = 0; i < parameters.size(); i++) {
            VariableElement parameter = parameters.get(i);
            TypeName paramTypeName = TypeName.get(parameter.asType());
            methodBuilder = methodBuilder
                    .addParameter(paramTypeName, parameter.getSimpleName().toString());

            String statementFormat = null;

            if (paramTypeName.equals(TypeName.INT)) {
                statementFormat = "$N.setInt($L, $N)";
            } else if (paramTypeName.equals(TypeName.DOUBLE)) {
                statementFormat = "$N.setDouble($L, $N)";
            } else if (paramTypeName.equals(TypeName.BOOLEAN)) {
                statementFormat = "$N.setBoolean($L, $N)";
            } else if (paramTypeName.equals(TypeName.LONG)) {
                statementFormat = "$N.setLong($L, $N)";
            } else if (paramTypeName.equals(TypeName.FLOAT)) {
                statementFormat = "$N.setFloat($L, $N)";
            } else if (paramTypeName.equals(TypeName.BYTE)) {
                statementFormat = "$N.setByte($L, $N)";
            } else if (paramTypeName.equals(TypeName.get(String.class))) {
                statementFormat = "$N.setString($L, $N)";
            } else if (paramTypeName.equals(TypeName.get(BigDecimal.class))) {
                statementFormat = "$N.setBigDecimal($L, $N)";
            } else if (paramTypeName.equals(TypeName.get(java.sql.Date.class))) {
                statementFormat = "$N.setDate($L, $N)";
            } else if (paramTypeName.equals(TypeName.get(java.sql.Time.class))) {
                statementFormat = "$N.setTime($L, $N)";
            } else if (paramTypeName.equals(TypeName.get(java.sql.Timestamp.class))) {
                statementFormat = "$N.setTimestamp($L, $N)";
            } else if (paramTypeName.equals(TypeName.get(java.sql.Array.class))) {
                statementFormat = "$N.setArray($L, $N)";
            } else if (paramTypeName.equals(TypeName.get(java.sql.Clob.class))) {
                statementFormat = "$N.setClob($L, $N)";
            } else if (paramTypeName.equals(TypeName.get(java.sql.Blob.class))) {
                statementFormat = "$N.setBlob($L, $N)";
            } else if (paramTypeName.equals(TypeName.get(java.sql.NClob.class))) {
                statementFormat = "$N.setNClob($L, $N)";
            } else if (paramTypeName.equals(TypeName.get(java.lang.Object.class))) {
                statementFormat = "$N.setObject($L, $N)";
            }

            if (statementFormat != null) {
                methodBuilder = methodBuilder
                        .addStatement(statementFormat, fieldSpec, i + 1, parameter.getSimpleName());
            }

        }

        // executing statement.
        if (returnTypeName == TypeName.VOID) {
            methodBuilder = methodBuilder.addStatement("$N.execute()", fieldSpec);
        } else if (returnTypeName.equals(RESULT_SET_TYPE_NAME)) {
            methodBuilder = methodBuilder.addStatement("return $N.executeQuery()", fieldSpec);
        }

        // if need, catch exceptions.
        if (catchExceptions) {
            methodBuilder = methodBuilder
                    .endControlFlow()
                    .beginControlFlow("catch ($T e)", SQLException.class)
                    .addStatement("e.printStackTrace()");

            if (returnTypeName.equals(RESULT_SET_TYPE_NAME)) {
                methodBuilder = methodBuilder.addStatement("return null");
            }

            methodBuilder = methodBuilder.endControlFlow();
        }

        // add field and method to type.
        return newTypeBuilder
                .addField(fieldSpec)
                .addMethod(methodBuilder.build());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element dbInterfaceClass : roundEnv.getElementsAnnotatedWith(DBInterface.class)) {
            if (dbInterfaceClass.getKind() != ElementKind.INTERFACE) {
                throw new RuntimeException("@DBInterface should be applied to interface");
            }
            DBInterface dbInterface = dbInterfaceClass.getAnnotation(DBInterface.class);

            // connection to database.
            FieldSpec mConnection = FieldSpec.builder(Connection.class, "mConnection", Modifier.PRIVATE)
                    .initializer("null")
                    .build();

            MethodSpec.Builder initIfNotYetBuilder = MethodSpec.methodBuilder("initIfNotYet")
                    .addModifiers(Modifier.PRIVATE)
                    .addException(SQLException.class)
                    .beginControlFlow("if ($N == $L || !$N.isValid(0))", mConnection, null, mConnection)
                    .addStatement("$N = $T.getConnection($S, $S, $S)",
                            mConnection, DriverManager.class, dbInterface.url(), dbInterface.login(), dbInterface.password());

            // for every method in interface
            for (Element enclosedElement : dbInterfaceClass.getEnclosedElements()) {
                if (enclosedElement.getAnnotation(DBQuery.class) != null) {
                    initIfNotYetBuilder = initIfNotYetBuilder.addStatement(enclosedElement.getSimpleName().toString() + PREPARED_STATEMENT_SUFFIX + " = null");
                }
            }

            // method for initializing of mConnection.
            MethodSpec waitInit = initIfNotYetBuilder
                    .endControlFlow()
                    .build();

            // type that implements interface, marked with @DBInterface.
            TypeSpec.Builder newTypeBuilder = TypeSpec.classBuilder(dbInterfaceClass.getSimpleName() + GENERATED_FILENAME_SUFFIX)
                    .addSuperinterface(TypeName.get(dbInterfaceClass.asType()))
                    .addField(mConnection)
                    .addMethod(waitInit)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            // for every method in interface
            for (Element enclosedElement : dbInterfaceClass.getEnclosedElements()) {
                if (enclosedElement.getAnnotation(DBQuery.class) != null) {
                    newTypeBuilder = addMethodImpl(newTypeBuilder, mConnection, (ExecutableElement) enclosedElement);
                }
            }

            // generated class implementing interface.
            JavaFile javaFile = JavaFile.builder(GENERATED_PACKAGE, newTypeBuilder.build())
                    .build();


            // write generated class to file.
            try {
                String filename = GENERATED_PACKAGE + "." + dbInterfaceClass.getSimpleName() + GENERATED_FILENAME_SUFFIX;
                JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(filename);
                Writer writer = sourceFile.openWriter();
                writer.write(javaFile.toString());
                writer.flush();
                writer.close();
            } catch (IOException ignored) {
            }

        }

        return true;
    }
}
