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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Store parameter names of methods.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ParameterMap {

    /**
     * Method list of the top-level class.
     */
    Method[] methods();

    /**
     * Inner classes.
     */
    InnerClass[] innerClasses();

    /**
     * Map method descriptor to parameter names.
     */
    @Retention(RetentionPolicy.CLASS)
    @interface Method {
        /**
         * The method descriptor in internal format (e.g.
         * {@code equals(Ljava/lang/Object;)Z)}.
         */
        String descriptor();
        String[] parameterNames();
    }

    @Retention(RetentionPolicy.CLASS)
    @interface InnerClass {
        /**
         * The internal name of the inner class.
         */
        String name();
        Method[] methods();
    }

}
