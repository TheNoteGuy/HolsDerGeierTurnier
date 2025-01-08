package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class WindowManager {
    private static final int DEFAULT_BUTTON_HEIGHT = 30;
    private static final int DEFAULT_LABEL_WIDTH = 200;
    private static final int DEFAULT_LABEL_HEIGHT = 30;
    private static final int BUTTON_PADDING = 50;
    private static final int TEXT_FIELD_WIDTH = 100;

    private final JFrame frame;
    private final Dimension screenSize;
    private final Map<Component, Font> fontCache = new HashMap<>();  // Cache for fonts

    public WindowManager() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setSize(screenSize.width, screenSize.height);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public WindowManager changeBackground(Color color) {
        frame.getContentPane().setBackground(color);
        return this;
    }

    public WindowManager setName(String name) {
        frame.setTitle(name);
        return this;
    }

    public WindowManager setSize(int x, int y) {
        frame.setSize(x, y);
        return this;
    }

    public WindowManager addButton(JButton button, int x, int y, String text) {
        button.setText(text);
        FontMetrics metrics = button.getFontMetrics(button.getFont());
        button.setBounds(x, y, metrics.stringWidth(text) + BUTTON_PADDING, DEFAULT_BUTTON_HEIGHT);
        frame.add(button);
        return this;
    }

    public WindowManager addText(int x, int y, String text) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, DEFAULT_LABEL_WIDTH, DEFAULT_LABEL_HEIGHT);
        frame.add(label);
        return this;
    }

    public WindowManager addCounter(JLabel label, int x, int y) {
        label.setText("0");
        label.setBounds(x, y, DEFAULT_LABEL_WIDTH, DEFAULT_LABEL_HEIGHT);
        frame.add(label);
        return this;
    }

    public WindowManager changeCounter(JLabel label, String text) {
        label.setText(text);
        return this;
    }

    public WindowManager removeLable(JLabel label) {
        Container parent = frame.getParent();
        if (parent != null) {
            parent.remove(label);
            frame.revalidate();
            frame.repaint();
        }
        return this;
    }

    public WindowManager addLabel(JLabel label, int x, int y) {
        return addLabel(label, x, y, label.getText());
    }

    public WindowManager addLabel(JLabel label, int x, int y, String text) {
        label.setBounds(x, y, DEFAULT_LABEL_WIDTH, DEFAULT_LABEL_HEIGHT);
        if (text != null) label.setText(text);
        frame.add(label);
        return this;
    }

    public WindowManager addTextField(TextField textField, int x, int y) {
        textField.setBounds(x, y, TEXT_FIELD_WIDTH, DEFAULT_BUTTON_HEIGHT);
        frame.add(textField);
        return this;
    }

    public void changeLabel(JLabel label, String text) {
        if (label != null) label.setText(text);
    }

    public WindowManager onButtonClick(JButton button, JLabel label) {
        button.addActionListener(e -> {
            try {
                int count = Integer.parseInt(label.getText()) + 1;
                changeCounter(label, String.valueOf(count));
            } catch (NumberFormatException ignored) {
                changeCounter(label, "1");
            }
        });
        return this;
    }

    public WindowManager runnable(JButton button, Runnable runnable) {
        button.addActionListener(e -> {
            runnable.run();
            refresh();
        });
        return this;
    }

    public WindowManager onButtonClick(JButton button, JLabel label, String text) {
        button.addActionListener(e -> changeLabel(label, text));
        return this;
    }

    public WindowManager setFontSizeForAllComponents(int size) {
        Container contentPane = frame.getContentPane();
        for (Component component : contentPane.getComponents()) {
            if (component instanceof JComponent) {
                Font existingFont = fontCache.get(component);
                if (existingFont == null || existingFont.getSize() != size) {
                    Font newFont = new Font(component.getFont().getName(),
                            component.getFont().getStyle(),
                            size);
                    component.setFont(newFont);
                    fontCache.put(component, newFont);
                }
            }
        }
        return this;
    }

    public void refresh() {
        frame.revalidate();
        frame.repaint();
    }

    public JFrame build() {
        return frame;
    }
}