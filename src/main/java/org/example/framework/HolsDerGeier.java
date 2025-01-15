package org.example.framework;

import lombok.Getter;
import org.example.ClassMover;
import org.example.Main;
import org.example.PlayerBotGetter;
import java.security.SecureRandom;
import java.util.*;
import javax.swing.JOptionPane;
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
            checkForCheating();

            int geierCard = getGeierCard();
            int[] pCards = new int[2];

            for (int i = 0; i < 2; i++) {
                try {
                    int toPlay = players[i].gibKarte(geierCard);
                    try {
                        validateCard(i, toPlay);
                    } catch (Exception e) {
                        // Zeige detailliertere Fehlermeldung für ungültige Karten
                        String playerName = players[i].getClass().getAnnotation(GeierInfo.class).name();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null,
                                    "Invalid card played by " + playerName + "!\n" +
                                            "Card value: " + toPlay + "\n" +
                                            "Valid range is 1-15\n" +
                                            "This might be a programming error rather than cheating.",
                                    "Invalid Move",
                                    JOptionPane.WARNING_MESSAGE);
                        });
                        fullReset();
                        throw e;
                    }
                    playerCards[i].add(toPlay);
                    pCards[i] = toPlay;
                } catch (Exception e) {
                    handleCheatingAttempt("Attempt to manipulate game state by player " + (i+1));
                    return;
                }
            }

            if (pCards[0] != pCards[1]) {
                boolean negative = geierCard < 0;
                int winner = pCards[0] > pCards[1] ? (negative ? 1 : 0) : (negative ? 0 : 1);
                playerPoints[winner] += (geierCard + savedPoints);
                savedPoints = 0;
            } else {
                savedPoints = geierCard;
            }
        }

        handleGameEnd();
    }

    private void checkForCheating() {
        // Überprüfe auf unrealistische Punktzahlen
        for (int points : playerPoints) {
            if (Math.abs(points) > 100) {
                handleCheatingAttempt("Unrealistic score detected");
            }
        }

        // Überprüfe auf doppelte Karten pro Spieler
        for (int i = 0; i < 2; i++) {
            Set<Integer> playerUsedCards = new HashSet<>();
            for (Integer card : playerCards[i]) {
                // Prüfe auf null oder ungültige Kartenwerte
                if (card == null) {
                    handleCheatingAttempt("Player " + (i+1) + " played null card");
                }

                // Prüfe auf Karten außerhalb des erlaubten Bereichs
                if (card < MIN_CARD || card > MAX_CARD) {
                    handleCheatingAttempt("Player " + (i+1) + " played invalid card value: " + card);
                }

                // Prüfe auf Duplikate
                if (!playerUsedCards.add(card)) {
                    handleCheatingAttempt("Player " + (i+1) + " used duplicate card: " + card);
                }
            }

            // Prüfe auf verdächtige Kartenfolgen
            LinkedList<Integer> playerMoves = playerCards[i];
            if (playerMoves.size() >= 2) {
                int lastMove = playerMoves.getLast();
                int previousMove = playerMoves.get(playerMoves.size() - 2);
                if (lastMove == previousMove) {
                    handleCheatingAttempt("Player " + (i+1) + " played same card twice in a row: " + lastMove);
                }
            }
        }

        // Prüfe auf Reflection oder andere verdächtige Stacktrace-Elemente
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("reflect")) {
                handleCheatingAttempt("Player attempted to use reflection");
            }
        }
    }

    private void handleCheatingAttempt(String reason) {
        // Identifiziere den Cheater (Spieler der zuletzt gezogen hat)
        HolsDerGeierSpieler cheater = players[playerCards[0].size() > playerCards[1].size() ? 0 : 1];
        String cheaterName = cheater.getClass().getAnnotation(GeierInfo.class).name();

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "CHEATER DETECTED!\nPlayer: " + cheaterName + "\nReason: " + reason,
                    "Security Alert",
                    JOptionPane.ERROR_MESSAGE);
        });

        // Verschiebe den Cheater in den Losers-Ordner
        try {
            ClassMover.moveToLosers(cheater.getClass());
            System.out.println("Moved cheater " + cheaterName + " to losers folder");
        } catch (Exception e) {
            System.err.println("Failed to move cheater to losers: " + e.getMessage());
        }

        fullReset();
        throw new SecurityException("Cheating attempt detected: " + reason + " by " + cheaterName);
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
            int currentRound = playerBotGetter.getRound();

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
                    System.out.println("Tournament completed! Final round: " + currentRound);
                } else {
                    System.out.println("Moving to round " + (currentRound + 1));
                }
            }
        } catch (Exception e) {
            System.err.println("Tournament handling failed: " + e.getMessage());
        }
    }

    private void validateCard(int player, int card) throws Exception {
        if (playerCards[player].contains(card) || card < MIN_CARD || card > MAX_CARD) {
            throw new Exception("Invalid card: " + card +
                    "\nThe card has already been played or is outside the valid range [" +
                    MIN_CARD + "-" + MAX_CARD + "]");
        }
    }
}
