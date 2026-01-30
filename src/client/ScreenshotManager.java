package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;

public class ScreenshotManager {
    private final JFrame parent;

    public ScreenshotManager(JFrame parent) {
        this.parent = parent;
    }

    public File captureFullScreen() {
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenImage = robot.createScreenCapture(screenRect);
            String tempFileName = "screenshot_" + UUID.randomUUID() + ".png";
            File tempFile = new File(System.getProperty("java.io.tmpdir"), tempFileName);
            ImageIO.write(screenImage, "PNG", tempFile);
            JOptionPane.showMessageDialog(parent, "截图成功！临时文件路径：" + tempFile.getAbsolutePath());
            return tempFile;
        } catch (AWTException ex) {
            JOptionPane.showMessageDialog(parent, "截图权限不足！请检查是否允许程序访问屏幕。");
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "保存截图失败：" + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public void handleScreenshot(File selectedFile, Runnable sendFileCallback, JComboBox<String> chatTypeBox) {
        try {
            File screenshotFile = captureFullScreen();
            if (screenshotFile == null || !screenshotFile.exists()) {
                JOptionPane.showMessageDialog(parent, "截图失败！");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    parent,
                    "截图已生成，是否发送给当前选中的" + (chatTypeBox.getSelectedItem().equals("群聊") ? "群聊" : "好友") + "？",
                    "确认发送截图",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                screenshotFile.delete();
                return;
            }

            selectedFile = screenshotFile;
            sendFileCallback.run();

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    if (screenshotFile.exists()) {
                        screenshotFile.delete();
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "截图发送失败：" + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
