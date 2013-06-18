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
public final class Clause extends AstNode {

    private final boolean isFinally;
    private final AstNode expression;

    Clause(@NotNull Position pos, @NotNull AstNode expression, boolean isFinally) {
        super(pos);
        this.expression = expression;
        this.isFinally = isFinally;
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        toString.add("finally", isFinally);
        toString.addValue(expression);
    }

    @Override
    public boolean equals(Object obj) {
        if ( !super.equals(obj) ) {
            return false;
        }
        Clause that = (Clause)obj;
        return isFinally == that.isFinally
                && expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = appendHash(hash, isFinally);
        hash = appendHash(hash, expression);
        return hash;
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

    public boolean isFinally() {
        return isFinally;
    }

    public AstNode getExpression() {
        return expression;
    }
}
