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
package ch.raffael.contracts.processor;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface Log {

    void error(int line, String message, String... args);
    void warn(int line, String message, String... args);
    void info(int line, String message, String... args);
    void debug(int line, String message, String... args);

    void error(String message, String... args);
    void warn(String message, String... args);
    void info(String message, String... args);
    void debug(String message, String... args);

    Log forResource(String resource);

}
