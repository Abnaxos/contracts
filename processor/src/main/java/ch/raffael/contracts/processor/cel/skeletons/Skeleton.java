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

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Skeleton {

    private final Class<?> skeletonClass;
    private final TypeToken<?> typeToken;
    private final Type asmType;
    private final Map<Method, Integer> lineNumbers;

    Skeleton(Class<?> skeletonClass) {
        this.skeletonClass = skeletonClass;
        typeToken = TypeToken.of(skeletonClass);
        asmType = Type.getType(skeletonClass);
        lineNumbers = ((SkeletonClassLoader)skeletonClass.getClassLoader()).lineNumbers.getOrDefault(skeletonClass, ImmutableMap.of());
    }

    public String className() {
        return skeletonClass.getName();
    }

    public Class<?> skeletonClass() {
        return skeletonClass;
    }

    public TypeToken<?> typeToken() {
        return typeToken;
    }

    public Type asmType() {
        return asmType;
    }

    public Integer getLineNumber(Method method) {
        return lineNumbers.get(method);
    }

    public Integer getLineNumber(java.lang.reflect.Method method) {
        if ( !method.getDeclaringClass().equals(skeletonClass) ) {
            return null;
        }
        else {
            return lineNumbers.get(Method.getMethod(method));
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        return skeletonClass.equals(((Skeleton)o).skeletonClass);
    }

    @Override
    public int hashCode() {
        return skeletonClass.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("skeletonClass", skeletonClass)
                .toString();
    }
}
