/*
 * Copyright 2012-2014 Raffael Herzog
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
package ch.raffael.contracts.processor.cel.skeletons;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.Method;

import static org.objectweb.asm.Opcodes.ASM5;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class SkeletonClassLoader extends ClassLoader {

    static {
        registerAsParallelCapable();
    }

    final Map<Class<?>, Map<Method, Integer>> lineNumbers = new MapMaker().concurrencyLevel(1).makeMap();

    private final ClassFileLoader classFileLoader;

    private static final ConcurrentMap<ClassLoader, SkeletonClassLoader> SKELETON_LOADERS =
            new MapMaker().concurrencyLevel(1).weakKeys().makeMap();

    public SkeletonClassLoader(ClassFileLoader classFileLoader) {
        super(null);
        this.classFileLoader = classFileLoader;
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        byte[] byteCode;
        try {
            byteCode = classFileLoader.loadClassFile(className);
        }
        catch ( IOException e ) {
            throw new ClassNotFoundException("I/O error loading class file for " + className, e);
        }
        if ( byteCode == null ) {
            throw new ClassNotFoundException(className);
        }
        ClassReader reader = new ClassReader(byteCode);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ImmutableMap.Builder<Method, Integer> lineNumbers = ImmutableBiMap.builder();
        reader.accept(new ClassVisitor(ASM5, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if ( name.equals("<clinit>") ) {
                    return null;
                }
                else {
                    return new SkeletonMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc, className) {
                        @Override
                        public void visitEnd() {
                            if ( getLineNumber() >= 0 ) {
                                lineNumbers.put(new Method(name, desc), getLineNumber());
                            }
                        }
                    };
                }
            }
        }, 0);
        byte[] skeletonByteCode = writer.toByteArray();
        Class<?> skeletonClass = defineClass(className, skeletonByteCode, 0, skeletonByteCode.length);
        this.lineNumbers.put(skeletonClass, lineNumbers.build());
        return skeletonClass;
    }
}
