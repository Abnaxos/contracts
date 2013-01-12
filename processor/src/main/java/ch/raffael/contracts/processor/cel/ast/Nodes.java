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
package ch.raffael.contracts.processor.cel.ast;

import ch.raffael.contracts.NotNull;


/**
 * Node used
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Nodes {

    private Nodes() {
    }

    @NotNull
    public static BlankNode blank() {
        return new BlankNode();
    }

    @NotNull
    public static Assertion assertion() {
        return new Assertion();
    }

    @NotNull
    public static Conditional conditional(@NotNull CelNode condition, @NotNull CelNode onTrue, @NotNull CelNode onFalse) {
        return new Conditional(condition, onTrue, onFalse);
    }

    @NotNull
    public static LogicalOr logicalOr(@NotNull CelNode left, @NotNull CelNode right) {
        return new LogicalOr(left, right);
    }

    @NotNull
    public static LogicalAnd logicalAnd(@NotNull CelNode left, @NotNull CelNode right) {
        return new LogicalAnd(left, right);
    }

    @NotNull
    public static BitwiseOr bitwiseOr(@NotNull CelNode left, @NotNull CelNode right) {
        return new BitwiseOr(left, right);
    }

    @NotNull
    public static BitwiseXor bitwiseXor(@NotNull CelNode left, @NotNull CelNode right) {
        return new BitwiseXor(left, right);
    }

    @NotNull
    public static BitwiseAnd bitwiseAnd(@NotNull CelNode left, @NotNull CelNode right) {
        return new BitwiseAnd(left, right);
    }

    public static Equal equal(@NotNull CelNode left, @NotNull CelNode right) {
        return new Equal(left, right);
    }

    public static NotEqual notEqual(@NotNull CelNode left, @NotNull CelNode right) {
        return new NotEqual(left, right);
    }

    public static GreaterThan greaterThan(@NotNull CelNode left, @NotNull CelNode right) {
        return new GreaterThan(left, right);
    }

    public static GreaterOrEqual greaterOrEqual(@NotNull CelNode left, @NotNull CelNode right) {
        return new GreaterOrEqual(left, right);
    }

    public static LessThan lessThan(@NotNull CelNode left, @NotNull CelNode right) {
        return new LessThan(left, right);
    }

    public static LessOrEqual lessOrEqual(@NotNull CelNode left, @NotNull CelNode right) {
        return new LessOrEqual(left, right);
    }

    public static LeftShift leftShift(@NotNull CelNode left, @NotNull CelNode right) {
        return new LeftShift(left, right);
    }

    public static RightShift rightShift(@NotNull CelNode left, @NotNull CelNode right) {
        return new RightShift(left, right);
    }

    public static UnsignedRightShift unsignedRightShift(@NotNull CelNode left, @NotNull CelNode right) {
        return new UnsignedRightShift(left, right);
    }

    public static Addition addition(@NotNull CelNode left, @NotNull CelNode right) {
        return new Addition(left, right);
    }

    public static Substraction substraction(@NotNull CelNode left, @NotNull CelNode right) {
        return new Substraction(left, right);
    }

    public static Multiplication multiplication(@NotNull CelNode left, @NotNull CelNode right) {
        return new Multiplication(left, right);
    }

    public static Division division(@NotNull CelNode left, @NotNull CelNode right) {
        return new Division(left, right);
    }

    public static Modulo modulo(@NotNull CelNode left, @NotNull CelNode right) {
        return new Modulo(left, right);
    }

}
