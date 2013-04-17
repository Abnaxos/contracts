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
import java.util.regex.Pattern;

import javax.annotation.Syntax;

import ch.raffael.contracts.meta.Equivalent;


/**
 * The parameter or return value must match a regular expression. Only applicable
 * to {@link CharSequence}.
 *
 * @see Pattern
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Documented
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.CLASS)
@Equivalent(
        asEnsure = "@regex(pattern(), flags()).matches(@result())",
        onParameter = "@regex(pattern(), flags()).matches(@param())")
public @interface Matches {

    @Syntax("Regex") String pattern();

    int flags() default 0;

}
