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
package ch.raffael.contracts.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Regex {

    private final Pattern pattern;

    public Regex(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean matches(CharSequence input) {
        return pattern.matcher(input).matches();
    }

    public boolean contains(CharSequence input) {
        return pattern.matcher(input).find();
    }

    public int count(CharSequence input) {
        int count = 0;
        Matcher matcher = pattern.matcher(input);
        while ( matcher.find() ) {
            count++;
        }
        return count;
    }

}
