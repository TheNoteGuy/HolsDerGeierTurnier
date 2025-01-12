
package org.example;

import lombok.Getter;
import lombok.Setter;

import org.example.trainingBots.*;
import org.example.bots.losers.IntelligentererGeier;
import org.example.bots.losers.RandomBot;
import org.example.bots.round1.TestBot;
import org.example.framework.HolsDerGeier;
import org.example.neuralHelpers.network.NeuralGeierTrainer;
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
    public static final Color BACKGROUND = new Color(28, 28, 30);
    public static final Color SECONDARY_BG = new Color(44, 44, 46);
    public static final Color ACCENT = new Color(100, 100, 166);
    public static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    public static final Color TEXT_SECONDARY = new Color(174, 174, 178);
    public static final Color SUCCESS = new Color(63, 143, 41);
    public static final Color DANGER = new Color(191, 16, 41);

    // Font constants (using system font fallbacks)
    public static final String FONT_FAMILY = "Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif";
    public static final Font TITLE_FONT = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font SCORE_FONT = new Font(FONT_FAMILY, Font.BOLD, 42);
    public static final Font LABEL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font INPUT_FONT = new Font(FONT_FAMILY, Font.PLAIN, 16);

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

    private static void createAndShowGUI() {
        JButton playerAddButton1 = WindowManager.createStyledButton("Select Player 1", ACCENT);
        JButton playerAddButton2 = WindowManager.createStyledButton("Select Player 2", ACCENT);
        JButton resetButton = WindowManager.createStyledButton("Reset Tournament", DANGER);
        JButton gameStartButton = WindowManager.createStyledButton("Start Games", SUCCESS);

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
        gamesLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

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
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        settingsPanel.add(numberOfGames);
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 120)));
        settingsPanel.add(buttonPanel);
        settingsPanel.add(Box.createVerticalGlue());

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
        mainPanel.add(WindowManager.createPlayerPanel(player1, counter1, playerAddButton1), gbc);

        // Center settings panel
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(settingsPanel, gbc);

        // Right player panel
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.LINE_START; // Align to the left
        mainPanel.add(WindowManager.createPlayerPanel(player2, counter2, playerAddButton2), gbc);

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


        NeuralGeierTrainer trainer = new NeuralGeierTrainer(800,hdg);
        trainer.addOpponents(new IntelligentererGeier(), new Loick(),new LoickV2(), new NoteGeier(), new OptimizedCounterBot(), new OptimizedArrayGeier(), new RandomBot(), new TestBot());
        trainer.startTraining(true);


    }
}