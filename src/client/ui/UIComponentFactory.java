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
        // 设置按钮样式
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(new Color(64, 158, 255)); // 蓝色主色调
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); // 内边距
        button.setFocusPainted(false); // 去掉焦点框
        button.setBorderPainted(false); // 去掉边框
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 手型光标
        // 圆角设置（通过设置按钮的形状）
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
                // 绘制圆角背景
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                // 悬停效果
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

    /**
     * 创建美化的输入框：圆角、边框
     * @return 美化后的文本输入框
     */
    public static JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        // 设置自定义圆角边框
        field.setBorder(new RoundBorder(8, new Color(204, 204, 204)));
        field.setOpaque(true); // 确保背景不透明，显示边框效果
        field.setBackground(Color.WHITE);
        return field;
    }

    /**
     * 美化密码框：复用自定义圆角边框
     * @return 美化后的密码输入框
     */
    public static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(new RoundBorder(8, new Color(204, 204, 204)));
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

    /**
     * 自定义圆角边框（替代重写UI的方式）
     */
    public static class RoundBorder extends AbstractBorder {
        private int radius; // 圆角半径
        private Color borderColor;

        public RoundBorder(int radius, Color borderColor) {
            this.radius = radius;
            this.borderColor = borderColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 绘制圆角边框
            g2.setColor(borderColor);
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 10, 8, 10); // 内边距，和之前保持一致
        }
    }
}