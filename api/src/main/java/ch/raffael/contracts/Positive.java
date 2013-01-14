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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.raffael.contracts.meta.Equivalent;


/**
 * <p>The parameter or return value be greater than 0.</p>
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Documented
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.CLASS)
@Equivalent(
        asEnsure = "@result() > 0",
        onParameter = "@param() > 0")
public @interface Positive {

}
