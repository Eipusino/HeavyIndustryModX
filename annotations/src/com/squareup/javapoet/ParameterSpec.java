/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.javapoet;

import javax.lang.model.*;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import static com.squareup.javapoet.Util.*;

/**
 * A generated parameter declaration.
 */
public final class ParameterSpec {
    public final String name;
    public final List<AnnotationSpec> annotations;
    public final Set<Modifier> modifiers;
    public final TypeName type;
    public final CodeBlock javadoc;

    private ParameterSpec(Builder builder) {
        name = checkNotNull(builder.name, "name == null");
        annotations = Util.immutableList(builder.annotations);
        modifiers = Util.immutableSet(builder.modifiers);
        type = checkNotNull(builder.type, "type == null");
        javadoc = builder.javadoc.build();
    }

    public static ParameterSpec get(VariableElement element) {
        checkArgument(element.getKind().equals(ElementKind.PARAMETER), "element is not a parameter");

        TypeName type = TypeName.get(element.asType());
        String name = element.getSimpleName().toString();
        // Copying parameter annotations can be incorrect so we're deliberately not including them.
        // See https://github.com/square/javapoet/issues/482.
        return ParameterSpec.builder(type, name)
                .addModifiers(element.getModifiers())
                .build();
    }

    static List<ParameterSpec> parametersOf(ExecutableElement method) {
        List<ParameterSpec> result = new ArrayList<>();
        for (VariableElement parameter : method.getParameters()) {
            result.add(ParameterSpec.get(parameter));
        }
        return result;
    }

    private static boolean isValidParameterName(String name) {
        // Allow "this" for explicit receiver parameters
        // See https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.4.1.
        if (name.endsWith(".this")) {
            return SourceVersion.isIdentifier(name.substring(0, name.length() - ".this".length()));
        }
        return name.equals("this") || SourceVersion.isName(name);
    }

    public static Builder builder(TypeName type, String name, Modifier... modifiers) {
        checkNotNull(type, "type == null");
        checkArgument(isValidParameterName(name), "not a valid name: %s", name);
        return new Builder(type, name)
                .addModifiers(modifiers);
    }

    public static Builder builder(Type type, String name, Modifier... modifiers) {
        return builder(TypeName.get(type), name, modifiers);
    }

    public boolean hasModifier(Modifier modifier) {
        return modifiers.contains(modifier);
    }

    void emit(CodeWriter codeWriter, boolean varargs) throws IOException {
        codeWriter.emitAnnotations(annotations, true);
        codeWriter.emitModifiers(modifiers);
        if (varargs) {
            TypeName.asArray(type).emit(codeWriter, true);
        } else {
            type.emit(codeWriter);
        }
        codeWriter.emit(" $L", name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        try {
            CodeWriter codeWriter = new CodeWriter(out);
            emit(codeWriter, false);
            return out.toString();
        } catch (IOException e) {
            throw new AssertionError();
        }
    }

    public Builder toBuilder() {
        return toBuilder(type, name);
    }

    Builder toBuilder(TypeName type, String name) {
        Builder builder = new Builder(type, name);
        builder.annotations.addAll(annotations);
        builder.modifiers.addAll(modifiers);
        return builder;
    }

    public static final class Builder {
        public final List<AnnotationSpec> annotations = new ArrayList<>();
        public final List<Modifier> modifiers = new ArrayList<>();
        private final TypeName type;
        private final String name;
        private final CodeBlock.Builder javadoc = CodeBlock.builder();

        private Builder(TypeName type, String name) {
            this.type = type;
            this.name = name;
        }

        public Builder addJavadoc(String format, Object... args) {
            javadoc.add(format, args);
            return this;
        }

        public Builder addJavadoc(CodeBlock block) {
            javadoc.add(block);
            return this;
        }

        public Builder addAnnotations(Iterable<AnnotationSpec> annotationSpecs) {
            checkArgument(annotationSpecs != null, "annotationSpecs == null");
            for (AnnotationSpec annotationSpec : annotationSpecs) {
                annotations.add(annotationSpec);
            }
            return this;
        }

        public Builder addAnnotation(AnnotationSpec annotationSpec) {
            annotations.add(annotationSpec);
            return this;
        }

        public Builder addAnnotation(ClassName annotation) {
            annotations.add(AnnotationSpec.builder(annotation).build());
            return this;
        }

        public Builder addAnnotation(Class<?> annotation) {
            return addAnnotation(ClassName.get(annotation));
        }

        public Builder addModifiers(Modifier... mods) {
            Collections.addAll(modifiers, mods);
            return this;
        }

        public Builder addModifiers(Iterable<Modifier> mods) {
            checkNotNull(mods, "modifiers == null");
            for (Modifier modifier : mods) {
                if (!modifier.equals(Modifier.FINAL)) {
                    throw new IllegalStateException("unexpected parameter modifier: " + modifier);
                }
                modifiers.add(modifier);
            }
            return this;
        }

        public ParameterSpec build() {
            return new ParameterSpec(this);
        }
    }
}
