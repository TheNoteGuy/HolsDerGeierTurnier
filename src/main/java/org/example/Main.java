package org.example;

import lombok.Getter;
import lombok.Setter;
import org.example.framework.HolsDerGeier;
import org.example.runnables.Runnables;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    // Color scheme
    private static final Color BACKGROUND = new Color(28, 28, 30);
    private static final Color SECONDARY_BG = new Color(44, 44, 46);
    private static final Color ACCENT = new Color (100, 100, 166) ;
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(174, 174, 178);
    private static final Color SUCCESS = new Color(63,143,41);
    private static final Color DANGER = new Color(191,16,41);

    // Font constants (using system font fallbacks)
    private static final String FONT_FAMILY = "Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif";
    private static final Font TITLE_FONT = new Font(FONT_FAMILY, Font.BOLD, 20);
    private static final Font SCORE_FONT = new Font(FONT_FAMILY, Font.BOLD, 42);
    private static final Font LABEL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 13);
    private static final Font INPUT_FONT = new Font(FONT_FAMILY, Font.PLAIN, 16);

    public static final HolsDerGeier hdg = new HolsDerGeier();
    private static final Runnables runnables = new Runnables();
    public static WindowManager windowManager;

    public static JLabel counter1 = new JLabel("0", SwingConstants.CENTER);
    public static JLabel counter2 = new JLabel("0", SwingConstants.CENTER);
    public static JLabel player1 = new JLabel("Player 1", SwingConstants.CENTER);
    public static JLabel player2 = new JLabel("Player 2", SwingConstants.CENTER);

    @Getter @Setter
    private static List<String> gameOutcomes = new ArrayList<>();

    public static JTextField numberOfGames = new JTextField("10000");

    public static void main(String[] args) {
        setupUIStyle();
        createAndShowGUI();
    }

    private static void setupUIStyle() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JButton createStyledButton(String text, Color bgColor) {
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
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setFont(LABEL_FONT);
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

    private static JPanel createPlayerPanel(JLabel nameLabel, JLabel scoreLabel, JButton addButton) {
        // Create main panel with fixed width
        JPanel panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                // Fixed width of 300px, height will be determined by layout
                return new Dimension(300, super.getPreferredSize().height);
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
        panel.setBackground(SECONDARY_BG);
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
        nameLabel.setPreferredSize(new Dimension(250, 30));
        nameLabel.setMaximumSize(new Dimension(250, 30));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setFont(TITLE_FONT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreLabel.setPreferredSize(new Dimension(250, 60));
        scoreLabel.setMaximumSize(new Dimension(250, 60));
        scoreLabel.setForeground(TEXT_PRIMARY);
        scoreLabel.setFont(SCORE_FONT);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

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

    private static void createAndShowGUI() {
        JButton playerAddButton1 = createStyledButton("Select Player 1", ACCENT);
        JButton playerAddButton2 = createStyledButton("Select Player 2", ACCENT);
        JButton resetButton = createStyledButton("Reset Tournament", DANGER);
        JButton gameStartButton = createStyledButton("Start Games", SUCCESS);

        gameOutcomes = new ArrayList<>();

        // Game settings panel
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(SECONDARY_BG);
        settingsPanel.setBorder(new CompoundBorder(
                new LineBorder(BACKGROUND, 1),
                new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel gamesLabel = new JLabel("Number of Iterations");
        gamesLabel.setFont(LABEL_FONT);
        gamesLabel.setForeground(TEXT_SECONDARY);
        gamesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        numberOfGames.setHorizontalAlignment(JTextField.CENTER);
        numberOfGames.setFont(INPUT_FONT);
        numberOfGames.setPreferredSize(new Dimension(180, 32));
        numberOfGames.setMaximumSize(new Dimension(180, 32));
        numberOfGames.setBackground(BACKGROUND);
        numberOfGames.setForeground(TEXT_PRIMARY);
        numberOfGames.setBorder(new CompoundBorder(
                new LineBorder(ACCENT, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        numberOfGames.setCaretColor(TEXT_PRIMARY);
        numberOfGames.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(SECONDARY_BG);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        gameStartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(gameStartButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(resetButton);
        buttonPanel.add(Box.createVerticalGlue());

        settingsPanel.add(Box.createVerticalGlue());
        settingsPanel.add(gamesLabel);
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        settingsPanel.add(numberOfGames);
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        settingsPanel.add(buttonPanel);
        settingsPanel.add(Box.createVerticalGlue());

        // Main panel layout
        // Main panel layout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL; // Changed from BOTH to VERTICAL
        gbc.weighty = 1.0;
        gbc.insets = new Insets(20, 20, 20, 20);

        // Left player panel
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.LINE_END; // Align to the right
        mainPanel.add(createPlayerPanel(player1, counter1, playerAddButton1), gbc);

        // Center settings panel
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(settingsPanel, gbc);

        // Right player panel
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.LINE_START; // Align to the left
        mainPanel.add(createPlayerPanel(player2, counter2, playerAddButton2), gbc);


        windowManager = new WindowManager()
                .setName("Tournament Manager")
                .addCustomPanel(mainPanel)
                .runnable(playerAddButton1, runnables.getAddPlayer1())
                .runnable(playerAddButton2, runnables.getAddPlayer2())
                .runnable(resetButton, runnables.getHardReset())
                .runnable(gameStartButton, runnables.getRunGame());

        JFrame frame = windowManager.build();
        frame.revalidate();
        frame.repaint();

        runnables.getHardReset().run();
    }
}