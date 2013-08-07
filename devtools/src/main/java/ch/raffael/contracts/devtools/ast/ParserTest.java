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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.slf4j.Logger;

import ch.raffael.util.common.logging.LogUtil;
import ch.raffael.util.swing.SwingUtil;
import ch.raffael.util.swing.WindowPlacementManager;
import ch.raffael.util.swing.context.Context;
import ch.raffael.util.swing.context.ContextManager;
import ch.raffael.util.swing.error.AbstractErrorDisplayer;
import ch.raffael.util.swing.error.ErrorDisplayer;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class ParserTest {

    private static final Preferences PREFS = Preferences.userNodeForPackage(ParserTest.class);
    private static final Logger log = LogUtil.getLogger();

    private JPanel root;
    private JTextArea source;
    private JButton parseButton;
    private AstInspector inspector;

    public ParserTest() {
        source.getActionMap().put("parse", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parseButton.doClick();
            }
        });
        source.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "parse");
        parseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    inspector.parse(source.getText());
                }
                catch ( Exception e ) {
                    ContextManager.getInstance().require(root).require(ErrorDisplayer.class).displayError(root, "Unexpected exception while parsing", e);
                }
            }
        });
    }

    public JComponent getComponent() {
        return root;
    }

    public void requestFocusInWindow() {
        source.requestFocusInWindow();
    }

    public static void main(String[] args) throws Exception {
        SwingUtil.setupMetalLookAndFeel();
        Context mainContext = ContextManager.getInstance().getRoot().create();
        mainContext.put(ErrorDisplayer.class, new AbstractErrorDisplayer() {
            @Override
            public void displayError(Component component, String message, Throwable exception, Object details) {
                JOptionPane.showMessageDialog(component, message, "Error", JOptionPane.ERROR_MESSAGE);
                log.error("{} -- Details: {}", message, details, exception);
            }
        });
        final JFrame mainFrame = new JFrame("Cel Parser Test");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final ParserTest contentPane = new ParserTest();
        mainFrame.setContentPane(SwingUtil.wrapEmptyBorder(contentPane.getComponent()));
        mainFrame.pack();
        mainContext.attach(mainFrame);
        mainContext.require(WindowPlacementManager.class).bindToPrefs(mainFrame, PREFS, "mainFrame-%s");
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                contentPane.requestFocusInWindow();
                mainFrame.removeWindowListener(this);
            }
        });
        mainFrame.setVisible(true);
    }
}
