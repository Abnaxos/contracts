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
 * <p>The compile-time processor for contracts.</p>
 *
 * <p>The compile-time processor is as non-intrusive as possible. To achieve this goal
 * while retaining full access to all private members of the class being extended without
 * having to use reflection, all code and meta information is kept in a separate private
 * nested class. This class will be named
 * <code>OriginalClass$$ch$raffael$contracts</code>. Using this trick, the processor can
 * do all the (complex) compilation of the contracts leaving the original class almost
 * untouched: All that's added to the original class is an entry in its inner class table.
 * The runtime agent, if enabled, will then instrument the original code using standard
 * {@link java.lang.instrument.Instrumentation Instrumentation} at class loading time.</p>
 *
 * <p><strong>TODO:</strong> The above is not entirely true; <em>javac</em> has to
 * generate package local accessor methods to allow the inner class access to private
 * members of its outer class, so we'll have to do the same. Therefore, there's no need
 * anymore to add the contracts class as inner class. Investigate that further, but right
 * now, it's a rather unimportant implementation detail. The concept stays the same:
 * Contract code is compiled into a separate class.</p>
 *
 * <p>Fun fact: If the contracts class isn't an inner class, we may actually consider
 * generating Java source code and compiling it using {@link javax.tools.JavaCompiler},
 * thus avoiding having to generate the byte code ourselves. Then again, generating the
 * bytecode is the least of the problems. ;)</p>
 *
 * <h2>Definitions</h2>
 *
 * <h3>Class names</h3>
 *
 * <dl>
 *     <dt>Natural class name</dt>
 *     <dd>The natural class name, as it's used in Java source code. Primitives are
 *     untouched.</dd>
 *
 *     <dt>Binary class name</dt>
 *     <dd>The binary class name. One example of a binary name is
 *     {@code OuterClass.InnerClass} (natural name) which becomes
 *     {@code OuterClass$InnerClass} (binary name). Primitives are untouched. These are
 *     the names used to look up classes using
 *     {@link java.lang.Class#forName(String) Class#forName()}. Note that arrays of
 *     primitives will be the same as the class descriptor ({@code int[]} &rarr;
 *     {@code [I}), arrays of objects will be a mix of the internal class name and
 *     its descriptor ({@code java.lang.String[]} &rarr; {@code [Ljava.lang.String;}).
 *     </dd>
 *
 *     <dt>Internal class name</dt>
 *     <dd>The internal class name, as used in the JVM. This is the same as the binary
 *     name, but '.' replaced with '/': java/lang/String. Primitives are untouched.</dd>
 *
 *     <dt>Class descriptor</dt>
 *     <dd>The class descriptor as used e.g. in method signatures. {@code int} becomes
 *     {@code I}, {@code java.lang.String} becomes {@code Ljava/lang/String;}.</dd>
 * </dl>
 */
package ch.raffael.contracts.processor;
