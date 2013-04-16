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
import ch.raffael.contracts.processor.ct.ClassName;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Literal extends AstNode {

    private final Kind kind;
    private final Object value;

    Literal(@NotNull Position position, @NotNull Kind kind, Object value) {
        super(position);
        this.kind = kind;
        this.value = value;
    }

    @Override
    protected void toString(Objects.ToStringHelper toString) {
        toString.addValue(kind).addValue(value);
    }

    @Override
    public boolean equals(Object o) {
        if ( super.equals(o) ) {
            Literal that = (Literal)o;
            return kind == that.kind && eq(value, that.value);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return appendHash(appendHash(super.hashCode(), kind), value);
    }

    @Override
    protected void doAccept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public Kind getKind() {
        return kind;
    }

    public Object getValue() {
        return value;
    }

    public static enum Kind {

        INT(ClassName.INT) {
            @Override
            public boolean isValueCompatible(Object value) {
                return value instanceof Integer;
            }
        },
        LONG(ClassName.LONG) {
            @Override
            public boolean isValueCompatible(Object value) {
                return value instanceof Long;
            }
        },
        FLOAT(ClassName.FLOAT) {
            @Override
            public boolean isValueCompatible(Object value) {
                return value instanceof Float;
            }
        },
        DOUBLE(ClassName.DOUBLE) {
            @Override
            public boolean isValueCompatible(Object value) {
                return value instanceof Double;
            }
        },
        BOOLEAN(ClassName.BOOLEAN) {
            @Override
            public boolean isValueCompatible(Object value) {
                return value instanceof Boolean;
            }
        },
        STRING(ClassName.STRING) {
            @Override
            public boolean isValueCompatible(Object value) {
                return value instanceof String;
            }
        },
        CHAR(ClassName.CHAR) {
            @Override
            public boolean isValueCompatible(Object value) {
                return value instanceof Character;
            }
        },
        NULL(ClassName.OBJECT) {
            @Override
            public boolean isValueCompatible(Object value) {
                return value == null;
            }
        };

        private final ClassName className;

        private Kind(ClassName className) {
            this.className = className;
        }

        public ClassName getClassName() {
            return className;
        }

        public abstract boolean isValueCompatible(Object value);

    }

}
