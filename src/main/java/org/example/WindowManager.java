package org.example;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class WindowManager {
    private final JFrame frame;
    private final int screenWidth;
    private static final int widthScreen = Toolkit.getDefaultToolkit().getScreenSize().width;
    private final int screenHeight;
    public static Main main;

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

    public static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Button background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);

                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);

                // Subtle border
                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setBackground(bgColor);
        button.setForeground(Main.TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setFont(Main.LABEL_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 32));

        // Enhanced hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            private final Color originalColor = bgColor;
            private final Color hoverColor = blend(bgColor, Color.WHITE, 0.15f);
            private final Color pressedColor = blend(bgColor, Color.BLACK, 0.1f);

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(pressedColor);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getMousePosition() != null ? hoverColor : originalColor);
            }
        });

        return button;
    }

    private static Color blend(Color c1, Color c2, float ratio) {
        return new Color(
                Math.min((int)(c1.getRed() * (1 - ratio) + c2.getRed() * ratio), 255),
                Math.min((int)(c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio), 255),
                Math.min((int)(c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio), 255)
        );
    }

    public static JPanel createPlayerPanel(JLabel nameLabel, JLabel scoreLabel, JButton addButton) {
        // Create main panel with fixed width
        int panelWidth = (int)(widthScreen * 0.2); 
        JPanel panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                // Fixed width of 300px, height will be determined by layout
                return new Dimension(panelWidth, super.getPreferredSize().height);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }

            // Add subtle gradient background
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(52, 52, 54),
                        0, getHeight(), new Color(44, 44, 46)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);

                // Add subtle highlight at top
                g2.setColor(new Color(255, 255, 255, 0));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(10, 1, getWidth()-10, 1);

                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Main.SECONDARY_BG);
        panel.setBorder(new CompoundBorder(
                new EmptyBorder(2, 2, 2, 2), // Outer padding
                new CompoundBorder(
                        // Subtle border
                        new LineBorder(new Color(255, 255, 255, 0), 10) {
                            @Override
                            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(getLineColor());
                                g2.drawRoundRect(x, y, width-1, height-1, 10, 10);
                                g2.dispose();
                            }
                        },
                        new EmptyBorder(25, 30, 25, 30) // Inner padding
                )
        ));
        panel.setOpaque(false);

        // Fixed width for labels
        nameLabel.setPreferredSize(new Dimension((int)(panelWidth * 0.8), 30));
        nameLabel.setMaximumSize(new Dimension(250, 30));
        nameLabel.setForeground(Main.TEXT_PRIMARY);
        nameLabel.setFont(Main.TITLE_FONT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreLabel.setPreferredSize(new Dimension((int)(panelWidth * 0.8), 60));
        scoreLabel.setMaximumSize(new Dimension(250, 60));
        scoreLabel.setForeground(Main.TEXT_PRIMARY);
        scoreLabel.setFont(Main.SCORE_FONT);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        addButton.setPreferredSize(new Dimension((int)(panelWidth * 0.6), 32));
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Add components with fixed spacing
        panel.add(Box.createVerticalGlue());
        panel.add(nameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(scoreLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(addButton);
        panel.add(Box.createVerticalGlue());

        return panel;
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