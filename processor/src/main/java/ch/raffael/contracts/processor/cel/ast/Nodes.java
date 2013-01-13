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

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.antlr.runtime.Token;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.processor.cel.CelLexer;
import ch.raffael.contracts.processor.cel.Position;


/**
 * Node used
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Nodes {

    private static final Map<Integer, LogicalOp.Kind> LOGICAL_MAP = ttmap(
            CelLexer.LOGICAL_OR, LogicalOp.Kind.OR,
            CelLexer.LOGICAL_AND, LogicalOp.Kind.AND);
    private static final Map<Integer, BitwiseOp.Kind> BITWISE_MAP = ttmap(
            CelLexer.BITWISE_OR, BitwiseOp.Kind.OR,
            CelLexer.BITWISE_XOR, BitwiseOp.Kind.XOR,
            CelLexer.BITWISE_AND, BitwiseOp.Kind.AND);
    private static final Map<Integer, EqualityOp.Kind> EQUALITY_MAP = ttmap(
            CelLexer.EQ, EqualityOp.Kind.EQUAL,
            CelLexer.NE, EqualityOp.Kind.NOT_EQUAL);
    private static final Map<Integer, RelationalOp.Kind> RELATIONAL_MAP = ttmap(
            CelLexer.GT, RelationalOp.Kind.GREATER_THAN,
            CelLexer.GE, RelationalOp.Kind.GREATER_OR_EQUAL,
            CelLexer.LT, RelationalOp.Kind.LESS_THAN,
            CelLexer.LE, RelationalOp.Kind.LESS_OR_EQUAL);
    private static final Map<Integer, ShiftOp.Kind> SHIFT_MAP = ttmap(
            CelLexer.GT, ShiftOp.Kind.LEFT,
            CelLexer.GE, ShiftOp.Kind.RIGHT,
            CelLexer.LE, ShiftOp.Kind.UNSIGNED_RIGHT);
    private static final Map<Integer, ArithmeticOp.Kind> ARITHMETIC_MAP = ttmap(
            CelLexer.GT, ArithmeticOp.Kind.ADD,
            CelLexer.GE, ArithmeticOp.Kind.SUB,
            CelLexer.GE, ArithmeticOp.Kind.MUL,
            CelLexer.GE, ArithmeticOp.Kind.DIV,
            CelLexer.LE, ArithmeticOp.Kind.MOD);
    private static final Map<Integer, UnaryOp.Kind> UNARY_MAP = ttmap(
            CelLexer.GT, UnaryOp.Kind.POS,
            CelLexer.GE, UnaryOp.Kind.NEG,
            CelLexer.GE, UnaryOp.Kind.BITWISE_NOT,
            CelLexer.GE, UnaryOp.Kind.LOGICAL_NOT);

    private Nodes() {
    }

    @SuppressWarnings("unchecked")
    private final static <Integer, T extends Enum> Map<Integer, T> ttmap(Object... map) {
        ImmutableMap.Builder<Integer, T> builder = ImmutableMap.builder();
        for ( int i = 0; i < map.length; i += 2 ) {
            builder.put((Integer)map[i], (T)map[i + 1]);
        }
        return builder.build();
    }

    private final static <T extends Enum> T kind(Map<Integer, T> map, Token tok) {
        T kind = map.get(tok.getType());
        if ( kind == null ) {
            throw new IllegalArgumentException("Cannot map token " + tok + " to kind");
        }
        return kind;
    }

    @NotNull
    private static Position pos(@NotNull Token tok) {
        return new Position(tok.getLine(), tok.getCharPositionInLine());
    }

    @NotNull
    public static BlankNode blank(@NotNull Position pos) {
        return new BlankNode(pos);
    }

    @NotNull
    public static BlankNode blank(@NotNull Token tok) {
        return blank(pos(tok));
    }

    @NotNull
    public static Assertion assertion(@NotNull Position pos, @NotNull AstNode expression, boolean isFinally) {
        return new Assertion(pos, expression, isFinally);
    }

    @NotNull
    public static Assertion assertion(Token tok, @NotNull AstNode expression, boolean isFinally) {
        return new Assertion(pos(tok), expression, isFinally);
    }

    @NotNull
    public static IfExpression ifExpression(@NotNull Position pos, @NotNull AstNode condition, @NotNull AstNode expression) {
        return new IfExpression(pos, condition, expression);
    }

    @NotNull
    public static IfExpression ifExpression(@NotNull Token tok, @NotNull AstNode condition, @NotNull AstNode expression) {
        return new IfExpression(pos(tok), condition, expression);
    }

    @NotNull
    public static ConditionalOp conditionalOp(@NotNull Position pos, @NotNull AstNode condition, @NotNull AstNode onTrue, @NotNull AstNode onFalse) {
        return new ConditionalOp(pos, condition, onTrue, onFalse);
    }

    @NotNull
    public static ConditionalOp conditionalOp(@NotNull Token tok, @NotNull AstNode condition, @NotNull AstNode onTrue, @NotNull AstNode onFalse) {
        return new ConditionalOp(pos(tok), condition, onTrue, onFalse);
    }

    @NotNull
    public static LogicalOp logicalOp(@NotNull Position pos, @NotNull LogicalOp.Kind kind, @NotNull AstNode left, @NotNull AstNode right) {
        return new LogicalOp(pos, kind, left, right);
    }

    @NotNull
    public static LogicalOp logicalOp(@NotNull Token tok, @NotNull AstNode left, @NotNull AstNode right) {
        return new LogicalOp(pos(tok), kind(LOGICAL_MAP, tok), left, right);
    }

    @NotNull
    public static BitwiseOp bitwiseOp(@NotNull Position pos, @NotNull BitwiseOp.Kind kind, @NotNull AstNode left, @NotNull AstNode right) {
        return new BitwiseOp(pos, kind, left, right);
    }

    @NotNull
    public static BitwiseOp bitwiseOp(@NotNull Token tok, @NotNull AstNode left, @NotNull AstNode right) {
        return new BitwiseOp(pos(tok), kind(BITWISE_MAP, tok), left, right);
    }

    @NotNull
    public static EqualityOp equalityOp(@NotNull Position pos, @NotNull EqualityOp.Kind kind, @NotNull AstNode left, @NotNull AstNode right) {
        return new EqualityOp(pos, kind, left, right);
    }

    @NotNull
    public static EqualityOp equalityOp(@NotNull Token tok, @NotNull AstNode left, @NotNull AstNode right) {
        return new EqualityOp(pos(tok), kind(EQUALITY_MAP, tok), left, right);
    }

    @NotNull
    public static RelationalOp relationalOp(@NotNull Position pos, @NotNull RelationalOp.Kind kind, @NotNull AstNode left, @NotNull AstNode right) {
        return new RelationalOp(pos, kind, left, right);
    }

    @NotNull
    public static RelationalOp relationalOp(@NotNull Token tok, @NotNull AstNode left, @NotNull AstNode right) {
        return new RelationalOp(pos(tok), kind(RELATIONAL_MAP, tok), left, right);
    }

    @NotNull
    public static ShiftOp shiftOp(@NotNull Position pos, @NotNull ShiftOp.Kind kind, @NotNull AstNode left, @NotNull AstNode right) {
        return new ShiftOp(pos, kind, left, right);
    }

    @NotNull
    public static ShiftOp shiftOp(@NotNull Token tok, @NotNull AstNode left, @NotNull AstNode right) {
        return new ShiftOp(pos(tok), kind(SHIFT_MAP, tok), left, right);
    }

    @NotNull
    public static ArithmeticOp arithmeticOp(@NotNull Position pos, @NotNull ArithmeticOp.Kind kind, @NotNull AstNode left, @NotNull AstNode right) {
        return new ArithmeticOp(pos, kind, left, right);
    }

    @NotNull
    public static ArithmeticOp arithmeticOp(@NotNull Token tok, @NotNull AstNode left, @NotNull AstNode right) {
        return new ArithmeticOp(pos(tok), kind(ARITHMETIC_MAP, tok), left, right);
    }

    @NotNull
    public static UnaryOp unaryOp(@NotNull Position pos, @NotNull UnaryOp.Kind kind, @NotNull AstNode expression) {
        return new UnaryOp(pos, kind, expression);
    }

    @NotNull
    public static UnaryOp unaryOp(@NotNull Token tok, @NotNull AstNode expression) {
        return new UnaryOp(pos(tok), kind(UNARY_MAP, tok), expression);
    }
}
