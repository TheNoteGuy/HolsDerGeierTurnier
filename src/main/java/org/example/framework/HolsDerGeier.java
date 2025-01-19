package org.example.framework;

import lombok.Getter;
import org.example.ClassMover;
import org.example.Main;
import org.example.PlayerBotGetter;
import java.security.SecureRandom;
import java.util.*;
import javax.swing.SwingUtilities;

public class HolsDerGeier {
    private static final int MIN_CARD = 1;
    private static final int MAX_CARD = 15;
    private static final int INIT_CARDS_SIZE = 15;

    private final ArrayList<Integer> cards = new ArrayList<>(INIT_CARDS_SIZE);
    @SuppressWarnings("unchecked")
    private final LinkedList<Integer>[] playerCards = (LinkedList<Integer>[]) new LinkedList[2];
    @Getter public static final HolsDerGeierSpieler[] players = new HolsDerGeierSpieler[2];
    private final int[] playerPoints = new int[2];
    private final int[] gamePoints = new int[2];
    private final SecureRandom random = new SecureRandom();

    @Getter private final List<Integer> winsPlayer1 = new ArrayList<>();
    @Getter private final List<Integer> winsPlayer2 = new ArrayList<>();
    @Getter private final List<Integer> draw = new ArrayList<>();
    @Getter private final PlayerBotGetter playerBotGetter = new PlayerBotGetter();

    @Getter public static long timeStart = 0;
    @Getter public static long timeStart1 = 0;

    public HolsDerGeier() {
        playerCards[0] = new LinkedList<>();
        playerCards[1] = new LinkedList<>();
    }

    private void resetGame() {
        cards.clear();
        playerCards[0].clear();
        playerCards[1].clear();
        playerPoints[0] = playerPoints[1] = 0;

        for (int i = -5; i < 11; i++) {
            if (i != 0) cards.add(i);
        }

        if (players[0] != null) players[0].reset();
        if (players[1] != null) players[1].reset();
    }

    public void fullReset() {
        playerBotGetter.getter();
        cards.clear();
        playerCards[0].clear();
        playerCards[1].clear();
        playerPoints[0] = playerPoints[1] = 0;
        gamePoints[0] = gamePoints[1] = 0;
        winsPlayer1.clear();
        winsPlayer2.clear();
        draw.clear();
        players[0] = players[1] = null;

        Main.windowManager.changeCounter(Main.counter1, "0");
        Main.windowManager.changeCounter(Main.counter2, "0");
        Main.windowManager.changeLabel(Main.player1, "none");
        Main.windowManager.changeLabel(Main.player2, "none");
    }

    public void addPlayer1(HolsDerGeierSpieler player1) {
        players[0] = player1;
        player1.register(this, 0);
        Main.windowManager.changeLabel(Main.player1,
                player1.getClass().getAnnotation(GeierInfo.class).name());
        winsPlayer1.clear();
    }

    public void addPlayer2(HolsDerGeierSpieler player2) {
        players[1] = player2;
        player2.register(this, 1);
        Main.windowManager.changeLabel(Main.player2,
                player2.getClass().getAnnotation(GeierInfo.class).name());
        winsPlayer2.clear();
    }

    public int letzterZug(int player) {
        LinkedList<Integer> moves = playerCards[player];
        return !moves.isEmpty() ? moves.getLast() : -99;
    }

    private int getGeierCard() {
        if (cards.isEmpty()) return 0;
        return cards.remove(random.nextInt(cards.size()));
    }

    public void newGame() throws Exception {
        if (cards.isEmpty()) resetGame();
        if (players[0] == null || players[1] == null) {
            throw new Exception("Players are missing!");
        }
        runGame();
    }

    public void newGame(int numberOfGames) throws Exception {
        for (int i = 0; i < numberOfGames; i++) {
            resetGame();
            if (players[0] == null || players[1] == null) {
                throw new Exception("Players are missing!");
            }
            runGame();
        }
    }

    private void runGame() throws Exception {
        int savedPoints = 0;
        timeStart = System.currentTimeMillis();

        while (!cards.isEmpty()) {
            int geierCard = getGeierCard();
            int[] pCards = new int[2];

            // Hole die Karten beider Spieler, bevor sie in die Historie kommen
            pCards[0] = players[0].gibKarte(geierCard);
            pCards[1] = players[1].gibKarte(geierCard);

            // Füge die Karten erst danach zur Historie hinzu
            playerCards[0].add(pCards[0]);
            playerCards[1].add(pCards[1]);

            if (pCards[0] != pCards[1]) {
                boolean negative = geierCard < 0;
                int winner = pCards[0] > pCards[1] ? (negative ? 1 : 0) : (negative ? 0 : 1);
                playerPoints[winner] += (geierCard + savedPoints);
                savedPoints = 0;
            } else {
                savedPoints += geierCard;
            }
        }

        handleGameEnd();
    }

    private void handleGameEnd() {
        int winner = Integer.compare(playerPoints[1], playerPoints[0]);
        long timeEnd = System.currentTimeMillis();

        String outcome = String.format("%s vs %s - %d : %d - %dms\n",
                players[0].getClass().getAnnotation(GeierInfo.class).name(),
                players[1].getClass().getAnnotation(GeierInfo.class).name(),
                playerPoints[0], playerPoints[1], (timeEnd - timeStart));

        if (winner == 0) draw.add(0);
        else if (winner < 0) winsPlayer1.add(1);
        else winsPlayer2.add(2);

        Main.getGameOutcomes().add(outcome);

        Main.windowManager.changeCounter(Main.counter1, String.valueOf(winsPlayer1.size()));
        Main.windowManager.changeCounter(Main.counter2, String.valueOf(winsPlayer2.size()));
        Main.windowManager.refresh();

        if (winner != 0) gamePoints[winner < 0 ? 0 : 1]++;

        int targetGames = Integer.parseInt(Main.numberOfGames.getText());
        if (winsPlayer1.size() + winsPlayer2.size() + draw.size() >= targetGames) {
            handleTournamentResults();
        }
    }

    private void handleTournamentResults() {
        try {
            Class<?> player1Class = players[0].getClass();
            Class<?> player2Class = players[1].getClass();

            int player1Wins = winsPlayer1.size();
            int player2Wins = winsPlayer2.size();

            if (player1Wins > player2Wins) {
                ClassMover.moveToNextRound(player1Class);
                ClassMover.moveToLosers(player2Class);
            } else if (player2Wins > player1Wins) {
                ClassMover.moveToNextRound(player2Class);
                ClassMover.moveToLosers(player1Class);
            } else {
                ClassMover.moveToNextRound(player1Class);
                ClassMover.moveToNextRound(player2Class);
            }

            if (playerBotGetter.isCurrentRoundEmpty()) {
                if (playerBotGetter.isTournamentComplete()) {
                    System.out.println("Tournament completed!");
                } else {
                    System.out.println("Moving to next round");
                }
            }
        } catch (Exception e) {
            System.err.println("Tournament handling failed: " + e.getMessage());
        }
    }
}