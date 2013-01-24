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

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.processor.cel.Position;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class ShiftOp extends BinaryOp {

    private final Kind kind;

    ShiftOp(@NotNull Position position, @NotNull Kind kind, @NotNull AstNode left, @NotNull AstNode right) {
        super(position, left, right);
        this.kind = kind;
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        toString.addValue(kind);
        super.toString(toString);
    }

    @Override
    public boolean equals(Object obj) {
        if ( super.equals(obj) ) {
            return ((ShiftOp)obj).kind == kind;
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return appendHash(super.hashCode(), kind);
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    protected void doAccept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static enum Kind {
        LEFT, RIGHT, UNSIGNED_RIGHT
    }

}
