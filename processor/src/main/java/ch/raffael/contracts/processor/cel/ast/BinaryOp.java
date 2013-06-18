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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.processor.cel.Position;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class BinaryOp extends AstNode {

    private final AstNode left;
    private final AstNode right;

    BinaryOp(Position position, AstNode left, AstNode right) {
        super(position);
        this.left = left;
        this.right = right;
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        super.toString(toString);
        toString.addValue(left).addValue(right);
    }

    @Override
    public boolean equals(Object o) {
        if ( super.equals(o) ) {
            BinaryOp that = (BinaryOp)o;
            return left.equals(that.left) && right.equals(that.right);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return appendHash(appendHash(super.hashCode(), left), right);
    }

    @NotNull
    @Override
    protected List<AstNode> children() {
        return ImmutableList.of(left, right);
    }

    public AstNode getLeft() {
        return left;
    }

    public AstNode getRight() {
        return right;
    }
}
