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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.jidesoft.swing.FolderChooser;
import com.jidesoft.swing.JideComboBox;
import com.jidesoft.swing.TreeSearchable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import ch.raffael.util.common.logging.LogUtil;
import ch.raffael.util.i18n.I18N;
import ch.raffael.util.swing.actions.ActionPresenter;
import ch.raffael.util.swing.actions.CommonAction;
import ch.raffael.util.swing.context.Context;
import ch.raffael.util.swing.error.ErrorDisplayer;
import ch.raffael.util.swing.util.History;
import ch.raffael.util.swing.util.HistoryEvent;
import ch.raffael.util.swing.util.HistoryListener;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class BrowserPanel {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = LogUtil.getLogger();

    private static final BrowserResources RES = I18N.getBundle(BrowserResources.class);
    private static final Preferences PREFS = Preferences.userNodeForPackage(BrowserPanel.class);
    private final JideComboBox sourceStyle;
    private final CommonAction addFolderAction;
    private final CommonAction removeFolderAction;
    private final CommonAction backAction;
    private final CommonAction forwardAction;
    private final CommonAction reloadClassAction;

    private JPanel root;
    private JSplitPane splitter;
    private JTree tree;
    private JToolBar treeToolbar;
    private JToolBar asmToolbar;
    private JPanel asmPanel;

    private RTextScrollPane asmScroller;
    private RSyntaxTextArea asmSource;

    private DefaultTreeModel treeModel;

    private final Context context;
    private final Set<Path> folders = new HashSet<>();
    private final History<TreePath> history = new History<>();

    public BrowserPanel(Context context) {
        context = context.create(root);
        this.context = context;
        for ( String uri : Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().split(PREFS.get("folders", "")) ) {
            try {
                folders.add(Paths.get(new URI(uri)));
            }
            catch ( URISyntaxException e ) {
                log.error("Error reading folders from preferences", e);
                //context.require(ErrorDisplayer.class).displayError(root, e);
            }
        }
        addFolderAction = new CommonAction(RES, "addFolder") {
            FolderChooser chooser = null;
            @Override
            public void actionPerformed(ActionEvent evt) {
                if ( chooser == null ) {
                    chooser = new FolderChooser();
                }
                if ( chooser.showOpenDialog(root) == JFileChooser.APPROVE_OPTION) {
                    folders.add(chooser.getSelectedFolder().toPath());
                    Joiner.on(' ').join(Collections2.transform(folders, new Function<Path, URI>() {
                        @Override
                        public URI apply(Path input) {
                            return input.toUri();
                        }
                    }));
                    PREFS.put("folders", Joiner.on(' ').join(
                            Collections2.transform(folders, new Function<Path, URI>() {
                                @Override
                                public URI apply(Path input) {
                                    return input.toUri();
                                }
                            })));
                    try {
                        tree.setModel(new DefaultTreeModel(scanTree()));
                    }
                    catch ( IOException e ) {
                        BrowserPanel.this.context.require(ErrorDisplayer.class).displayError(root, e);
                    }
                }
            }
        };
        removeFolderAction = new CommonAction(RES, "removeFolder") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if ( tree.getSelectionPath() != null ) {
                    BrowserTreeNode node = (BrowserTreeNode)tree.getSelectionPath().getLastPathComponent();
                    folders.remove(node.getPath());
                    PREFS.put("folders", Joiner.on(' ').join(
                            Collections2.transform(folders, new Function<Path, URI>() {
                                @Override
                                public URI apply(Path input) {
                                    return input.toUri();
                                }
                            })));
                    try {
                        tree.setModel(new DefaultTreeModel(scanTree()));
                    }
                    catch ( IOException e ) {
                        BrowserPanel.this.context.require(ErrorDisplayer.class).displayError(root, e);
                    }
                }
            }
        };
        removeFolderAction.setEnabled(false);
        context.require(ActionPresenter.class).builder(JToolBar.class)
                .init(treeToolbar)
                .add(new CommonAction(RES, "rescan") {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            tree.setModel(new DefaultTreeModel(scanTree()));
                        }
                        catch ( IOException e ) {
                            BrowserPanel.this.context.require(ErrorDisplayer.class).displayError(root, e);
                        }
                    }
                })
                .add(addFolderAction)
                .add(removeFolderAction);
        int splitterLocation = PREFS.getInt("splitterLocation", 0);
        if ( splitterLocation > 0 ) {
            splitter.setDividerLocation(splitterLocation);
        }
        splitter.addPropertyChangeListener("dividerLocation", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                PREFS.putInt("splitterLocation", splitter.getDividerLocation());
            }
        });
        asmSource = new RSyntaxTextArea();
        asmSource.setEditable(false);
        asmSource.setHighlightCurrentLine(false);
        asmSource.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        asmSource.setAntiAliasingEnabled(true);
        asmScroller = new RTextScrollPane(asmSource);
        asmScroller.setLineNumbersEnabled(true);
        asmPanel.add(asmScroller, BorderLayout.CENTER);
        backAction = new CommonAction(RES, "back") {
            @Override
            public void actionPerformed(ActionEvent e) {
                history.back();
            }
        };
        backAction.setEnabled(false);
        forwardAction = new CommonAction(RES, "forward") {
            @Override
            public void actionPerformed(ActionEvent e) {
                history.forward();
            }
        };
        forwardAction.setEnabled(false);
        history.addHistoryListener(new HistoryListener() {
            @Override
            public void historyChanged(HistoryEvent event) {
                backAction.setEnabled(history.hasPrevious());
                forwardAction.setEnabled(history.hasNext());
                tree.setSelectionPath(history.getCurrent());
            }
        });
        reloadClassAction = new CommonAction(RES, "reloadClass") {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSource();
            }
        };
        context.require(ActionPresenter.class).builder(JToolBar.class)
                .init(asmToolbar)
                .add(reloadClassAction)
                .add(backAction)
                .add(forwardAction);
        asmToolbar.add(Box.createHorizontalGlue());
        sourceStyle = new JideComboBox(new Object[] { "Java", "Bytecode" });
        Dimension sourceStyleSize = sourceStyle.getPreferredSize();
        for ( int i = 0; i < sourceStyle.getItemCount(); i++ ) {
            sourceStyle.setSelectedIndex(i);
            Dimension prefSize = sourceStyle.getPreferredSize();
            sourceStyleSize.width = Math.max(prefSize.width, sourceStyleSize.width);
            sourceStyleSize.height = Math.max(prefSize.height, sourceStyleSize.height);
        }
        sourceStyle.setMaximumSize(sourceStyleSize);
        sourceStyle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                PREFS.putInt("sourceStyle", sourceStyle.getSelectedIndex());
                updateSource();
            }
        });
        sourceStyle.setSelectedIndex(Math.min(sourceStyle.getItemCount()-1, PREFS.getInt("sourceStyle", 0)));
        asmToolbar.add(sourceStyle);
        try {
            treeModel = new DefaultTreeModel(scanTree());
            tree.setModel(treeModel);
            tree.setRootVisible(false);
            tree.setShowsRootHandles(true);
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            ToolTipManager.sharedInstance().registerComponent(tree);
            TreeSearchable search = new TreeSearchable(tree) {
                @Override
                protected String convertElementToString(Object value) {
                    TreePath path = (TreePath)value;
                    return ((BrowserTreeNode)((TreePath)value).getLastPathComponent()).getLabel();
                }
            };
            search.setRecursive(true);
            tree.setCellRenderer(new DefaultTreeCellRenderer() {
                @Override
                public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                    BrowserTreeNode node = (BrowserTreeNode)value;
                    setIcon(node.getKind().getIcon());
                    setText(node.getLabel());
                    if ( node.getPath() != null ) {
                        setToolTipText(node.getPath().toString());
                    }
                    return this;
                }
            });
            tree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent evt) {
                    if ( tree.getSelectionPath() != null ) {
                        BrowserTreeNode node = (BrowserTreeNode)tree.getSelectionPath().getLastPathComponent();
                        removeFolderAction.setEnabled(node.getKind() == BrowserTreeNode.Kind.FOLDER);
                        if ( node.getKind() == BrowserTreeNode.Kind.CLASS ) {
                            history.add(tree.getSelectionPath());
                        }
                        updateSource();
                    }
                }
            });
        }
        catch ( IOException e ) {
            context.require(ErrorDisplayer.class).displayError(root, e);
        }
        tree.requestFocus();
    }

    private void updateSource() {
        if ( tree.getSelectionPath() == null ) {
            asmSource.setText("");
            reloadClassAction.setEnabled(false);
            return;
        }
        BrowserTreeNode node = (BrowserTreeNode)tree.getSelectionPath().getLastPathComponent();
        if ( node.getKind() != BrowserTreeNode.Kind.CLASS ) {
            asmSource.setText("");
            reloadClassAction.setEnabled(false);
        }
        else {
            boolean indent = false;
            reloadClassAction.setEnabled(true);
            StringWriter string = new StringWriter();
            try ( InputStream in = new BufferedInputStream(Files.newInputStream(node.getPath()));
                  PrintWriter out = new PrintWriter(string))
            {
                ASMifier printer = new ASMifier();
                ClassReader reader = new ClassReader(in);
                TraceClassVisitor tracer;
                if ( sourceStyle.getSelectedItem() != null && sourceStyle.getSelectedItem().equals("Bytecode") ) {
                    tracer = new TraceClassVisitor(null, new Textifier(), out);
                }
                else {
                    tracer = new TraceClassVisitor(null, new ASMifier(), out);
                    indent = true;
                }
                reader.accept(tracer, 0);
            }
            catch ( IOException e ) {
                this.context.require(ErrorDisplayer.class).displayError(root, e);
            }
            if ( indent ) {
                StringBuilder buf = new StringBuilder();
                int d = 0;
                for ( String line : string.toString().split("\\n\\r?|\\r") ) {
                    if ( line.equals("}") ) {
                        d--;
                    }
                    for ( int i = 0; i < d; i++ ) {
                        buf.append("  ");
                    }
                    buf.append(line).append('\n');
                    if ( line.equals("{") ) {
                        d++;
                    }
                }
                asmSource.setText(buf.toString());
            }
            else {
                asmSource.setText(string.toString());
            }
            asmSource.setSelectionStart(0);
            asmSource.setSelectionEnd(0);
        }
    }

    private BrowserTreeNode scanTree() throws IOException {
        history.clear();
        BrowserTreeNode root = new BrowserTreeNode(null, BrowserTreeNode.Kind.ROOT, null);
        for ( Path folderPath : folders ) {
            final BrowserTreeNode folder = new BrowserTreeNode(root, BrowserTreeNode.Kind.FOLDER, folderPath.toAbsolutePath().normalize());
            root.add(folder);
            Files.walkFileTree(folder.getPath(), new FileVisitor<Path>() {
                private BrowserTreeNode current = null;
                private final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.class");
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if ( current == null ) {
                        current = folder;
                    }
                    else {
                        BrowserTreeNode node = new BrowserTreeNode(current, BrowserTreeNode.Kind.PACKAGE, dir);
                        current.add(node);
                        current = node;
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if ( matcher.matches(file) ) {
                        current.add(new BrowserTreeNode(current, BrowserTreeNode.Kind.CLASS, file));
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.TERMINATE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    current = current.getParent();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        root.sort();
        return root;
    }

    public JComponent getComponent() {
        return root;
    }

    public void requestFocusInWindow() {
        tree.requestFocusInWindow();
    }

}
