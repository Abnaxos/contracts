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

import com.google.common.base.Objects;

import ch.raffael.contracts.processor.cel.Position;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class IfExpression extends AstNode {

    private final AstNode condition;
    private final AstNode expression;

    IfExpression(Position position, AstNode condition, AstNode expression) {
        super(position);
        this.condition = condition;
        this.expression = expression;
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        super.toString(toString);
        toString.add("condition", condition).addValue(expression);
    }

    @Override
    public boolean equals(Object o) {
        if ( super.equals(o) ) {
            IfExpression that = (IfExpression)o;
            return this.condition.equals(that.condition)
                    && this.expression.equals(that.expression);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return appendHash(appendHash(super.hashCode(), this.condition), this.expression);
    }

    public AstNode getCondition() {
        return condition;
    }

    public AstNode getExpression() {
        return expression;
    }
}
