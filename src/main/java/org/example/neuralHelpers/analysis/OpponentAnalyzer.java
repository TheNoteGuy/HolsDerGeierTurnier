package org.example.neuralHelpers.analysis;

import lombok.Getter;
import java.util.*;

import org.example.neuralHelpers.memory.StateManager;

public class OpponentAnalyzer {
    private final StateManager stateManager;
    private final Map<Integer, List<Integer>> geierToCardMapping;
    private final Map<Integer, Double> cardFrequencies;
    @Getter
    private double aggressionIndex;
    private static final int ANALYSIS_WINDOW = 5;

    public OpponentAnalyzer(StateManager stateManager) {
        this.stateManager = stateManager;
        this.geierToCardMapping = new HashMap<>();
        this.cardFrequencies = new HashMap<>();
        this.aggressionIndex = 0.5;
        initializeFrequencies();
    }

    private void initializeFrequencies() {
        for (int i = 1; i <= 15; i++) {
            cardFrequencies.put(i, 0.0);
        }
    }

    public void analyzeMove(int opponentCard, int geierCard) {
        updateCardFrequency(opponentCard);
        updateGeierMapping(opponentCard, geierCard);
        updateAggressionIndex(opponentCard, geierCard);
    }

    private void updateCardFrequency(int card) {
        cardFrequencies.compute(card, (k, v) -> (v * 0.9) + 0.1);
        cardFrequencies.replaceAll((k, v) -> k == card ? v : v * 0.9);
    }

    private void updateGeierMapping(int card, int geierCard) {
        geierToCardMapping.computeIfAbsent(geierCard, k -> new ArrayList<>()).add(card);
        if (geierToCardMapping.get(geierCard).size() > ANALYSIS_WINDOW) {
            geierToCardMapping.get(geierCard).remove(0);
        }
    }

    private void updateAggressionIndex(int card, int geierCard) {
        boolean isAggressive = (geierCard > 0 && card > 10) || (geierCard < 0 && card < 5);
        aggressionIndex = aggressionIndex * 0.9 + (isAggressive ? 0.1 : 0.0);
    }

    public int predictNextCard(int currentGeierCard) {
        List<Integer> history = geierToCardMapping.get(currentGeierCard);
        if (history == null || history.isEmpty()) {
            return predictBasedOnFrequency();
        }
        return history.get(history.size() - 1);
    }

    private int predictBasedOnFrequency() {
        return cardFrequencies.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(8);
    }

    public boolean isOpponentPredictable() {
        double variance = calculatePlayVariance();
        return variance < 0.3;
    }

    private double calculatePlayVariance() {
        List<Integer> recentPlays = stateManager.getOpponentCards();
        if (recentPlays.size() < ANALYSIS_WINDOW) return 1.0;

        int start = recentPlays.size() - ANALYSIS_WINDOW;
        double mean = recentPlays.subList(start, recentPlays.size()).stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);

        return recentPlays.subList(start, recentPlays.size()).stream()
            .mapToDouble(card -> Math.pow(card - mean, 2))
            .average()
            .orElse(1.0) / 225.0; // Normalisiert durch max mögliche Varianz (15^2)
    }

    public void reset() {
        geierToCardMapping.clear();
        initializeFrequencies();
        aggressionIndex = 0.5;
    }
}