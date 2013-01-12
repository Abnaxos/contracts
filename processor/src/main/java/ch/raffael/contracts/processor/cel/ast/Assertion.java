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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Objects;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Assertion extends CelNode {

    private boolean isFinally = false;
    private final List<CelNode> ifs = new LinkedList<>();
    private CelNode expression;

    Assertion() {
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        toString.add("finally", isFinally);
    }

    @Override
    public boolean equals(Object obj) {
        if ( !super.equals(obj) ) {
            return false;
        }
        Assertion that = (Assertion)obj;
        return isFinally == that.isFinally
                && ifs.equals(that.ifs)
                && expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = appendHash(hash, isFinally);
        hash = appendHash(hash, ifs);
        hash = appendHash(hash, expression);
        return hash;
    }

    public boolean isFinally() {
        return isFinally;
    }

    public void setFinally(boolean isFinally) {
        this.isFinally = isFinally;
    }

    public Assertion expression(CelNode expression) {
        this.expression = child(expression);
        return this;
    }

    public Assertion ifs(Collection<CelNode> expressions) {
        ifs.addAll(children(expressions));
        return this;
    }

    public CelNode getExpression() {
        return expression;
    }

    public List<CelNode> getIfs() {
        return ifs;
    }
}
