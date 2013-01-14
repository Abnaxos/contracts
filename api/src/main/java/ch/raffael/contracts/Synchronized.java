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
package ch.raffael.contracts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Syntax;

import ch.raffael.contracts.meta.Permanent;


/**
 * <p>Synchronize the method body on the specified object. All contracts will be evaluated
 * within this <code>synchronized</code> block.</p>
 *
 * <p>Note that <code>Synchronized("this")</code> is always implied if the method is
 * declared <code>synchronized</code>.</p>
 *
 * <p>Unlike most other contract annotations, this instrumentation will always be active,
 * even if the agent is not loaded.</p>
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@Permanent
public @interface Synchronized {

    @Syntax("Cel") String value() default "this";

}
