/*
 * Copyright 2012-2014 Raffael Herzog
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
package ch.raffael.contracts.processor.cel.parser;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.TerminalNode;

import ch.raffael.contracts.processor.cel.Position;
import ch.raffael.contracts.processor.cel.ast.AstNode;
import ch.raffael.contracts.processor.cel.ast.Literal;
import ch.raffael.contracts.processor.cel.ast.Nodes;
import ch.raffael.util.common.NotImplementedException;
import ch.raffael.util.common.UnreachableCodeException;

import static ch.raffael.contracts.processor.cel.parser.CelParser.ClauseContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.ExpressionContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.FactorContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.FinallyExpressionContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.LiteralContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.MethodCallContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.ParenExpressionContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.PrimaryContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.SelectorContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.ThrowExpressionContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.TopLevelExprContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.UnaryContext;
import static ch.raffael.contracts.processor.cel.parser.CelParser.UnaryNoPosNegContext;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class AstBuilder extends CelBaseListener {

    public CelParser install(CelParser parser) {
        parser.setErrorHandler(new ErrorStrategy(parser.getErrorHandler()));
        parser.addParseListener(this);
        return parser;
    }

    @Override
    public void exitClause(ClauseContext ctx) {
        Position pos;
        if ( ctx.directPost != null ) {
            pos = new Position(ctx.directPost);
        }
        else {
            pos = ctx.pre.node.getPosition();
        }
        ctx.node = Nodes.clause(pos, ctx.pre.node, ctx.post.stream().map(c -> c.node).iterator());
    }

    @Override
    public void exitTopLevelExpr(@NotNull TopLevelExprContext ctx) {
        if ( ctx.expression() != null ) {
            ctx.node = ctx.expression().node;
        }
        else if ( ctx.throwExpression() != null ) {
            ctx.node = ctx.expression().node;
        }
        else if ( ctx.finallyExpression() != null ) {
            ctx.node = ctx.finallyExpression().node;
        }
    }

    @Override
    public void exitThrowExpression(@NotNull ThrowExpressionContext ctx) {
        ctx.node = Nodes.throwExpr(ctx.THROW().getSymbol(), ctx.classRef().className, ctx.ID().getText(),
                                   ctx.expression() == null ? null : ctx.expression().node);
    }

    @Override
    public void exitFinallyExpression(@NotNull FinallyExpressionContext ctx) {
        ctx.node = Nodes.finallyExpr(ctx.FINALLY().getSymbol(), ctx.expression().node);
    }

    @Override
    public void exitParenExpression(@NotNull ParenExpressionContext ctx) {
        ctx.node = ctx.expression().node;
        if ( ctx.parenExpression() != null ) {
            ctx.node = Nodes.imply(ctx.node.getPosition(), ctx.node, ctx.parenExpression().node);
        }
    }

    @Override
    public void exitExpression(ExpressionContext ctx) {
        TerminalNode operator;
        if ( ctx.unary() != null ) {
            ctx.node = ctx.unary().node;
        }
        else if ( (operator = first(ctx.MUL(), ctx.DIV(), ctx.MOD(), ctx.ADD(), ctx.SUB())) != null ) {
            ctx.node = Nodes.arithmeticOp(operator.getSymbol(), ctx.expression(0).node, ctx.expression(1).node);
        }
        else if ( (operator = first(ctx.LEFT_SHIFT(), ctx.RIGHT_SHIFT(), ctx.URIGHT_SHIFT())) != null ) {
            ctx.node = Nodes.shiftOp(operator.getSymbol(), ctx.expression(0).node, ctx.expression(1).node);
        }
        else if ( ctx.INSTANCEOF() != null ) {
            /*FIXME:*/throw new NotImplementedException("instanceof");
        }
        else if ( (operator = first(ctx.GT(), ctx.GE(), ctx.LT(), ctx.LE())) != null ) {
            ctx.node = Nodes.relationalOp(operator.getSymbol(), ctx.expression(0).node, ctx.expression(1).node);
        }
        else if ( (operator = first(ctx.BITWISE_AND(), ctx.BITWISE_OR(), ctx.BITWISE_XOR())) != null ) {
            ctx.node = Nodes.bitwiseOp(operator.getSymbol(), ctx.expression(0).node, ctx.expression(1).node);
        }
        else if ( (operator = first(ctx.LOGICAL_AND(), ctx.LOGICAL_OR())) != null ) {
            ctx.node = Nodes.logicalOp(operator.getSymbol(), ctx.expression(0).node, ctx.expression(1).node);
        }
        else if ( ctx.CONDITIONAL() != null ) {
            ctx.node = Nodes.conditionalOp(ctx.CONDITIONAL().getSymbol(), ctx.expression(0).node, ctx.expression(1).node, ctx.expression(2).node);
        }
        else {
            throw new UnreachableCodeException();
        }
    }

    @Override
    public void exitUnary(UnaryContext ctx) {
        if ( ctx.ADD() != null ) {
            ctx.node = Nodes.unaryOp(ctx.ADD().getSymbol(), ctx.unary().node);
        }
        else if ( ctx.SUB() != null ) {
            ctx.node = Nodes.unaryOp(ctx.SUB().getSymbol(), ctx.unary().node);
        }
        else {
            ctx.node = ctx.unaryNoPosNeg().node;
        }
    }

    @Override
    public void exitUnaryNoPosNeg(UnaryNoPosNegContext ctx) {
        if ( ctx.BITWISE_NOT() != null ) {
            ctx.node = Nodes.unaryOp(ctx.BITWISE_NOT().getSymbol(), ctx.unary().node);
        }
        else if ( ctx.LOGICAL_NOT() != null ) {
            ctx.node = Nodes.unaryOp(ctx.LOGICAL_NOT().getSymbol(), ctx.unary().node);
        }
        else if ( ctx.cast() != null ) {
            ctx.node = ctx.cast().node;
        }
        else if ( ctx.factor() != null ) {
            ctx.node = ctx.factor().node;
        }
        else {
            throw new UnreachableCodeException();
        }
    }

    @Override
    public void exitFactor(FactorContext ctx) {
        ctx.node = ctx.primary().node;
        for ( SelectorContext sel : ctx.selector() ) {
            if ( sel.ID() != null ) {
                ctx.node = Nodes.idReference(sel.ID().getSymbol(), ctx.node, sel.ID().getSymbol().getText());
            }
            else if ( sel.methodCall() != null ) {
                ctx.node = methodCall(ctx.node, sel.methodCall());
            }
            else if ( sel.INDEX_OPEN() != null ) {
                ctx.node = Nodes.arrayAccess(sel.INDEX_CLOSE().getSymbol(), ctx.node, sel.expression().node);
            }
            else {
                throw new UnreachableCodeException();
            }
        }
    }

    @Override
    public void exitPrimary(PrimaryContext ctx) {
        if ( ctx.PAREN_OPEN() != null ) {
            ctx.node = ctx.expression().node;
        }
        else if ( ctx.methodCall() != null ) {
            ctx.node = methodCall(null, ctx.methodCall());
        }
        else if ( ctx.ID() != null ) {
            ctx.node = Nodes.idReference(ctx.ID().getSymbol(), ctx.ID().getText());
        }
        else if ( ctx.literal() != null ) {
            ctx.node = ctx.literal().node;
        }
        else if ( ctx.CLASS() != null ) {
            /*FIXME:*/throw new NotImplementedException();
        }
        else if ( ctx.THIS() != null ) {
            /*FIXME:*/throw new NotImplementedException();
        }
        else {
            throw new UnreachableCodeException();
        }
    }

    @Override
    public void exitLiteral(LiteralContext ctx) {
        if ( ctx.STRING() != null ) {
            ctx.node = Literals.string(ctx.STRING().getSymbol(), Literal.Kind.STRING);
        }
        else if ( ctx.CHAR() != null ) {
            ctx.node = Literals.string(ctx.CHAR().getSymbol(), Literal.Kind.CHAR);
        }
        else if ( ctx.INT() != null ) {
            ctx.node = Literals.integer(ctx.INT().getSymbol());
        }
        else if ( ctx.FLOAT() != null ) {
            ctx.node = Literals.floatingPoint(ctx.FLOAT().getSymbol());
        }
        else if ( ctx.TRUE() != null ) {
            ctx.node = Nodes.literal(ctx.TRUE().getSymbol(), Literal.Kind.BOOLEAN, true);
        }
        else if ( ctx.FALSE() != null ) {
            ctx.node = Nodes.literal(ctx.FALSE().getSymbol(), Literal.Kind.BOOLEAN, false);
        }
        else if ( ctx.NULL() != null ) {
            ctx.node = Nodes.literal(ctx.NULL().getSymbol(), Literal.Kind.NULL, null);
        }
        else {
            throw new UnreachableCodeException();
        }
    }

    private AstNode methodCall(AstNode source, MethodCallContext ctx) {
        return Nodes.methodCall(ctx.ID().getSymbol(), source, ctx.ID().getText(), Lists.transform(ctx.expression(), new Function<ExpressionContext, AstNode>() {
            @Override
            public AstNode apply(ExpressionContext input) {
                return input.node;
            }
        }));
    }

    private static <T> T first(T... elements) {
        for ( T e : elements ) {
            if ( e != null ) {
                return e;
            }
        }
        return null;
    }

    class ErrorStrategy implements ANTLRErrorStrategy {

        final ANTLRErrorStrategy delegate;

        ErrorStrategy(ANTLRErrorStrategy delegate) {
            this.delegate = delegate;
        }

        @Override
        public void reset(@NotNull Parser recognizer) {
        }

        @Override
        public Token recoverInline(@NotNull Parser recognizer) throws RecognitionException {
            stopAstBuilding(recognizer);
            return delegate.recoverInline(recognizer);
        }

        @Override
        public void recover(@NotNull Parser recognizer, @Nullable RecognitionException e) {
            stopAstBuilding(recognizer);
            delegate.recover(recognizer, e);
        }

        @Override
        public void sync(@NotNull Parser recognizer) {
            delegate.sync(recognizer);
        }

        @Override
        public boolean inErrorRecoveryMode(@NotNull Parser recognizer) {
            return delegate.inErrorRecoveryMode(recognizer);
        }

        @Override
        public void reportError(@NotNull Parser recognizer, @Nullable RecognitionException e) throws RecognitionException {
            delegate.reportError(recognizer, e);
        }

        @Override
        public void reportMatch(@NotNull Parser recognizer) {
            delegate.reportMatch(recognizer);
        }

        private void stopAstBuilding(Parser parser) {
            parser.removeParseListener(AstBuilder.this);
        }
    }

}
