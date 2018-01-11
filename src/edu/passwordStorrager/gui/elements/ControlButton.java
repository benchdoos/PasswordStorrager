package edu.passwordStorrager.gui.elements;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ControlButton extends JButton {
    private static final Color LIGHT_GREY = new Color(196, 196, 196);
    private static final Color DARKER_GREY = new Color(172, 172, 172);
    private static final Color DARKEST_GREY = new Color(162, 162, 162);

    private static final Border MOUSE_RELEASED_BORDER = new ControlButtonBorder(LIGHT_GREY);
    private static final Border MOUSE_OVER_BORDER = new ControlButtonBorder(DARKER_GREY);
    private static final Border MOUSE_PRESSED_BORDER = new ControlButtonBorder(DARKEST_GREY);

    private static final int WIDTH = 40;
    private static final int HEIGHT = 22;
    private static final int RADIUS = 5;

    final int HORIZONTAL = 0;
    final int VERTICAL = 1;
    int direction = VERTICAL;
    int outerRoundRectSize = 10;
    int innerRoundRectSize = 8;

    GradientPaint GP;

    Border innerBorder = BorderFactory.createEmptyBorder(0, 8, 0, 8);
    Border outerBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);


    public ControlButton() {

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    setBorder(BorderFactory.createCompoundBorder(MOUSE_OVER_BORDER, innerBorder));
                }
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(BorderFactory.createCompoundBorder(MOUSE_RELEASED_BORDER, innerBorder));
                super.mouseExited(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    setBorder(BorderFactory.createCompoundBorder(MOUSE_PRESSED_BORDER, innerBorder));
                }
                super.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBorder(BorderFactory.createCompoundBorder(MOUSE_RELEASED_BORDER, innerBorder));
                super.mouseReleased(e);
            }
        });

        setBorder(BorderFactory.createCompoundBorder(MOUSE_RELEASED_BORDER, innerBorder));
//        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        Color white = new Color(251, 251, 251);
        Color grey = new Color(226, 226, 226);

        ButtonModel model = getModel();

        g.setColor(white);
        g.fillRoundRect(0, 0, WIDTH, HEIGHT, RADIUS, RADIUS);
        
        if (model.isPressed()) {
            g.setColor(grey);
            g.fillRoundRect(0, 0, WIDTH, HEIGHT, RADIUS, RADIUS);
        }
//        g.dispose();
        
        /*
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int h = getHeight();
        int w = getWidth();
        ButtonModel model = getModel();
        if (!model.isEnabled()) {
            setForeground(Color.GRAY);
            GP = new GradientPaint(0, 0, new Color(192, 192, 192), 0, h, new Color(192, 192, 192),
                    true);
        } else {
            setForeground(Color.WHITE);
            if (model.isRollover()) {
                if (direction == VERTICAL) {
                    GP = new GradientPaint(0, 0, startColor, 0, h, rollOverColor,
                            true);
                } else {
                    GP = new GradientPaint(0, 0, startColor, w, 0, rollOverColor,
                            true);
                }
            } else {
                if (direction == VERTICAL) {
                    GP = new GradientPaint(0, 0, startColor, 0, h, endColor,
                            true);
                } else {
                    GP = new GradientPaint(0, 0, startColor, w, 0, endColor,
                            true);
                }
            }
        }
        g2d.setPaint(GP);
        GradientPaint p1;
        GradientPaint p2;
        if (getModel().isPressed()) {

            if (direction == VERTICAL) {
                GP = new GradientPaint(0, 0, startColor, 0, h, pressedColor,
                        true);
            } else {
                GP = new GradientPaint(0, 0, startColor, w, 0, pressedColor,
                        true);
            }
            g2d.setPaint(GP);

            p1 = new GradientPaint(0, 0, new Color(0, 0, 0), 0, h - 1,
                    new Color(100, 100, 100));
            p2 = new GradientPaint(0, 1, new Color(0, 0, 0, 50), 0, h - 3,
                    new Color(255, 255, 255, 100));
        } else {
            p1 = new GradientPaint(0, 0, new Color(100, 100, 100), 0, h - 1,
                    new Color(0, 0, 0));
            p2 = new GradientPaint(0, 1, new Color(255, 255, 255, 100), 0,
                    h - 3, new Color(0, 0, 0, 50));
            GP = new GradientPaint(0, 0, startColor, 0, h, endColor, true);
        }
        RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, 0, w - 1,
                h - 1, outerRoundRectSize, outerRoundRectSize);
        Shape clip = g2d.getClip();
        g2d.clip(r2d);
        g2d.fillRect(0, 0, w, h);
        g2d.setClip(clip);
//        g2d.setPaint(p1);
        *//*g2d.drawRoundRect(0, 0, w - 1, h - 1, outerRoundRectSize,
                outerRoundRectSize);*//*
//        g2d.setPaint(p2);
        *//*g2d.drawRoundRect(1, 1, w - 3, h - 3, innerRoundRectSize,
                innerRoundRectSize);*//*
        g2d.dispose();
*/
        super.paintComponent(g);
    }

    static class ControlButtonBorder implements Border {
        Color color;

        public ControlButtonBorder(Color color) {
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(color);
            g.drawRoundRect(x, y, WIDTH, HEIGHT, RADIUS, RADIUS);
            g.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 5, 4, 10);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}


