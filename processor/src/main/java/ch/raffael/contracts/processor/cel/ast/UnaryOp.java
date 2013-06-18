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

import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.processor.cel.Position;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class UnaryOp extends AstNode {

    private final Kind kind;
    private final AstNode expression;

    UnaryOp(Position position, Kind kind, AstNode expression) {
        super(position);
        this.kind = kind;
        this.expression = expression;
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        super.toString(toString);
        toString.addValue(kind).addValue(expression);
    }

    @Override
    public boolean equals(Object o) {
        if ( super.equals(o) ) {
            UnaryOp that = (UnaryOp)o;
            return kind == that.kind && expression.equals(that.expression);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return appendHash(appendHash(super.hashCode(), kind), expression);
    }

    @NotNull
    @Override
    protected List<AstNode> children() {
        return Collections.singletonList(expression);
    }

    @Override
    protected void doAccept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public Kind getKind() {
        return kind;
    }

    public AstNode getExpression() {
        return expression;
    }

    public static enum Kind {
        POS, NEG, BITWISE_NOT, LOGICAL_NOT
    }

}
