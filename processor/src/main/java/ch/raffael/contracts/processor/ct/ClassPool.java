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
package ch.raffael.contracts.processor.ct;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.Nullable;
import ch.raffael.contracts.Require;
import ch.raffael.contracts.processor.ASM;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ClassPool {

    private final ClassPool parent;
    private final List<ClassSource> sources = new LinkedList<>();
    private final Map<ClassName, CtClass> classes = new HashMap<>();

    public ClassPool(@Nullable ClassPool parent) {
        this.parent = parent;
    }

    public ClassPool(@Nullable ClassPool parent, @Nullable ClassSource... sources) {
        this(parent);
        if ( sources != null ) {
            this.sources.addAll(Arrays.asList(sources));
        }
    }

    public ClassPool(@Nullable ClassPool parent, @Nullable Iterable<ClassSource> sources) {
        this(parent);
        if ( sources != null ) {
            Iterables.addAll(this.sources, sources);
        }
    }

    public synchronized void addSource(@NotNull ClassSource source) {
        sources.add(source);
    }

    @NotNull
    public synchronized CtClass get(ClassName name) throws NotFoundException {
        if ( classes.containsKey(name) ) {
            return classes.get(name);
        }
        if ( parent != null ) {
            try {
                return parent.get(name);
            }
            catch ( NotFoundException e ) {
                // ignore, we're going to try to find it ourselves
            }
        }
        ClassName searchName = name.withArrayDepth(0);
        CtClass ctClass = null;
        if ( searchName.isPrimitive() ) {
            ctClass = new CtClass(null, this, searchName, null, null, null);
            classes.put(name, ctClass);
        }
        else {
            for ( ClassSource source : sources ) {
                try {
                    URL url = source.findClass(searchName);
                    if ( url != null ) {
                        ctClass = loadCtClass(searchName, url);
                        classes.put(name, ctClass);
                        break;
                    }
                }
                catch ( IOException e ) {
                    throw new NotFoundException("Error loading class " + name.toBinary(), e);
                }
            }
        }
        if ( ctClass == null ) {
            throw new NotFoundException("Class " + name.toBinary() + " not found");
        }
        for ( int i = 0; i < name.getArrayDepth(); i++ ) {
            searchName = searchName.withArrayDepth(i + 1);
            ctClass = new CtClass(null, this, searchName, get(ClassName.OBJECT),
                                  new CtClass[] { get(ClassName.SERIALIZABLE), get(ClassName.CLONEABLE) },
                                  ctClass);
            classes.put(searchName, ctClass);
        }
        return ctClass;
    }

    @NotNull
    private CtClass loadCtClass(
            @NotNull @Require("name.arrayDepth==0 && !name.isPrimitive") ClassName name,
            @NotNull URL source)
            throws IOException, NotFoundException
    {
        ClassReader reader;
        try ( InputStream stream = new BufferedInputStream(source.openStream()) ) {
            reader = new ClassReader(stream);
        }
        CtClassLoader loader = new CtClassLoader(source);
        try {
            reader.accept(loader, 0);
        }
        catch ( AsmException e ) {
            if ( e.getCause() instanceof NotFoundException ) {
                throw (NotFoundException)e.getCause();
            }
            else {
                throw new NotFoundException("Unexpected error loading class " + name.toInternal() + " from " + source, e.getCause());
            }
        }
        return loader.ctClass;
    }

    private class CtClassLoader extends ClassVisitor {

        private final URL source;

        private CtClass ctClass;

        private CtClassLoader(URL source) {
            super(ASM.API);
            this.source = source;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaceNames) {
            try {
                CtClass superClass = null;
                if ( superName != null ) {
                    superClass = get(ClassName.forInternal(superName));
                }
                CtClass[] interfaces = null;
                if ( interfaceNames != null && interfaceNames.length > 0 ) {
                    interfaces = new CtClass[interfaceNames.length];
                    for ( int i = 0; i < interfaceNames.length; i++ ) {
                        interfaces[i] = get(ClassName.forInternal(interfaceNames[i]));
                    }
                }
                ctClass = new CtClass(source, ClassPool.this, ClassName.forInternal(name), superClass, interfaces, null);
            }
            catch ( IllegalClassNameException | NotFoundException e ) {
                throw new AsmException(e);
            }
        }
    }

}
