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

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.processor.cel.Position;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Imply extends AstNode {

    private final AstNode condition;
    private final AstNode implies;

    Imply(Position position, AstNode condition, AstNode implies) {
        super(position);
        this.condition = condition;
        this.implies = implies;
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        super.toString(toString);
        toString.add("condition", condition).add("implies", implies);
    }

    @Override
    public boolean equals(Object o) {
        if ( super.equals(o) ) {
            Imply that = (Imply)o;
            return this.condition.equals(that.condition)
                    && this.implies.equals(that.implies);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return appendHash(appendHash(super.hashCode(), this.condition), this.implies);
    }

    @NotNull
    @Override
    protected List<AstNode> children() {
        return ImmutableList.of(condition, implies);
    }

    @Override
    protected void doAccept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public AstNode getCondition() {
        return condition;
    }

    public AstNode getImplies() {
        return implies;
    }
}
