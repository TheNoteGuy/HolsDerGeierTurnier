package org.example.neuralHelpers.memory;

import lombok.Getter;
import java.util.*;

public class GameMemory {
    private final Map<Integer, List<GameMove>> moveHistory;
    private final Map<Integer, Double> cardSuccessRates;
    private final StateManager stateManager;
    private static final int HISTORY_SIZE = 1000;

    @Getter
    private static class GameMove {
        private final int ownCard;
        private final int opponentCard;
        private final int geierCard;
        private final boolean won;
        private final int points;

        public GameMove(int ownCard, int opponentCard, int geierCard, boolean won, int points) {
            this.ownCard = ownCard;
            this.opponentCard = opponentCard;
            this.geierCard = geierCard;
            this.won = won;
            this.points = points;
        }
    }

    public GameMemory(StateManager stateManager) {
        this.moveHistory = new HashMap<>();
        this.cardSuccessRates = new HashMap<>();
        this.stateManager = stateManager;
        initializeSuccessRates();
    }

    private void initializeSuccessRates() {
        for (int i = 1; i <= 15; i++) {
            cardSuccessRates.put(i, 0.5); // Start neutral
        }
    }

    public void recordMove(int ownCard, int opponentCard, int geierCard, boolean won, int points) {
        List<GameMove> moves = moveHistory.computeIfAbsent(geierCard, k -> new ArrayList<>());
        moves.add(new GameMove(ownCard, opponentCard, geierCard, won, points));
        
        if (moves.size() > HISTORY_SIZE) {
            moves.remove(0);
        }
        
        updateSuccessRate(ownCard, won);
    }

    private void updateSuccessRate(int card, boolean won) {
        double currentRate = cardSuccessRates.get(card);
        double newRate = currentRate * 0.95 + (won ? 0.05 : 0.0);
        cardSuccessRates.put(card, newRate);
    }

    public int suggestCard(int geierCard) {
        List<Integer> availableCards = stateManager.getAvailableCardsList();
        if (availableCards.isEmpty()) return -1;

        // Analysiere vergangene Züge für diese Geierkarte
        List<GameMove> relevantMoves = moveHistory.getOrDefault(geierCard, new ArrayList<>());
        
        // Wenn keine Historie, wähle Karte basierend auf Erfolgsrate
        if (relevantMoves.isEmpty()) {
            return availableCards.stream()
                .max(Comparator.comparingDouble(card -> cardSuccessRates.get(card)))
                .orElse(availableCards.get(0));
        }

        // Analysiere erfolgreiche Züge
        Map<Integer, Integer> cardWins = new HashMap<>();
        for (GameMove move : relevantMoves) {
            if (move.won) {
                cardWins.merge(move.ownCard, 1, Integer::sum);
            }
        }

        // Wähle beste verfügbare Karte
        return availableCards.stream()
            .max(Comparator.comparingInt(card -> cardWins.getOrDefault(card, 0)))
            .orElse(availableCards.get(0));
    }

    public double getCardSuccessRate(int card) {
        return cardSuccessRates.getOrDefault(card, 0.5);
    }

    public void reset() {
        moveHistory.clear();
        initializeSuccessRates();
    }
}