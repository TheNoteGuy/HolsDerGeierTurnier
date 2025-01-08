package org.example;

import javax.swing.*;
import java.awt.*;

public class WindowManager {
    private final JFrame frame;
    private final int screenWidth;
    private final int screenHeight;

    public WindowManager() {
        screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(screenWidth * 3/4, screenHeight * 3/4);
        frame.setLocationRelativeTo(null);  // Center on screen
        frame.setMinimumSize(new Dimension(800, 600));
    }


    public WindowManager setName(String name) {
        frame.setTitle(name);
        return this;
    }

    public WindowManager addCustomPanel(JPanel panel) {
        frame.add(panel, BorderLayout.CENTER);
        return this;
    }

    public WindowManager runnable(JButton button, Runnable runnable) {
        button.addActionListener(e -> {
            runnable.run();
            frame.revalidate();
            frame.repaint();
        });
        return this;
    }

    public WindowManager changeCounter(JLabel label, String text) {
        SwingUtilities.invokeLater(() -> {
            label.setText(text);
            label.revalidate();
            label.repaint();
        });
        return this;
    }

    public void changeLabel(JLabel label, String text) {
        SwingUtilities.invokeLater(() -> {
            label.setText(text);
            label.revalidate();
            label.repaint();
        });
    }

    public WindowManager onButtonClick(JButton button, JLabel label) {
        button.addActionListener(e -> {
            int count = Integer.parseInt(label.getText());
            count++;
            changeCounter(label, String.valueOf(count));
        });
        return this;
    }

    public WindowManager onButtonClick(JButton button, JLabel label, String text) {
        button.addActionListener(e -> {
            changeLabel(label, text);
        });
        return this;
    }

    public void refresh() {
        frame.revalidate();
        frame.repaint();
    }

    public JFrame build() {
        frame.setVisible(true);
        return frame;
    }
}