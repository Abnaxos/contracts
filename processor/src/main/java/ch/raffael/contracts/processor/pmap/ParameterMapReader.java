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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static com.google.common.collect.Maps.*;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ParameterMapReader {

    private final Map<String, Map<String, String[]>> classes = newLinkedHashMap();

    private String className;

    private Map<String, String[]> currentClass;

    public void read(ClassReader reader) {
        reader.accept(new RootVisitor(), 0);
    }

    private class RootVisitor extends ClassVisitor {

        public RootVisitor() {
            super(Opcodes.ASM4);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            className = name;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if ( desc.equals(Type.getDescriptor(ParameterMap.class)) ) {
                return new ParameterMapVisitor();
            }
            else {
                return null;
            }
        }
    }

    private class AbstractAnnotationVisitor extends AnnotationVisitor {
        private AbstractAnnotationVisitor() {
            super(Opcodes.ASM4);
        }
    }

    private class ParameterMapVisitor extends AbstractAnnotationVisitor {
        private final MethodListVisitor methodList = new MethodListVisitor();
        private ParameterMapVisitor() {
            classes.put(null, methodList.methods);
        }
        @Override
        public AnnotationVisitor visitArray(String name) {
            if ( name.equals("methods") ) {
                return methodList;
            }
            else if ( name.equals("innerClasses") ) {
                return new InnerClassListVisitor();
            }
            else {
                return null;
            }
        }
    }

    private class MethodListVisitor extends AbstractAnnotationVisitor {
        private final Map<String, String[]> methods = newLinkedHashMap();
        private MethodListVisitor() {
        }
        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            if ( desc.equals(Type.getDescriptor(ParameterMap.Method.class)) ) {
                return new MethodVisitor(methods);
            }
            else {
                return null;
            }
        }
    }

    private class MethodVisitor extends AbstractAnnotationVisitor {
        private final Map<String, String[]> target;
        private String descriptor;
        private final List<String> parameterNames = Lists.newArrayList();
        private MethodVisitor(Map<String, String[]> target) {
            this.target = target;
        }
        @Override
        public void visit(String name, Object value) {
            if ( name.equals("descriptor") ) {
                descriptor = (String)value;
            }
        }
        @Override
        public AnnotationVisitor visitArray(String name) {
            if ( name.equals("parameterNames") ) {
                return new ParameterNamesVisitor(parameterNames);
            }
            else {
                return null;
            }
        }
        @Override
        public void visitEnd() {
            target.put(descriptor, parameterNames.toArray(new String[parameterNames.size()]));
        }
    }

    private class ParameterNamesVisitor extends AbstractAnnotationVisitor {
        private final List<String> parameterNames;
        private ParameterNamesVisitor(List<String> parameterNames) {
            this.parameterNames = parameterNames;
        }
        @Override
        public void visit(String name, Object value) {
            parameterNames.add((String)value);
        }
    }

    private class InnerClassListVisitor extends AbstractAnnotationVisitor {

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            if ( desc.equals(Type.getDescriptor(ParameterMap.InnerClass.class)) ) {
                return new InnerClassVisitor();
            }
            else {
                return null;
            }
        }
    }

    private class InnerClassVisitor extends AbstractAnnotationVisitor {
        private String name;
        private final MethodListVisitor methodList = new MethodListVisitor();
        @Override
        public void visit(String name, Object value) {
            if ( name.equals("name") ) {
                this.name = (String)value;
            }
        }
        @Override
        public AnnotationVisitor visitArray(String name) {
            if ( name.equals("methods") ) {
                return methodList;
            }
            else {
                return null;
            }
        }
        @Override
        public void visitEnd() {
            if ( classes.containsKey(name) ) {
                throw new InvalidPMapException("Duplicate class: " + name);
            }
            classes.put(name, methodList.methods);
        }
    }

    public static void main(String[] args) throws Exception {
        Files.walkFileTree(Paths.get(args[0]), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.toString();
                if ( name.endsWith(".class") ) {
                    dumpFile(name);
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void dumpFile(String fileName) throws IOException {
        ParameterMapReader map = new ParameterMapReader();
        try ( InputStream in = new BufferedInputStream(new FileInputStream(fileName)) ) {
            ClassReader reader = new ClassReader(in);
            map.read(reader);
        }
        for ( Map.Entry<String, Map<String, String[]>> classEntry : map.classes.entrySet() ) {
            String indent;
            if ( classEntry.getKey() == null ) {
                System.out.println(map.className);
                indent = "  ";
            }
            else {
                System.out.println("  Inner class: " + classEntry.getKey());
                indent = "    ";
            }
            for ( Map.Entry<String, String[]> method : classEntry.getValue().entrySet() ) {
                System.out.println(indent + method.getKey() + ": " + Arrays.asList(method.getValue()));
            }
        }
    }

}
