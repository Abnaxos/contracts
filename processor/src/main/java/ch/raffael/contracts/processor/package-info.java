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

/**
 * The compile-time processor for contracts.
 *
 * The compile-time processor is as non-intrusive as possible. To achieve this goal
 * while retaining full access to all private members of the class being extended without
 * having to use reflection, all code and meta information is kept in a separate private
 * nested class. This class will be named `OriginalClass$$ch$raffael$contracts`. Using
 * this trick, the processor can do all the (complex) compilation of the contracts leaving
 * the original class almost untouched: All that's added to the original class is an entry
 * in its inner class table. The runtime agent, if enabled, will then instrument the
 * original code using standard
 * {@link java.lang.instrument.Instrumentation Instrumentation} at class loading time.
 *
 * Definitions
 * -----------
 *
 * ### Class names
 *
 * Natural class name
 * :   The natural class name, as it's used in Java source code. Primitives are untouched.
 *
 * Binary class name
 * :   The binary class name. One example of a binary name is `OuterClass.InnerClass`
 *     (natural name) which becomes `OuterClass$InnerClass` (binary name). Primitives are
 *     untouched. These are the names used to look up classes using
 *     {@link java.lang.Class#forName(String) Class#forName()}. Note that arrays of
 *     primitives will be the same as the class descriptor (`int[]` &rarr; `[I`, arrays
 *     of objects will be a mix of the internal class name and its descriptor
 *     (`java.lang.String[]` &rarr; `[Ljava.lang.String;`).
 *
 * Internal class name
 * :   The internal class name, as used in the JVM. This is the same as the binary name,
 *     but '.' replaced with '/': java/lang/String. Primitives are untouched.
 *
 * Class descriptor
 * :   The class descriptor as used e.g. in method signatures. `int` becomes `I`,
 *     `java.lang.String` becomes `Ljava/lang/String;`.
 *
 * @todo The statements about access to private members are not entirely true; *javac* has
 * to generate package local accessor methods to allow the inner class access to private
 * members of its outer class, so we'll have to do the same. Therefore, there's no need
 * anymore to add the contracts class as inner class. Investigate that further, but right
 * now, it's a rather unimportant implementation detail. The concept stays the same:
 * Contract code is compiled into a separate class.
 *
 * Fun fact: If the contracts class isn't an inner class, we may actually consider
 * generating Java source code and compiling it using {@link javax.tools.JavaCompiler},
 * thus avoiding having to generate the byte code ourselves. Then again, generating the
 * bytecode is the least of the problems. ;)
 */
package ch.raffael.contracts.processor;
