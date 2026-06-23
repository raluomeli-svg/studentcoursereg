package com.nimet.rcrs.gui;

import javax.swing.*;
import java.awt.*;

public class UITheme {

    public static final Color PRIMARY     = new Color(0x1a3a5c);
    public static final Color SECONDARY   = new Color(0x2e86de);
    public static final Color ACCENT      = new Color(0xf9ca24);
    public static final Color BG_PAGE     = new Color(0xf0f2f5);
    public static final Color BG_CARD     = Color.WHITE;
    public static final Color SUCCESS     = new Color(0x00b894);
    public static final Color SUCCESS_BG  = new Color(0xe8f5e9);
    public static final Color ERROR       = new Color(0xd63031);
    public static final Color TEXT        = new Color(0x2d3436);
    public static final Color TEXT_MUTED  = new Color(0x636e72);
    public static final Color BORDER      = new Color(0xdfe6e9);

    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_MONO    = new Font("Monospaced", Font.BOLD, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(SECONDARY);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_LABEL);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton successButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(SUCCESS);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_LABEL);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ERROR);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_LABEL);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(0xdfe6e9));
        b.setForeground(TEXT);
        b.setFont(FONT_LABEL);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));
        return p;
    }

    public static JPanel page() {
        JPanel p = new JPanel();
        p.setBackground(BG_PAGE);
        return p;
    }

    private UITheme() {}
}
