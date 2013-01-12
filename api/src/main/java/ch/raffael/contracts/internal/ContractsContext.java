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
package ch.raffael.contracts.internal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ch.raffael.contracts.ContractViolationError;
import ch.raffael.contracts.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class ContractsContext {

    public static final String ROOT_NAME = "*";

    private static final ContractsContext ROOT = new ContractsContext("");
    private static final Log LOG = Log.getInstance();

    private static final Map<String, ContractsContext> CONTEXTS = new HashMap<String, ContractsContext>();

    private final String name;
    private final LinkedList<ContractsContext> children = new LinkedList<ContractsContext>();
    private volatile boolean enabled;

    private ContractsContext(@NotNull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ContractsPolicy[" + name + "]";
    }

    @NotNull
    public static ContractsContext getContext(@NotNull String name) {
        ContractsContext context;
        if ( name.equals(ROOT_NAME) ) {
            return ROOT;
        }
        synchronized ( CONTEXTS ) {
            context = CONTEXTS.get(name);
            if ( context != null ) {
                return context;
            }
            if ( !ROOT_NAME.equals(name) ) {
                boolean firstChar = true;
                for ( int i = 0; i < name.length(); i++ ) {
                    char c = name.charAt(i);
                    if ( firstChar ) {
                        if ( !Character.isJavaIdentifierStart(c) ) {
                            throw new IllegalArgumentException("Illegal policy name: '" + name + "'");
                        }
                        firstChar = false;
                    }
                    else {
                        if ( c == '.' ) {
                            firstChar = true;
                        }
                        else if ( !Character.isJavaIdentifierPart(c) ) {
                            throw new IllegalArgumentException("Illegal policy name: '" + name + "'");
                        }
                    }
                }
                if ( firstChar ) {
                    throw new IllegalArgumentException("Illegal policy name: '" + name + "'");
                }
            }
            return getContext0(name);
        }
    }

    @NotNull
    private static ContractsContext getContext0(@NotNull String name) {
        ContractsContext context;
        context = CONTEXTS.get(name);
        if ( context == null ) {
            context = new ContractsContext(name);
            ContractsContext parent;
            int pos = name.lastIndexOf('.');
            if ( pos < 0 ) {
                parent = ROOT;
            }
            else {
                parent = getContext0(name.substring(0, pos));
            }
            parent.children.add(context);
            CONTEXTS.put(name, context);
        }
        return context;
    }

    @NotNull
    public static ContractsContext getContext(@NotNull Class<?> clazz) {
        Class<?> outer = clazz;
        while ( outer.getEnclosingClass() != null ) {
            outer = outer.getEnclosingClass();
        }
        synchronized ( CONTEXTS ) {
            return getContext0(outer.getName());
        }
    }

    @NotNull
    public static ContractsContext getContext(@NotNull Package pkg) {
        synchronized ( CONTEXTS ) {
            return getContext0(pkg.getName());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        synchronized ( CONTEXTS ) {
            LOG.info("Enabling contracts for %s", name);
            enable0();
        }
    }

    private void enable0() {
        enabled = true;
        for ( ContractsContext child : children ) {
            child.enable0();
        }
    }

    public void disable() {
        synchronized ( CONTEXTS ) {
            LOG.info("Disabling contracts for %s", name);
            disable0();
        }
    }

    private void disable0() {
        enabled = false;
        for ( ContractsContext child : children ) {
            child.disable0();
        }
    }

    public void violation(ContractViolationError violation) {
        throw violation;
    }

}
