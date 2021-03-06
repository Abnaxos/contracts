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
package ch.raffael.contracts;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Syntax;

import ch.raffael.contracts.meta.Permanent;
import ch.raffael.contracts.util.NeedsWork;


/**
 * Surround the method body with lock()/unlock() on the specified
 * {@link java.util.concurrent.locks.Lock Lock} object. All contracts will be evaluated
 * while this lock is being held.
 *
 * Unlike most other contract annotations, this instrumentation will always be active,
 * even if the agent is not loaded.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@Permanent
@NeedsWork(description = "Consider using @Synchronized, choosing lock()/unlock() if the result of the expression is a Lock")
public @interface Locked {

    @Syntax("Cel") String value();

}
