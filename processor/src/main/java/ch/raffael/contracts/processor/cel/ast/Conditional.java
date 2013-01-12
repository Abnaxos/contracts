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


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Conditional extends CelNode {

    private final CelNode condition;
    private final CelNode onTrue;
    private final CelNode onFalse;

    Conditional(@NotNull CelNode condition, @NotNull CelNode onTrue, @NotNull CelNode onFalse) {
        this.condition = condition;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        // FIXME: Not implemented
        super.toString(toString);
    }

    @Override
    public boolean equals(Object obj) {
        if ( !super.equals(obj) ) {
            return false;
        }
        Conditional that = (Conditional)obj;
        return condition.equals(that.condition)
                && (onTrue.equals(that.onTrue)
                && onFalse.equals(that.onFalse));
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = appendHash(hash, condition);
        hash = appendHash(hash, onTrue);
        hash = appendHash(hash, onFalse);
        return hash;
    }

    @NotNull
    public CelNode getCondition() {
        return condition;
    }

    @NotNull
    public CelNode getOnTrue() {
        return onTrue;
    }

    @NotNull
    public CelNode getOnFalse() {
        return onFalse;
    }
}
