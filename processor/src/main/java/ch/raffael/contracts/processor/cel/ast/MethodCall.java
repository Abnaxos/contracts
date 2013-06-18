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
import ch.raffael.contracts.Nullable;
import ch.raffael.contracts.processor.cel.Position;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class MethodCall extends Selector {

    private final String methodName;
    private final List<AstNode> arguments;

    MethodCall(@NotNull Position position, @Nullable AstNode source, @NotNull String methodName, @NotNull List<AstNode> arguments) {
        super(position, source);
        this.methodName = methodName;
        this.arguments = ImmutableList.copyOf(arguments);
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        super.toString(toString);
        toString.add("id", methodName);
        toString.add("args", arguments);
    }

    @Override
    public boolean equals(Object o) {
        if ( super.equals(o) ) {
            MethodCall that = (MethodCall)o;
            return methodName.equals(that.methodName)
                    && arguments.equals(that.arguments);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return appendHash(appendHash(super.hashCode(), methodName), arguments);
    }

    @NotNull
    @Override
    protected List<AstNode> children() {
        return ImmutableList.<AstNode>builder().add(getSource()).addAll(arguments).build();
    }

    @Override
    protected void doAccept(AstVisitor visitor) {
        visitor.visit(this);
    }

    @NotNull
    public String getMethodName() {
        return methodName;
    }

    @NotNull
    public List<AstNode> getArguments() {
        return arguments;
    }
}
