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
 * Annotations and runtime classes for contracts.
 *
 * Concepts
 * --------
 *
 * There are three main annotations:
 *
 * {@link ch.raffael.contracts.Require Require}
 * :   *Preconditions*: Conditions that must be true on method entry. This can also be
 *     used to check the method parameters. **Inheritance**: Inheriting classes may
 *     *extend* the contract, i.e. inherited preconditions will be *OR*-associated.
 *
 * {@link ch.raffael.contracts.Ensure Ensure}
 * :   *Postconditions*: Conditions that must be true on method entry. This can also be
 *     used to check the method's return value. **Inheritance**: Inheriting classes may
 *     further *constrain* the contract, i.e. inherited postconditions will be
 *     *AND*-associated.
 *
 * {@link ch.raffael.contracts.Invariant Invariant}
 * :   Conditions that must be true both on method entry and exit for each method of the
 * class. **Inheritance**: Inheriting classes may further *constrain* the contract, i.e.
 * inherited invariants will be *AND*-associated.
 *
 * The conditions are expressed using Cel expressions.
 *
 * The Contract Expression Language Cel
 * ------------------------------------
 *
 * Generally, Cel expressions are just Java expressions evaluated in the scope of the
 * class they are defined in. Conditions that are defined for a method (Require and
 * Ensure) additionally may access the method parameters (but not local variables). See
 * also the full [ANTLR3 grammar](processor/cel/Cel.g).
 *
 * There are, however, some limitations in these expressions:
 *
 *  *  **New instances**: Cel expressions cannot create new instances. The `new` operator
 *     is disabled.
 *
 *  *  **Assignments**: Cel expressions must be free of any side-effects, therefore they
 *     cannot change any values. All assignment operators, including pre- and
 *     post-increment and -decrement.
 *
 *  *  **Generics**: For now, Cel expressions operate on the erasure, there's no support
 *      for generics.
 *
 *  * **Autoboxing**: There is no support for autoboxing/-unboxing.
 *
 * On the other side, there are a few extensions to Java expressions described below.
 *
 *
 * ### String and character literals
 *
 * Contract expressions are embedded in the Java code as annotations. This means that
 * whenever you're writing a string literal, you'd have to escape the quotes:
 *
 * ```java
 * {@literal @}Require("foo.startsWith(\"bar\")")
 * {@literal @}Ensure("@result().startsWith(\"\\\"\")") // ouch!
 * ```
 *
 * To work around this issue, Cel also supports singe quotes for string literals:
 *
 * ```java
 * {@literal @}Require("foo.startsWith('bar')")
 * {@literal @}Ensure("@result().startsWith('\"')")
 * ```
 *
 * This, however, conflicts with char literals, which is why Cel introduces a new syntax
 * for them. Use single quotes as usual, but append a 'c' (case-insensitive):
 *
 * ```java
 * {@literal @}Require("foo=='X'c")
 * ```
 *
 *
 * ### Conditional conditions ;)
 *
 * Basically, this is just another way of writing logical OR. The advantage is that it's
 * more natural and intuitively understandable. An example:
 *
 * ```java
 * if ( myObject != null ) myObject.isValid()
 * ```
 *
 * Read: "If myObject is not null, it must be valid". This is a more readable way of
 * writing:
 *
 * ```java
 * myObject == null || myObject.isValid()
 * ```
 *
 *
 * ### Finally
 *
 * Postconditions, if not otherwise specified, will only be checked upon normal method
 * exit. To check them also when the method is throwing an exception, use `finally`:
 *
 * ```java
 * {@literal @}Ensure("finally a==b")
 * ```
 *
 * To check a post-condition only when throwing, use the {@code @thrown} function (see
 * below):
 *
 * ```java
 * {@literal @}Ensure("finally if(@thrown()) a==b
 * ```
 *
 *
 * ### Functions
 *
 * Functions provide some extended functionality needed to express contracts. All
 * functions start with an '@' character. The following functions are recognised:
 *
 * `@old(<expression>)`
 * :   *[Ensure, Invariant]*<br/>
 *     Evaluate the expression on method-entry but refer to its value on method-exit.
 *
 *     **Example**
 *
 *     ```java
 *     size() == @old(size()) + 1
 *     ```
 *     (e.g. postcondition for the add() method of a list)
 *
 * `@result()`
 * :   *[Ensure]*<br/>
 *     Refer to the return value of the method.
 *
 * `@thrown([<exception-class>])`
 * :   *[Ensure, Invariant]*<br/>
 *     Check that an exception has been thrown. If the exception is not specified,
 *     {@link java.lang.Throwable Throwable} is assumed. There are two variants:
 *
 *      *  `@thrown(IllegalStateException)` is true, if the method threw an
 *         IllegalStateException.
 *
 *      *  `@thrown(IllegalStateException).getMessage().equals("Foo")` is true, if the
 *         method threw an IllegalStateException and the message equals "foo".
 *
 * `@param([<index>])`
 * :   *[Require, Ensure]*<br/>
 *     Usually, you can (and should) refer to parameters by their name. This allows to
 *     access parameters by their index. If the annotation is on the method, the index is
 *     required and absolute (<code>@param(0)</code> refers to the first parameter). If
 *     the annotation is on a parameter, the index can be omitted and is relative:
 *     `@param(0)` refers to the annotated parameter, `@param(-1)` and `@param(+1)` refer
 *     to the parameter to the left and right of the annotated parameter. In this context,
 *     the index can be omitted: `@param()` refers to the annotated parameter.
 *
 *`@each(<expression>, <identifier> -> <expression>)`
 * :   *[Require, Ensure, Invariant]*<br/>
 *     Make sure an expression is true for each element of an `Iterable` or array.
 *
 *     **Example**
 *
 *     ```java
 *     {@literal @}each(getChildren(), child -> child.isValid())
 *     ```
 *     Ensure that each element of `getChildren()` is valid.
 *
 * `@equal(<expression>, <expression>)`
 * :   *[Require, Ensure, Invariant]*<br/>
 *     Null-safe equals. `@equal(a, b)` is the same as `a==null ? b==null : a.equals(b)`.
 *
 * `@regex(<regular-expression> [, <flags>])`
 * :   *[Require, Ensure, Invariant]*<br/>
 *     Shorthand to {@link java.util.regex.Pattern}. Specify flags as a comma separated
 *     list of the constants defined in `Pattern` (e.g.
 *     `@regex("ab+c.*", CASE_INSENSITIVE, DOTALL)`). The result of this function is a
 *     {@link ch.raffael.contracts.internal.Regex}.
 */
package ch.raffael.contracts;
