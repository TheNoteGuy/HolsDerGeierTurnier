package org.example;

import lombok.Getter;
import lombok.Setter;
import org.example.framework.HolsDerGeier;
import org.example.runnables.Runnables;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Main {
    // Screen dimensions as ratios to avoid repeated calculations
    private static final double WIDTH_RATIO = Toolkit.getDefaultToolkit().getScreenSize().width / 1920.0;
    private static final double HEIGHT_RATIO = Toolkit.getDefaultToolkit().getScreenSize().height / 1080.0;
    private static final Color BACKGROUND_COLOR = new Color(0x686868);
    private static final String WINDOW_TITLE = "Hols der Geier WS24/25";
    private static final String DEFAULT_GAMES = "1000";
    private static final int DEFAULT_FONT_SIZE = 12;

    // Static components
    public static final HolsDerGeier hdg = new HolsDerGeier();
    public static WindowManager windowManager;
    public static final JLabel counter1 = new JLabel();
    public static final JLabel counter2 = new JLabel();
    public static final JLabel counter3 = new JLabel();
    public static final JLabel player1 = new JLabel();
    public static final JLabel player2 = new JLabel();
    public static final TextField numberOfGames = new TextField(DEFAULT_GAMES, 1);


    @Getter @Setter
    private static ArrayList<String> gameOutcomes = new ArrayList<>();

    public static void main() {
        setupWindow();
    }

    private static void setupWindow() {
        // Create buttons only once
        JButton[] buttons = createButtons();
        Runnables runnables = new Runnables();

        windowManager = new WindowManager()
                .setName(WINDOW_TITLE)
                // Player labels
                .addLabel(player1, getScaledWidth(100), getScaledHeight(450), "none")
                .addLabel(player2, getScaledWidth(300), getScaledHeight(450), "none")
                // Buttons with positions
                .addButton(buttons[0], getScaledWidth(800), getScaledHeight(400), "Start Game")
                .addButton(buttons[1], getScaledWidth(100), getScaledHeight(400), "Player 1")
                .addButton(buttons[2], getScaledWidth(300), getScaledHeight(400), "Player 2")
                .addButton(buttons[3], getScaledWidth(500), getScaledHeight(400), "Reset")
                // Counters
                .addCounter(counter1, getScaledWidth(100), getScaledHeight(500))
                .addCounter(counter2, getScaledWidth(300), getScaledHeight(500))
                .addCounter(counter3, getScaledWidth(500), getScaledHeight(500))
                // Text field
                .addTextField(numberOfGames, getScaledWidth(190), getScaledHeight(350))
                // Button actions
                .runnable(buttons[1], runnables.getAddPlayer1())
                .runnable(buttons[2], runnables.getAddPlayer2())
                .runnable(buttons[3], runnables.getHardReset())
                .runnable(buttons[0], runnables.getRunGame())
                // Styling
                .changeBackground(BACKGROUND_COLOR)
                .setFontSizeForAllComponents(DEFAULT_FONT_SIZE);

        JFrame frame = windowManager.build();
        frame.revalidate();
        frame.repaint();
        runnables.getHardReset().run();
    }

    private static JButton[] createButtons() {
        JButton[] buttons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            buttons[i] = new JButton();
        }
        return buttons;
    }

    private static int getScaledWidth(int value) {
        return (int)(value * WIDTH_RATIO);
    }

    private static int getScaledHeight(int value) {
        return (int)(value * HEIGHT_RATIO);
    }
}