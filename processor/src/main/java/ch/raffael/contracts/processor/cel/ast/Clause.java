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

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.Nullable;
import ch.raffael.contracts.processor.cel.Position;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Clause extends AstNode {

    private final AstNode precondition;
    private final List<AstNode> postconditions;

    Clause(@NotNull Position pos, @Nullable AstNode precondition, @NotNull Iterator<AstNode> postconditions) {
        super(pos);
        this.precondition = precondition;
        this.postconditions = ImmutableList.copyOf(postconditions);
    }

    @Nullable
    public AstNode getPrecondition() {
        return precondition;
    }

    @NotNull
    public List<AstNode> getPostconditions() {
        return postconditions;
    }

    @NotNull
    @Override
    protected List<AstNode> children() {
        if ( precondition != null ) {
            return ImmutableList.<AstNode>builder().add(precondition).addAll(postconditions).build();
        }
        else {
            return postconditions;
        }
    }

    @Override
    protected void doAccept(AstVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(precondition, postconditions);
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
        final Clause that = (Clause)obj;
        return Objects.equal(this.precondition, that.precondition) && Objects.equal(this.postconditions, that.postconditions);
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        toString.add("pre", precondition);
        toString.add("post", postconditions);
    }

}
