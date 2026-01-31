package client.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * UI组件工厂类
 * 专门负责创建和定制各种UI组件，包括按钮、输入框、面板等
 */
public class UIComponentFactory {

    /**
     * 创建美化样式的按钮
     * @param text 按钮文本
     * @return 美化后的按钮组件
     */
    public static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(new Color(7, 193, 96));
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
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 6, 6);
                if (c instanceof AbstractButton && ((AbstractButton) c).getModel().isRollover()) {
                    g2.setColor(new Color(6, 174, 86));
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 6, 6);
                }
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return button;
    }

    /**
     * 创建美化的输入框：圆角、边框
     * @return 美化后的文本输入框
     */
    public static JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(new RoundBorder(6, new Color(220, 220, 220)));
        field.setOpaque(true);
        field.setBackground(Color.WHITE);
        return field;
    }

    public static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(new RoundBorder(6, new Color(220, 220, 220)));
        field.setOpaque(true);
        field.setBackground(Color.WHITE);
        return field;
    }

    /**
     * 创建圆角面板（带背景色和内边距）
     * @param bgColor 面板背景色
     * @param padding 内边距大小
     * @return 圆角面板组件
     */
    public static JPanel createRoundedPanel(Color bgColor, int padding) {
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
        panel.setOpaque(false); // 透明背景，显示圆角
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
            return new Insets(10, 12, 10, 12);
        }
    }
}