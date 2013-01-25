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
package ch.raffael.contracts.devtools.browser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;

import ch.raffael.util.i18n.I18N;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class BrowserTreeNode implements TreeNode {

    private static final BrowserResources RES = I18N.getBundle(BrowserResources.class);
    private static final Comparator<BrowserTreeNode> SORTER = new Comparator<BrowserTreeNode>() {
        @Override
        public int compare(BrowserTreeNode left, BrowserTreeNode right) {
            return left.getLabel().compareTo(right.getLabel());
        }
    };

    private final BrowserTreeNode parent;
    private final List<BrowserTreeNode> children = new ArrayList<>();
    private final Kind kind;
    private final Path path;

    public BrowserTreeNode(BrowserTreeNode parent, Kind kind, Path path) {
        this.parent = parent;
        this.kind = kind;
        this.path = path;
    }

    @Override
    public String toString() {
        return "BrowserTreeNode{" + kind + "," + path + "}";
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        BrowserTreeNode that = (BrowserTreeNode)o;
        if ( kind != that.kind ) {
            return false;
        }
        if ( Objects.equal(parent, that.parent) ) {
            return false;
        }
        return getLabel().equals(that.getLabel());
    }

    @Override
    public int hashCode() {
        int result = parent != null ? parent.hashCode() : 0;
        result = 31 * result + kind.hashCode();
        result = 31 * result + getLabel().hashCode();
        return result;
    }

    @Override
    public BrowserTreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public BrowserTreeNode getParent() {
        return parent;
    }

    @SuppressWarnings("unchecked")
    public <T extends TreeNode> T findParent(Class<T> type) {
        TreeNode node = this;
        while ( node != null && !type.isInstance(node) ) {
            node = node.getParent();
        }
        return (T)node;
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return !kind.isLeaf();
    }

    @Override
    public boolean isLeaf() {
        return kind.isLeaf() || children.isEmpty();
    }

    @Override
    public Enumeration<BrowserTreeNode> children() {
        return Iterators.asEnumeration(children.iterator());
    }

    public Path getPath() {
        return path;
    }

    public Kind getKind() {
        return kind;
    }

    public String getLabel() {
        switch ( kind ) {
            case ROOT:
                return "";
            case FOLDER:
                return path.toString();
            default:
                return path.getFileName().toString();
        }
    }

    public void add(BrowserTreeNode child) {
        children.add(child);
    }

    public void sort() {
        Collections.sort(children, SORTER);
        for ( BrowserTreeNode child : children ) {
            child.sort();
        }
    }

    public static enum Kind {
        ROOT(RES.treeFolderIcon(), false),
        FOLDER(RES.treeFolderIcon(), false),
        PACKAGE(RES.treePackageIcon(), false),
        CLASS(RES.treeClassIcon(), true);

        private final ImageIcon icon;
        private final boolean leaf;

        private Kind(ImageIcon icon, boolean leaf) {
            this.icon = icon;
            this.leaf = leaf;
        }

        public ImageIcon getIcon() {
            return icon;
        }

        public boolean isLeaf() {
            return leaf;
        }
    }

}
