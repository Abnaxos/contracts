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

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.antlr.v4.runtime.Token;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.Nullable;
import ch.raffael.contracts.Require;
import ch.raffael.contracts.processor.cel.Position;
import ch.raffael.contracts.processor.cel.parser.CelLexer;
import ch.raffael.contracts.util.NeedsWork;


/**
 * Node used
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@NeedsWork(description = "There shouldn't be any references to Token from here")
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
            CelLexer.LEFT_SHIFT, ShiftOp.Kind.LEFT,
            CelLexer.RIGHT_SHIFT, ShiftOp.Kind.RIGHT,
            CelLexer.URIGHT_SHIFT, ShiftOp.Kind.UNSIGNED_RIGHT);
    private static final Map<Integer, ArithmeticOp.Kind> ARITHMETIC_MAP = ttmap(
            CelLexer.ADD, ArithmeticOp.Kind.ADD,
            CelLexer.SUB, ArithmeticOp.Kind.SUB,
            CelLexer.MUL, ArithmeticOp.Kind.MUL,
            CelLexer.DIV, ArithmeticOp.Kind.DIV,
            CelLexer.MOD, ArithmeticOp.Kind.MOD);
    private static final Map<Integer, UnaryOp.Kind> UNARY_MAP = ttmap(
            CelLexer.ADD, UnaryOp.Kind.POS,
            CelLexer.SUB, UnaryOp.Kind.NEG,
            CelLexer.BITWISE_NOT, UnaryOp.Kind.BITWISE_NOT,
            CelLexer.LOGICAL_NOT, UnaryOp.Kind.LOGICAL_NOT);

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
    public static Clause clause(@NotNull Position pos, @NotNull AstNode expression, boolean isFinally) {
        return new Clause(pos, expression, isFinally);
    }

    @NotNull
    public static Clause clause(Token tok, @NotNull AstNode expression, boolean isFinally) {
        return new Clause(pos(tok), expression, isFinally);
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

    @NotNull
    public static IdReference idReference(@NotNull Position pos, @Nullable AstNode source, @NotNull String identifier) {
        return new IdReference(pos, source, identifier);
    }

    @NotNull
    public static IdReference idReference(@NotNull Token tok, @Nullable AstNode source, @NotNull String identifier) {
        return new IdReference(pos(tok), source, identifier);
    }

    @NotNull
    public static IdReference idReference(@NotNull Position pos, @NotNull String identifier) {
        return new IdReference(pos, null, identifier);
    }

    @NotNull
    public static IdReference idReference(@NotNull Token tok, @NotNull String identifier) {
        return new IdReference(pos(tok), null, identifier);
    }

    @NotNull
    public static MethodCall methodCall(@NotNull Position pos, @Nullable AstNode source, @NotNull String methodName, @NotNull List<AstNode> arguments) {
        return new MethodCall(pos, source, methodName, arguments);
    }

    @NotNull
    public static MethodCall methodCall(@NotNull Token tok, @Nullable AstNode source, @NotNull String methodName, @NotNull List<AstNode> arguments) {
        return new MethodCall(pos(tok), source, methodName, arguments);
    }

    @NotNull
    public static MethodCall methodCall(@NotNull Position pos, @NotNull String methodName, @NotNull List<AstNode> arguments) {
        return new MethodCall(pos, null, methodName, arguments);
    }

    @NotNull
    public static MethodCall methodCall(@NotNull Token tok, @NotNull String methodName, @NotNull List<AstNode> arguments) {
        return new MethodCall(pos(tok), null, methodName, arguments);
    }

    @NotNull
    public static ArrayAccess arrayAccess(@NotNull Position pos, @NotNull AstNode source, @NotNull AstNode index) {
        return new ArrayAccess(pos, source, index);
    }

    @NotNull
    public static ArrayAccess arrayAccess(@NotNull Token tok, @NotNull AstNode source, @NotNull AstNode index) {
        return new ArrayAccess(pos(tok), source, index);
    }

    @NotNull
    public static Literal literal(@NotNull Position pos,
                                  @NotNull Literal.Kind kind,
                                  @Require("kind.isValueCompatible(value)") Object value)
    {
        return new Literal(pos, kind, value);
    }

    @NotNull
    public static Literal literal(@NotNull Token tok,
                                  @NotNull Literal.Kind kind,
                                  @Require("kind.isValueCompatible(value)") Object value)
    {
        return new Literal(pos(tok), kind, value);
    }

}
