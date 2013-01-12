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


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Template extends CelNode {

    Template() {
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
        Template that = (Template)obj;
        // FIXME: Not implemented
        return true;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        // FIXME: Not implemented
        return hash;
    }

}
