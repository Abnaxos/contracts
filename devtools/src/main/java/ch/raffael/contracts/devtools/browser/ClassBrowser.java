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

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
public class ClassBrowser {

    private static final Preferences PREFS = Preferences.userNodeForPackage(ClassBrowser.class);
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = LogUtil.getLogger();

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
        final JFrame mainFrame = new JFrame("Contracts Class Browser");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final BrowserPanel browser = new BrowserPanel(mainContext);
        mainFrame.setContentPane(SwingUtil.wrapEmptyBorder(browser.getComponent()));
        mainFrame.pack();
        mainContext.attach(mainFrame);
        mainContext.require(WindowPlacementManager.class).bindToPrefs(mainFrame, PREFS, "mainFrame-%s");
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                browser.requestFocusInWindow();
                mainFrame.removeWindowListener(this);
            }
        });
        mainFrame.setVisible(true);
    }

}
