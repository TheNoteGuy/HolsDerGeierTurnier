package org.example.neuralHelpers.strategy;

import lombok.Getter;
import org.example.neuralHelpers.exceptions.StrategyManagementException;
import org.example.neuralHelpers.memory.GameMemory;
import org.example.neuralHelpers.memory.StateManager;
import java.util.*;

public class StrategyManager {
    private final Map<Stage, BaseStrategy> strategies;
    @Getter
    private Stage currentStage;
    private final StateManager stateManager;
    private final GameMemory gameMemory;
    private final Map<Stage, Double> strategyPerformance;
    private int movesInCurrentStage;
    private static final int MOVES_BEFORE_EVALUATION = 3;
    private final Map<Stage, Integer> stageWins;
    private final Map<Stage, Integer> stageMoves;
    private static final int HISTORY_SIZE = 5;
    private final Deque<Stage> stageHistory;

    public StrategyManager(StateManager stateManager, GameMemory gameMemory) {
        this.stateManager = stateManager;
        this.gameMemory = gameMemory;
        this.strategies = new EnumMap<>(Stage.class);
        this.strategyPerformance = new EnumMap<>(Stage.class);
        this.currentStage = Stage.NEUTRAL;
        this.movesInCurrentStage = 0;
        this.stageWins = new EnumMap<>(Stage.class);
        this.stageMoves = new EnumMap<>(Stage.class);
        this.stageHistory = new LinkedList<>();
        initializeStrategies();
        initializeStageStats();
    }

    private void initializeStrategies() {
        for (Stage stage : Stage.values()) {
            strategies.put(stage, createStrategyForStage(stage));
            strategyPerformance.put(stage, 0.0);
        }
    }

    private void initializeStageStats() {
        Arrays.stream(Stage.values()).forEach(stage -> {
            stageWins.put(stage, 0);
            stageMoves.put(stage, 0);
        });
    }

    private BaseStrategy createStrategyForStage(Stage stage) {
        switch (stage) {
            case PASSIVE:
                return new PassiveStrategy(stateManager, gameMemory);
            case AGGRESSIVE:
                return new AggressiveStrategy(stateManager, gameMemory);
            case NEUTRAL:
            default:
                return new BluffStrategy(stateManager, gameMemory);
        }
    }

    public int selectCard(int currentGeierCard) {
        validateGeierCard(currentGeierCard);
        
        try {
            movesInCurrentStage++;
            if (shouldEvaluateStrategy()) {
                evaluateAndUpdateStrategy();
            }
            
            BaseStrategy currentStrategy = getValidStrategy();
            int selectedCard = currentStrategy.selectCard(currentGeierCard);
            
            validateSelectedCard(selectedCard);
            updateStageStats(selectedCard, currentGeierCard);
            return selectedCard;
        } catch (Exception e) {
            logStrategyError("Fehler bei Kartenauswahl", e);
            return selectFallbackCard();
        }
    }

    private void updateStageStats(int selectedCard, int geierCard) {
        stageMoves.merge(currentStage, 1, Integer::sum);
        if (wasLastMoveSuccessful(selectedCard, geierCard)) {
            stageWins.merge(currentStage, 1, Integer::sum);
        }
    }

    private boolean wasLastMoveSuccessful(int selectedCard, int geierCard) {
        int lastOpponentCard = stateManager.getOpponentCards().isEmpty() ? -1 : 
            stateManager.getOpponentCards().get(stateManager.getOpponentCards().size() - 1);
        return lastOpponentCard != -1 && isWinningMove(selectedCard, lastOpponentCard, geierCard);
    }

    private BaseStrategy getValidStrategy() {
        BaseStrategy strategy = strategies.get(currentStage);
        if (strategy == null) {
            throw new StrategyManagementException(
                String.format("Keine Strategie für Stage %s gefunden", currentStage)
            );
        }
        return strategy;
    }

    private void validateGeierCard(int card) {
        if (card < -5 || card > 10 || card == 0) {
            throw new IllegalArgumentException(
                String.format("Ungültige Geierkarte: %d", card)
            );
        }
    }

    private void validateSelectedCard(int card) {
        if (card < 1 || card > 15) {
            throw new StrategyManagementException(
                String.format("Ungültige Kartenauswahl: %d", card)
            );
        }
        if (!stateManager.isCardAvailable(card)) {
            throw new StrategyManagementException(
                String.format("Karte %d nicht verfügbar", card)
            );
        }
    }

    private int selectFallbackCard() {
        List<Integer> available = stateManager.getAvailableCardsList();
        return available.isEmpty() ? -1 : available.get(0);
    }

    private void logStrategyError(String message, Exception e) {
        System.err.println(String.format("[StrategyManager] %s: %s", message, e.getMessage()));
    }

    private boolean shouldEvaluateStrategy() {
        return movesInCurrentStage >= MOVES_BEFORE_EVALUATION;
    }

    private void evaluateAndUpdateStrategy() {
        Stage bestStage = findBestPerformingStage();
        
        if (shouldSwitchStrategy(bestStage)) {
            switchToStrategy(bestStage);
        }
    }

    private Stage findBestPerformingStage() {
        return Arrays.stream(Stage.values())
            .max(Comparator.comparingDouble(this::calculateStagePerformance))
            .orElse(Stage.NEUTRAL);
    }

    private double calculateStagePerformance(Stage stage) {
        int moves = stageMoves.get(stage);
        if (moves == 0) return 0.5;
        return (double) stageWins.get(stage) / moves;
    }

    private boolean shouldSwitchStrategy(Stage newStage) {
        if (currentStage == newStage) return false;
        if (stageHistory.size() >= HISTORY_SIZE) {
            stageHistory.pollFirst();
        }
        return !stageHistory.contains(newStage);
    }

    private void switchToStrategy(Stage newStage) {
        currentStage = newStage;
        stageHistory.addLast(newStage);
        movesInCurrentStage = 0;
    }

    private boolean isWinningMove(int ownCard, int opponentCard, int geierCard) {
        if (geierCard > 0) {
            return ownCard > opponentCard;
        } else {
            return ownCard < opponentCard;
        }
    }

    public void reset() {
        currentStage = Stage.NEUTRAL;
        movesInCurrentStage = 0;
        strategyPerformance.replaceAll((k, v) -> 0.0);
        stageWins.replaceAll((k, v) -> 0);
        stageMoves.replaceAll((k, v) -> 0);
        stageHistory.clear();
    }
}