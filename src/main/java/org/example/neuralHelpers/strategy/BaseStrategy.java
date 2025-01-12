package org.example.neuralHelpers.strategy;

import org.example.neuralHelpers.memory.GameMemory;
import org.example.neuralHelpers.memory.StateManager;

import lombok.Getter;


public abstract class BaseStrategy {
    @Getter
    protected final Stage currentStage;
    protected final StateManager stateManager;
    protected final GameMemory gameMemory;
    
    public BaseStrategy(Stage stage, StateManager stateManager, GameMemory gameMemory) {
        this.currentStage = stage;
        this.stateManager = stateManager;
        this.gameMemory = gameMemory;
    }

    // Hauptmethode für Kartenauswahl
    public abstract int selectCard(int currentGeierCard);

    // Bewertung einer Karte im aktuellen Kontext
    protected double evaluateCard(int card, int geierCard) {
        double baseValue = calculateBaseValue(card, geierCard);
        double riskAdjustedValue = adjustForRisk(baseValue);
        double successRateValue = gameMemory.getCardSuccessRate(card);
        
        return (baseValue * 0.4) + (riskAdjustedValue * 0.3) + (successRateValue * 0.3);
    }

    // Basiswert einer Karte berechnen
    protected double calculateBaseValue(int card, int geierCard) {
        if (geierCard > 0) {
            return 1.0 - (Math.abs(card - geierCard) / 15.0);
        } else {
            return (double) card / 15.0;
        }
    }

    // Risikoanpassung basierend auf Stage
    protected double adjustForRisk(double baseValue) {
        return baseValue * currentStage.getRiskFactor();
    }

    // Prüfen ob Bluff sinnvoll ist
    protected boolean shouldBluff() {
        return Math.random() < currentStage.getBluffProbability();
    }

    // Strategiewechsel möglich?
    protected boolean shouldSwitchStrategy() {
        int roundsPlayed = stateManager.getPlayedCards().size();
        return roundsPlayed > 0 && roundsPlayed % 5 == 0;
    }
}