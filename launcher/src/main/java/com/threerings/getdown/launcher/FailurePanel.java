//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/threerings/getdown/blob/master/LICENSE

package com.threerings.getdown.launcher;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.Spacer;
import com.samskivert.swing.VGroupLayout;
import com.threerings.getdown.util.MessageUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static com.threerings.getdown.Log.log;

/**
 * Displays Getdown failure message in separate swing panel.
 */
public final class FailurePanel extends JFrame {
    public FailurePanel(ResourceBundle msgs, String failureMessage)
    {
        _msgs = msgs;

        setLayout(new VGroupLayout());
        setResizable(true);
        setTitle(get("m.failure_title"));

        String messageText = String.format("<html><body>%s</body></html>", xlate(failureMessage));

        JLabel message = new JLabel(messageText);
        message.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        add(message);

        add(message);

        JPanel row = GroupLayout.makeButtonBox(GroupLayout.CENTER);
        JButton button = new JButton(get("m.failure_ok"));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        row.add(button);
        getRootPane().setDefaultButton(button);
        add(row);
        add(new Spacer(5, 5));

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /** Used to look up localized messages. */
    protected String get (String key)
    {
        // if this string is tainted, we don't translate it, instead we
        // simply remove the taint character and return it to the caller
        if (MessageUtil.isTainted(key)) {
            return MessageUtil.untaint(key);
        }
        try {
            return _msgs.getString(key);
        } catch (MissingResourceException mre) {
            log.warning("Missing translation message '" + key + "'.");
            return key;
        }
    }

    protected String xlate (String compoundKey)
    {
        // to be more efficient about creating unnecessary objects, we
        // do some checking before splitting
        int tidx = compoundKey.indexOf('|');
        if (tidx == -1) {
            return get(compoundKey);

        } else {
            String key = compoundKey.substring(0, tidx);
            String argstr = compoundKey.substring(tidx+1);
            String[] args = argstr.split("\\|");
            // unescape and translate the arguments
            for (int i = 0; i < args.length; i++) {
                // if the argument is tainted, do no further translation
                // (it might contain |s or other fun stuff)
                if (MessageUtil.isTainted(args[i])) {
                    args[i] = MessageUtil.unescape(MessageUtil.untaint(args[i]));
                } else {
                    args[i] = xlate(MessageUtil.unescape(args[i]));
                }
            }
            return get(key, args);
        }
    }
    protected String get (String key, String[] args)
    {
        String msg = get(key);
        if (msg != null) return MessageFormat.format(MessageUtil.escape(msg), (Object[])args);
        return key + Arrays.asList(args);
    }

   // protected Getdown _getdown;
    protected ResourceBundle _msgs;
}
