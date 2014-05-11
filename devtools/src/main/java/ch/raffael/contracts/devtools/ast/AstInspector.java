/*
 * Copyright 2012-2014 Raffael Herzog
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

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.gui.TreeViewer;

import ch.raffael.contracts.processor.cel.ast.Clause;
import ch.raffael.contracts.processor.cel.parser.AstBuilder;
import ch.raffael.contracts.processor.cel.parser.CelLexer;
import ch.raffael.contracts.processor.cel.parser.CelParser;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class AstInspector {

    private JPanel root;
    private JTree syntaxTree;
    private JTextField nodeType;
    private JTextField nodePosition;
    private JComboBox nodeAnnotations;
    private JTextArea annotationString;
    private JTextArea log;
    private JTabbedPane tabs;
    private JSplitPane astSplitter;
    private JScrollPane parseTreeContainer;
    private JSlider parseTreeScale;

    private TreeViewer parseTreeViewer;

    public AstInspector() {
        syntaxTree.setModel(new DefaultTreeModel(AstTreeNode.empty()));
        syntaxTree.setCellRenderer(new AstTreeNode.Renderer());
        parseTreeScale.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if ( parseTreeViewer != null ) {
                    parseTreeViewer.setScale(parseTreeScale.getValue() / 1000.0 + 1.0);
                }
            }
        });
        syntaxTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        syntaxTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                AstTreeNode selection = null;
                if ( syntaxTree.getSelectionPath() != null ) {
                    selection = (AstTreeNode)syntaxTree.getSelectionPath().getLastPathComponent();
                }
                if ( selection == null || selection.getAstNode() == null ) {
                    nodePosition.setText("");
                    nodeType.setText("");
                }
                else {
                    nodeType.setText(selection.getRenderer().description(selection.getAstNode()));
                    nodePosition.setText(selection.getAstNode().getPosition().toString());
                }
            }
        });
        updateParseTree(null);
    }

    public JComponent getComponent() {
        return root;
    }

    public void parse(final String source) {
        final StringBuilder errors = new StringBuilder();
        final StringBuilder trace = new StringBuilder("Log:\n");
        ANTLRErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line, int charPositionInLine, String msg, @Nullable RecognitionException e) {
                errors.append("Line ").append(line).append(':').append(charPositionInLine)
                        .append(": ").append(msg).append('\n');
            }
        };
        final CelLexer lexer = new CelLexer(new ANTLRInputStream(source));
        final CelParser parser = new CelParser(new CommonTokenStream(lexer));
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);
        parser.addParseListener(new ParseTreeListener() {
            @Override
            public void enterEveryRule(ParserRuleContext ctx) {
                trace.append("    enter\t")
                        .append(parser.getRuleNames()[ctx.getRuleIndex()])
                        .append(", LT(1)=")
                        .append(parser.getTokenStream().LT(1).getText())
                        .append('\n');
            }

            @Override
            public void visitTerminal(TerminalNode node) {
                trace.append("    consume\t")
                        .append(node.getSymbol())
                        .append(" rule ")
                        .append(parser.getRuleNames()[parser.getContext().getRuleIndex()])
                        .append('\n');
            }

            @Override
            public void visitErrorNode(ErrorNode node) {
            }

            @Override
            public void exitEveryRule(ParserRuleContext ctx) {
                trace.append("    exit\t")
                        .append(parser.getRuleNames()[ctx.getRuleIndex()])
                        .append(", LT(1)=")
                        .append(parser.getTokenStream().LT(1).getText())
                        .append('\n');
            }
        });
        parser.setBuildParseTree(true);
        AstBuilder builder = new AstBuilder();
        builder.install(parser);
        final CelParser.ClauseContext rootContext = parser.clause();
        if ( errors.length() != 0 ) {
            errors.append('\n');
        }
        Runnable guiUpdate = new Runnable() {
            @Override
            public void run() {
                log.setText(errors.toString() + trace.toString());
                log.setSelectionStart(0);
                log.setSelectionEnd(0);
                if ( rootContext == null || rootContext.node == null ) {
                    syntaxTree.setModel(new DefaultTreeModel(AstTreeNode.empty()));
                    updateParseTree(null);
                }
                else {
                    syntaxTree.setModel(new DefaultTreeModel(AstTreeNode.from(rootContext.node)));
                    updateParseTree(new TreeViewer(Arrays.asList(parser.getRuleNames()), rootContext));
                }
                for ( int i = 0; i < syntaxTree.getRowCount(); i++ ) {
                    syntaxTree.expandRow(i);
                }
            }
        };
        if ( SwingUtilities.isEventDispatchThread() ) {
            guiUpdate.run();
        }
        else {
            SwingUtilities.invokeLater(guiUpdate);
        }
    }

    public Clause getAst() {
        return (Clause)((AstTreeNode)syntaxTree.getModel().getRoot()).getAstNode();
    }

    private void updateParseTree(final TreeViewer treeViewer) {
        this.parseTreeViewer = treeViewer;
        if ( treeViewer == null ) {
            parseTreeContainer.setViewportView(null);
        }
        else {
            parseTreeContainer.setViewportView(treeViewer);
            treeViewer.setScale(parseTreeScale.getValue() / 1000.0 + 1.0);
            new DragScroller().install(treeViewer);
            treeViewer.addMouseWheelListener(new MouseAdapter() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    parseTreeScale.setValue(parseTreeScale.getValue() - e.getWheelRotation() * 100);
                }
            });
        }
    }

    private class DragScroller implements MouseListener, MouseMotionListener {
        private final Point prevPosition = new Point();
        public void install(JComponent component) {
            component.addMouseListener(this);
            component.addMouseMotionListener(this);
        }
        @Override
        public void mousePressed(MouseEvent e) {
            if ( (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 ) {
                updatePrevPosition(e);
            }
        }
        @Override
        public void mouseDragged(MouseEvent e) {
            if ( (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 ) {
                Point viewPosition = parseTreeContainer.getViewport().getViewPosition();
                viewPosition.x += (prevPosition.x - e.getXOnScreen()) * 1;
                viewPosition.y += (prevPosition.y - e.getYOnScreen()) * 1;
                if ( viewPosition.x < 0 ) {
                    viewPosition.x = 0;
                }
                if ( viewPosition.y < 0 ) {
                    viewPosition.y = 0;
                }
                parseTreeContainer.getViewport().setViewPosition(viewPosition);
                updatePrevPosition(e);
            }
        }
        private void updatePrevPosition(MouseEvent e) {
            prevPosition.x = e.getXOnScreen();
            prevPosition.y = e.getYOnScreen();
        }
        @Override
        public void mouseClicked(MouseEvent e) {
        }
        @Override
        public void mouseReleased(MouseEvent e) {
        }
        @Override
        public void mouseEntered(MouseEvent e) {
        }
        @Override
        public void mouseExited(MouseEvent e) {
        }
        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

}
