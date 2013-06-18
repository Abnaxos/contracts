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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;

import ch.raffael.contracts.Ensure;
import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.Nullable;
import ch.raffael.contracts.processor.cel.CelError;
import ch.raffael.contracts.processor.cel.Position;
import ch.raffael.util.common.collections.USet;

import static com.google.common.base.Preconditions.*;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class AstNode {

    private AstNode parent;

    private final Position position;
    private final USet<CelError> errors = new USet(new LinkedHashSet<>());

    private final Map<Annotation<?>, Object> annotations = new HashMap<>();

    private List<AstNode> children = null;

    AstNode(Position position) {
        this.position = position;
    }

    @Override
    public String toString() {
        Objects.ToStringHelper toString = Objects.toStringHelper(this);
        toString.addValue(position);
        toString(toString);
        return toString.toString();
    }

    protected void toString(Objects.ToStringHelper toString) {
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        AstNode that = (AstNode)o;
        return eq(parent, that.parent);
    }

    protected static boolean eq(Object a, Object b) {
        return Objects.equal(a, b);
    }

    @Override
    public int hashCode() {
        int result = parent == null ? 0 : parent.hashCode();
        return result;
    }

    protected static int appendHash(int hash, @Nullable Object object) {
        return 31 * hash + (object == null ? 0 : object.hashCode());
    }

    protected static int appendHash(int hash, int anInt) {
        return 31 * hash + anInt;
    }

    protected static int appendHash(int hash, long aLong) {
        return 31 * hash + (int)(aLong ^ (aLong >>> 32));
    }

    protected static int appendHash(int hash, boolean aBoolean) {
        return 31 * hash + (aBoolean ? 1 : 0);
    }

    public AstNode getParent() {
        return parent;
    }

    @NotNull
    public List<AstNode> getChildren() {
        if ( children == null ) {
            children = children();
        }
        return children;
    }

    @NotNull
    protected abstract List<AstNode> children();

    public Position getPosition() {
        return position;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Set<CelError> getErrors() {
        return errors.unmodifiable();
    }

    public void addError(CelError error) {
        errors.add(error);
    }

    @NotNull
    @Ensure("@result == visitor")
    public <T extends AstVisitor> T accept(@NotNull T visitor) {
        doAccept(visitor);
        return visitor;
    }

    protected abstract void doAccept(AstVisitor visitor);

    protected <T extends AstNode> T child(T child) {
        AstNode c = child; // private access won't work with child.parent
        checkState(c.parent == null, "Child already has a parent");
        c.parent = this;
        return child;
    }

    protected <N extends AstNode, C extends Collection<N>> C children(C children) {
        for ( N child : children ) {
            child(child);
        }
        return children;
    }

    @SuppressWarnings("unchecked")
    public <T> T set(Annotation<T> annotation, T value) {
        if ( value == null ) {
            return (T)annotations.remove(annotation);
        }
        else {
            return (T)annotations.put(annotation, value);
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T get(@NotNull Annotation<T> annotation) {
        T value = (T)annotations.get(annotation);
        if ( value == null ) {
            throw new IllegalStateException("Required annotation " + annotation + " not set on " + this);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    @Ensure("if(fallback!=null) @result!=null")
    public <T> T get(@NotNull Annotation<T> annotation, T fallback) {
        T value = get(annotation);
        return value == null ? fallback : value;
    }

}
