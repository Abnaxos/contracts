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
import ch.raffael.contracts.Nullable;
import ch.raffael.contracts.processor.cel.Position;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class Selector extends AstNode {

    private final AstNode source;

    Selector(@NotNull Position position, @Nullable AstNode source) {
        super(position);
        this.source = source;
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        toString.add("source", source);
    }

    @Override
    public boolean equals(Object o) {
        if ( super.equals(o) ) {
            return eq(source, ((Selector)o).source);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return appendHash(super.hashCode(), source);
    }

    @Nullable
    public AstNode getSource() {
        return source;
    }
}
