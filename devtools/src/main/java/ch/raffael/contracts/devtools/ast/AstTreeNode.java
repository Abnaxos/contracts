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

import java.awt.Component;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import ch.raffael.contracts.processor.cel.ast.AstNode;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class AstTreeNode implements TreeNode {

    private final AstTreeNode parent;
    private final AstNode node;
    private final NodeRenderer renderer;
    private final List<AstTreeNode> children;

    public AstTreeNode(AstTreeNode parent, AstNode node) {
        this.parent = parent;
        this.node = node;
        if ( node != null ) {
            renderer = NodeRenderer.get(node);
            children = ImmutableList.copyOf(Lists.transform(
                    node.getChildren(),
                    new Function<AstNode, AstTreeNode>() {
                        @Override
                        public AstTreeNode apply(AstNode input) {
                            return new AstTreeNode(AstTreeNode.this, input);
                        }
                    }));
        }
        else {
            renderer = null;
            children = Collections.emptyList();
        }
    }

    public static AstTreeNode empty() {
        return new AstTreeNode(null, null);
    }

    public static AstTreeNode from(AstNode node) {
        return new AstTreeNode(null, node);
    }

    public AstNode getAstNode() {
        return node;
    }

    public NodeRenderer getRenderer() {
        return renderer;
    }

    @Override
    public AstTreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public Enumeration children() {
        return Iterators.asEnumeration(children.iterator());
    }

    public static class Renderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            AstTreeNode node = (AstTreeNode)value;
            if ( node.getAstNode() != null ) {
                setText(node.getRenderer().describe(node.getAstNode()));
            }
            else {
                setText("No AST available");
            }
            return this;
        }
    }

}
