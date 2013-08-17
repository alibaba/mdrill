/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.regexp;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

/**
 * Interactive demonstration and testing harness for regular expressions classes.
 * @author <a href="mailto:jonl@muppetlabs.com">Jonathan Locke</a>
 * @version $Id: REDemo.java 126149 2004-02-19 02:35:28Z vgritsenko $
 */
public class REDemo extends Applet implements TextListener
{
    /**
     * Matcher and compiler objects
     */
    RE r = new RE();
    REDebugCompiler compiler = new REDebugCompiler();

    /**
     * Components
     */
    TextField fieldRE;          // Field for entering regexps
    TextField fieldMatch;       // Field for entering match strings
    TextArea outRE;             // Output of RE compiler
    TextArea outMatch;          // Results of matching operation

    /**
     * Add controls and init applet
     */
    public void init()
    {
        // Add components using the dreaded GridBagLayout
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.EAST;
        gb.setConstraints(add(new Label("Regular expression:", Label.RIGHT)), c);
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gb.setConstraints(add(fieldRE = new TextField("\\[([:javastart:][:javapart:]*)\\]", 40)), c);
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.EAST;
        gb.setConstraints(add(new Label("String:", Label.RIGHT)), c);
        c.gridy = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.WEST;
        gb.setConstraints(add(fieldMatch = new TextField("aaa([foo])aaa", 40)), c);
        c.gridy = 2;
        c.gridx = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.weightx = 1.0;
        gb.setConstraints(add(outRE = new TextArea()), c);
        c.gridy = 2;
        c.gridx = GridBagConstraints.RELATIVE;
        gb.setConstraints(add(outMatch = new TextArea()), c);

        // Listen to text changes
        fieldRE.addTextListener(this);
        fieldMatch.addTextListener(this);

        // Initial UI update
        textValueChanged(null);
    }

    /**
     * Say something into RE text area
     * @param s What to say
     */
    void sayRE(String s)
    {
        outRE.setText(s);
    }

    /**
     * Say something into match text area
     * @param s What to say
     */
    void sayMatch(String s)
    {
        outMatch.setText(s);
    }

    /**
     * Convert throwable to string
     * @param t Throwable to convert to string
     */
    String throwableToString(Throwable t)
    {
        String s = t.getClass().getName();
        String m;
        if ((m = t.getMessage()) != null)
        {
            s += "\n" + m;
        }
        return s;
    }

    /**
     * Change regular expression
     * @param expr Expression to compile
     */
    void updateRE(String expr)
    {
        try
        {
            // Compile program
            r.setProgram(compiler.compile(expr));

            // Dump program into RE feedback area
            CharArrayWriter w = new CharArrayWriter();
            compiler.dumpProgram(new PrintWriter(w));
            sayRE(w.toString());
            System.out.println(w);
        }
        catch (Exception e)
        {
            r.setProgram(null);
            sayRE(throwableToString(e));
        }
        catch (Throwable t)
        {
            r.setProgram(null);
            sayRE(throwableToString(t));
        }
    }

    /**
     * Update matching info by matching the string against the current
     * compiled regular expression.
     * @param match String to match against
     */
    void updateMatch(String match)
    {
        try
        {
            // If the string matches the regexp
            if (r.match(match))
            {
                // Say that it matches
                String out = "Matches.\n\n";

                // Show contents of parenthesized subexpressions
                for (int i = 0; i < r.getParenCount(); i++)
                {
                    out += "$" + i + " = " + r.getParen(i) + "\n";
                }
                sayMatch(out);
            }
            else
            {
                // Didn't match!
                sayMatch("Does not match");
            }
        }
        catch (Throwable t)
        {
            sayMatch(throwableToString(t));
        }
    }

    /**
     * Called when text values change
     * @param e TextEvent
     */
    public void textValueChanged(TextEvent e)
    {
        // If it's a generic update or the regexp changed...
        if (e == null || e.getSource() == fieldRE)
        {
            // Update regexp
            updateRE(fieldRE.getText());
        }

        // We always need to update the match results
        updateMatch(fieldMatch.getText());
    }

    /**
     * Main application entrypoint.
     * @param arg Command line arguments
     */
    static public void main(String[] arg)
    {
        Frame f = new Frame("RE Demo");
        // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        REDemo demo = new REDemo();
        f.add(demo);
        demo.init();
        f.pack();
        f.setVisible(true);
    }
}
