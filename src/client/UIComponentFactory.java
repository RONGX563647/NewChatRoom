package client;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

public class UIComponentFactory {

    public JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(new Color(64, 158, 255));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                c.setOpaque(false);
                ((AbstractButton) c).setContentAreaFilled(false);
            }
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                if (c instanceof AbstractButton && ((AbstractButton) c).getModel().isRollover()) {
                    g2.setColor(new Color(84, 172, 255));
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                }
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return button;
    }

    public JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(new RoundBorder(8, new Color(204, 204, 204)));
        field.setOpaque(true);
        field.setBackground(Color.WHITE);
        return field;
    }

    public JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(new RoundBorder(8, new Color(204, 204, 204)));
        field.setOpaque(true);
        field.setBackground(Color.WHITE);
        return field;
    }

    public JPanel createRoundedPanel(Color bgColor, int padding) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        panel.setOpaque(false);
        return panel;
    }

    public static class RoundBorder extends AbstractBorder {
        private int radius;
        private Color borderColor;

        public RoundBorder(int radius, Color borderColor) {
            this.radius = radius;
            this.borderColor = borderColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 10, 8, 10);
        }
    }
}
