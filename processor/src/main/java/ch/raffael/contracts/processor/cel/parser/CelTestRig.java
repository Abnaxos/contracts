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
package ch.raffael.contracts.processor.cel.parser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import com.google.common.io.Files;
import org.antlr.v4.runtime.misc.TestRig;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class CelTestRig {

    public static void main(String[] args) throws Exception {
        final JFrame frame = new JFrame("Enter Cel Expression");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout());
        final JTextArea source = new JTextArea();
        source.setLineWrap(true);
        source.setWrapStyleWord(true);
        panel.add(BorderLayout.CENTER, new JScrollPane(source));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        final JButton parseButton = new JButton("Parse");
        panel.add(BorderLayout.SOUTH, parseButton);
        parseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                new Thread(new Runnable() {
                    public void run() {
                        try {

                            File dest = null;
                            dest = File.createTempFile("cel-testrig-", ".cel");
                            dest.deleteOnExit();
                            Files.write(source.getText(), dest, Charset.defaultCharset());
                            TestRig.main(new String[] {
                                    "ch.raffael.contracts.processor.cel.parser.Cel", "clause",
                                    "-gui", "-trace",
                                    dest.toString()
                            });
                            dest.delete();
                        }
                        catch ( Exception e ) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }).start();
                //frame.setVisible(false);
            }
        });
        source.getActionMap().put("parse", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parseButton.doClick();
            }
        });
        source.getActionMap().put("quit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        source.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "parse");
        source.getInputMap().put(KeyStroke.getKeyStroke("ESC"), "quit");
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
    }

}
