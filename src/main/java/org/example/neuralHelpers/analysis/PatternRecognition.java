package org.example.neuralHelpers.analysis;

import lombok.Getter;
import org.example.neuralHelpers.memory.StateManager;
import java.util.*;

public class PatternRecognition {
    private final StateManager stateManager;
    private final Map<List<Integer>, Integer> patterns;
    private final int[] lastMoves;
    private static final int PATTERN_LENGTH = 3;
    private static final int MAX_PATTERNS = 1000;

    @Getter
    private final Map<String, Double> patternStats;

    public PatternRecognition(StateManager stateManager) {
        this.stateManager = stateManager;
        this.patterns = new LinkedHashMap<List<Integer>, Integer>(MAX_PATTERNS, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_PATTERNS;
            }
        };
        this.lastMoves = new int[PATTERN_LENGTH];
        this.patternStats = new HashMap<>();
    }

    public void analyzeMoves(int opponentCard, int geierCard) {
        shiftAndAddMove(opponentCard);
        updatePatterns();
        updateStats(opponentCard, geierCard);
    }

    private void shiftAndAddMove(int newMove) {
        System.arraycopy(lastMoves, 1, lastMoves, 0, PATTERN_LENGTH - 1);
        lastMoves[PATTERN_LENGTH - 1] = newMove;
    }

    private void updatePatterns() {
        if (isValidSequence()) {
            List<Integer> currentPattern = getCurrentPattern();
            patterns.merge(currentPattern, 1, Integer::sum);
        }
    }

    private boolean isValidSequence() {
        return lastMoves[PATTERN_LENGTH - 1] != 0;
    }

    private List<Integer> getCurrentPattern() {
        List<Integer> pattern = new ArrayList<>(PATTERN_LENGTH);
        for (int move : lastMoves) {
            pattern.add(move);
        }
        return pattern;
    }

    private void updateStats(int card, int geierCard) {
        String key = String.format("card_%d_geier_%d", card, geierCard);
        double oldValue = patternStats.getOrDefault(key, 0.0);
        patternStats.put(key, oldValue * 0.9 + 0.1);
    }

    public int predictNextMove(int currentGeierCard) {
        if (!isValidSequence()) return -1;

        List<Integer> currentSeq = getCurrentPattern().subList(1, PATTERN_LENGTH);
        Map<Integer, Integer> predictions = new HashMap<>();

        patterns.forEach((pattern, frequency) -> {
            if (pattern.subList(0, PATTERN_LENGTH - 1).equals(currentSeq)) {
                predictions.merge(pattern.get(PATTERN_LENGTH - 1), frequency, Integer::sum);
            }
        });

        return predictions.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(-1);
    }

    public double getPatternStrength() {
        if (patterns.isEmpty()) return 0.0;

        int totalPatterns = patterns.values().stream().mapToInt(Integer::intValue).sum();
        int uniquePatterns = patterns.size();

        return (double) totalPatterns / uniquePatterns / PATTERN_LENGTH;
    }

    public void reset() {
        patterns.clear();
        Arrays.fill(lastMoves, 0);
        patternStats.clear();
    }
}