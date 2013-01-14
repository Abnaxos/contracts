/*
 * Copyright 2012-2013 Raffael Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.raffael.contracts.processor.pmap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor7;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ch.raffael.contracts.meta.NeedsWork;

import static ch.raffael.contracts.processor.ContractsProcessor.*;
import static org.objectweb.asm.Opcodes.*;


/**
 * <p></p>Processor gathering parameter names and writing them to the Contracts class as
 * {@link ParameterMap} annotations.</p>
 *
 * <p>Due to limitations of Java, gathering parameter names is a bit complicated. There
 * are two possible sources for parameter names:</p>
 *
 * <ul>
 *     <li>The source code using an annotation processor.</li>
 *
 *     <li>The local variable table in the debug information.</li>
 * </ul>
 *
 * <p>Both these sources have some drawbacks which make it impossible to gather
 * <em>all</em> parameter names:</p>
 *
 * <ul>
 *     <li>The annotation processor can't read parameter names of methods in anonymous
 *     classes (this includes Enum constants) or local classes. Annotation processing
 *     by stops by definition at code level, i.e. method bodies or field
 *     initializers. See also
 *     <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6587158">bug #6587158</a>.
 *     </li>
 *
 *     <li>A local variable table won't be present in abstract methods (which includes
 *     all interface methods). Abstract methods have no method body, therefore no local
 *     variables, therefore no local variable table.</li>
 * </ul>
 *
 * <p>However, if we combine these two methods, we can extract almost all parameter names:
 * Anonymous classes can't be abstract, so we can always extract the parameter names from
 * debug information. There's only one exception, where it's absolutely impossible to
 * collect parameter names: Abstract methods of local classes. I think, we can bear with
 * that. The <code>param()</code> function exists, after all. ;)</p>
 *
 * <p>Another possibility would be to use the
 * <a href="http://docs.oracle.com/javase/7/docs/jdk/api/javac/tree/index.html">Compiler
 * Tree API</a>, however this is only available for OpenJDK/Oracle JDK and very
 * complicated to use.</p>
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@NeedsWork(description = "This is a quick and dirty proof-of-concept, it may handle error conditions poorly")
public class ParameterMapAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Building parameter maps");
        for ( Element elem: roundEnv.getRootElements() ) {
            if ( elem.getKind() == ElementKind.CLASS || elem.getKind() == ElementKind.INTERFACE ) {
                // FIXME: enums & enum constants
                try {
                    TypeElement typeElem = (TypeElement)elem;
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing: " + ((TypeElement)elem).getQualifiedName());
                    String className = typeElem.getQualifiedName().toString();
                    if ( className.endsWith(CONTRACTS_CLASS_SUFFIX) ) {
                        continue;
                    }
                    String ctrClassName = className + CONTRACTS_CLASS_SUFFIX;
                    JavaFileObject contractsFile = processingEnv.getFiler().createClassFile(ctrClassName, elem);
                    ClassWriter classWriter = new ClassWriter(0);
                    classWriter.visit(V1_7, ACC_PUBLIC + ACC_SUPER, toInternalName(ctrClassName), null, null, null);
                    MethodVisitor ctor = classWriter.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
                    ctor.visitCode();
                    ctor.visitLabel(new Label());
                    ctor.visitEnd();
                    //classWriter.visitOuterClass(toInternalName(className), null, null);
                    AnnotationVisitor parameterMap = classWriter.visitAnnotation(Type.getDescriptor(ParameterMap.class), false);
                    writeType(parameterMap, typeElem, null, null);
                    parameterMap.visitEnd();
                    classWriter.visitEnd();
                    try ( OutputStream output = contractsFile.openOutputStream() ) {
                        output.write(classWriter.toByteArray());
                    }
                }
                catch ( IOException e ) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage());
                }
            }
        }
        return false;
    }

    private void writeType(AnnotationVisitor annotation, TypeElement element, String innerPrefix, AnnotationVisitor innerClassArray) {
        AnnotationVisitor methodsArray = annotation.visitArray("methods");
        for ( Element elem : element.getEnclosedElements() ) {
            String methodName;
            if ( elem.getKind() == ElementKind.CONSTRUCTOR ) {
                methodName = CTOR_NAME;
            }
            else if ( elem.getKind() == ElementKind.METHOD ) {
                methodName = elem.getSimpleName().toString();
            }
            else {
                continue;
            }
            AnnotationVisitor methodAnnotation = methodsArray.visitAnnotation(null, Type.getDescriptor(ParameterMap.Method.class));
            methodAnnotation.visit("descriptor", getMethodDescriptor((ExecutableElement)elem));
            AnnotationVisitor paramNames = methodAnnotation.visitArray("parameterNames");
            for ( VariableElement param : ((ExecutableElement)elem).getParameters() ) {
                paramNames.visit(null, param.getSimpleName().toString());
            }
            paramNames.visitEnd();
            methodAnnotation.visitEnd();
        }
        methodsArray.visitEnd();
        boolean endInnerClassArray = false;
        if ( innerClassArray == null ) {
            innerClassArray = annotation.visitArray("innerClasses");
            endInnerClassArray = true;
        }
        for ( Element innerElem : element.getEnclosedElements() ) {
            if ( isType(innerElem) ) {
                AnnotationVisitor innerClass = innerClassArray.visitAnnotation(null, Type.getDescriptor(ParameterMap.InnerClass.class));
                String name;
                if ( innerPrefix == null ) {
                    name = innerElem.getSimpleName().toString();
                }
                else {
                    name = innerPrefix + "." + innerElem.getSimpleName();
                }
                innerClass.visit("name", name);
                writeType(innerClass, (TypeElement)innerElem, name, innerClassArray);
                innerClass.visitEnd();
            }
        }
        if ( endInnerClassArray ) {
            innerClassArray.visitEnd();
        }
    }

    private boolean isType(Element elem) {
        return elem.getKind() == ElementKind.CLASS
                || elem.getKind() == ElementKind.INTERFACE
                || elem.getKind() == ElementKind.ENUM;
    }

    private String getMethodDescriptor(ExecutableElement element) {
        final StringBuilder buf = new StringBuilder();
        buf.append(element.getSimpleName()).append('(');
        TypeVisitor<Void, Void> typeWriter = new BinaryTypeWriter(buf);
        for ( VariableElement param : element.getParameters() ) {
            TypeMirror type = processingEnv.getTypeUtils().erasure(param.asType());
            type.accept(typeWriter, null);
        }
        buf.append(')');
        processingEnv.getTypeUtils().erasure(element.getReturnType()).accept(typeWriter, null);
        return buf.toString();
    }

    private class BinaryTypeWriter extends SimpleTypeVisitor7<Void, Void> {

        private final StringBuilder buf;

        public BinaryTypeWriter(StringBuilder buf) {
            this.buf = buf;
        }

        @Override
        public Void visitPrimitive(PrimitiveType t, Void p) {
            switch ( t.getKind() ) {
                case BOOLEAN:
                    buf.append('Z');
                    break;
                case BYTE:
                    buf.append('B');
                    break;
                case SHORT:
                    buf.append('S');
                    break;
                case INT:
                    buf.append('I');
                    break;
                case LONG:
                    buf.append('J');
                    break;
                case CHAR:
                    buf.append('C');
                    break;
                case FLOAT:
                    buf.append('F');
                    break;
                case DOUBLE:
                    buf.append('D');
                    break;
                case VOID:
                    buf.append('V');
                    break;
                default:
                    return defaultAction(t, p);
            }
            return null;
        }

        @Override
        public Void visitNoType(NoType t, Void p) {
            if ( t.getKind() == TypeKind.VOID ) {
                buf.append('V');
                return null;
            }
            else {
                return defaultAction(t, p);
            }
        }

        @Override
        public Void visitArray(ArrayType t, Void p) {
            buf.append('[');
            return t.getComponentType().accept(this, null);
        }

        @Override
        public Void visitDeclared(DeclaredType t, Void p) {
            buf.append('L');
            buf.append(toInternalName(processingEnv.getElementUtils().getBinaryName((TypeElement)t.asElement()).toString()));
            buf.append(';');
            return null;
        }
    }
}
