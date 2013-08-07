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
package ch.raffael.contracts.devtools.ast;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.processor.cel.ast.AstNode;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class NodeRenderer<T extends AstNode> {

    private final static Map<Class<? extends AstNode>, NodeRenderer> renderers =
            ImmutableMap.<Class<? extends AstNode>, NodeRenderer>builder()
                    .put(AstNode.class, new NodeRenderer<AstNode>() {
                        @Override
                        protected String describe(AstNode node) {
                            return node.toString();
                        }
                    })
                    .build();

    private NodeRenderer() {
    }

    @NotNull
    public static NodeRenderer get(@NotNull AstNode node) {
        return get(node.getClass());
    }

    @NotNull
    public static NodeRenderer get(@NotNull Class<? extends AstNode> nodeClass) {
        NodeRenderer renderer = null;
        while ( renderer == null ) {
            renderer = renderers.get(nodeClass);
            nodeClass = (Class<? extends AstNode>)nodeClass.getSuperclass();
        }
        return renderer;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public String description(@NotNull AstNode node) {
        return describe((T)node);
    }

    @NotNull
    protected abstract String describe(@NotNull T node);

}
