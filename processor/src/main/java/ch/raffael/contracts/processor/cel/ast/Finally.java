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
package ch.raffael.contracts.processor.cel.ast;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.processor.cel.Position;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Finally extends AstNode {

    private final AstNode expression;

    public Finally(Position position, AstNode expression) {
        super(position);
        this.expression = expression;
    }

    public AstNode getExpression() {
        return expression;
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

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(expression);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null || getClass() != obj.getClass() ) {
            return false;
        }
        if ( !super.equals(obj) ) {
            return false;
        }
        final Finally other = (Finally)obj;
        return Objects.equal(this.expression, other.expression);
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        toString.addValue(expression);
    }
}
