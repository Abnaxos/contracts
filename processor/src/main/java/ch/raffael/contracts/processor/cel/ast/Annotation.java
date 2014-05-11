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
package ch.raffael.contracts.processor.cel.ast;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import ch.raffael.util.common.UnexpectedException;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Annotation<T> {

    private final Class<T> annotationValueType;
    private String annotationName;

    private Annotation(Class<T> type) {
        this.annotationValueType = type;
    }

    @Override
    public String toString() {
        return "<" + annotationName + ":" + (annotationValueType.getPackage().getName().equals("java.lang") ? annotationValueType.getSimpleName() : annotationValueType.getName()) + ">";
    }

    public String name() {
        return annotationName;
    }

    private static <T> Annotation<T> annotation(Class<T> type) {
        return new Annotation<T>(type);
    }

    static {
        try {
            for ( Field field : Annotation.class.getDeclaredFields() ) {
                if ( Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && field.getType() == Annotation.class ) {
                    Annotation<?> annotation = (Annotation<?>)field.get(null);
                    annotation.annotationName = field.getName();
                }
            }
        }
        catch ( Exception e ) {
            throw new UnexpectedException(e);
        }
    }

}
