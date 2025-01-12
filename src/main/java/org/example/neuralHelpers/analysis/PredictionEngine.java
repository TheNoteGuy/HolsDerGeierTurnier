package org.example.neuralHelpers.analysis;

import lombok.Getter;
import org.example.neuralHelpers.memory.StateManager;
import java.util.*;

public class PredictionEngine {
    private final OpponentAnalyzer opponentAnalyzer;
    private final PatternRecognition patternRecognition;
    private final StateManager stateManager;
    private final Map<Integer, Double> predictionAccuracy;
    
    @Getter
    private double overallConfidence;

    public PredictionEngine(StateManager stateManager) {
        if (stateManager == null) {
            throw new IllegalArgumentException("StateManager darf nicht null sein");
        }
        this.stateManager = stateManager;
        this.opponentAnalyzer = new OpponentAnalyzer(stateManager);
        this.patternRecognition = new PatternRecognition(stateManager);
        this.predictionAccuracy = new HashMap<>();
        this.overallConfidence = 0.5;
    }

    public int predictNextMove(int currentGeierCard) {
        if (currentGeierCard < -5 || currentGeierCard > 10) {
            throw new IllegalArgumentException("Ungültige Geierkarte: " + currentGeierCard);
        }

        try {
            int patternPrediction = patternRecognition.predictNextMove(currentGeierCard);
            int analyzerPrediction = opponentAnalyzer.predictNextCard(currentGeierCard);
            
            if (patternPrediction == -1 && analyzerPrediction == -1) {
                return getDefaultPrediction();
            }

            return combineAndWeightPredictions(
                patternPrediction, 
                analyzerPrediction
            );
        } catch (Exception e) {
            logError("Fehler bei der Vorhersage", e);
            return getDefaultPrediction();
        }
    }

    private int getDefaultPrediction() {
        return 8; // Sichere Mittelwert-Vorhersage
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
    }


    private int combineAndWeightPredictions(int patternPred, int analyzerPred) {
        if (patternPred == -1) return analyzerPred;
        if (analyzerPred == -1) return patternPred;
        
        double patternConf = patternRecognition.getPatternStrength();
        double analyzerConf = opponentAnalyzer.isOpponentPredictable() ? 0.7 : 0.3;
        
        overallConfidence = Math.max(patternConf, analyzerConf);
        
        return patternConf > analyzerConf ? patternPred : analyzerPred;
    }

    public void updatePredictions(int actualCard, int predictedCard) {
        double accuracy = predictionAccuracy.getOrDefault(predictedCard, 0.5);
        boolean wasCorrect = (actualCard == predictedCard);
        
        accuracy = accuracy * 0.9 + (wasCorrect ? 0.1 : 0.0);
        predictionAccuracy.put(predictedCard, accuracy);
    }

    public void processMove(int opponentCard, int geierCard) {
        opponentAnalyzer.analyzeMove(opponentCard, geierCard);
        patternRecognition.analyzeMoves(opponentCard, geierCard);
    }

    public Map<Integer, Double> getPredictionProbabilities(int currentGeierCard) {
        Map<Integer, Double> probabilities = new HashMap<>();
        List<Integer> availableCards = stateManager.getAvailableCardsList();
        
        for (int card : availableCards) {
            double probability = calculateProbability(card, currentGeierCard);
            probabilities.put(card, probability);
        }
        
        return probabilities;
    }

    private double calculateProbability(int card, int geierCard) {
        double baseProb = predictionAccuracy.getOrDefault(card, 0.5);
        double patternInfluence = patternRecognition.getPatternStrength();
        double analyzerInfluence = opponentAnalyzer.getAggressionIndex();
        
        return (baseProb * 0.4) + (patternInfluence * 0.3) + (analyzerInfluence * 0.3);
    }

    public void reset() {
        opponentAnalyzer.reset();
        patternRecognition.reset();
        predictionAccuracy.clear();
        overallConfidence = 0.5;
    }
}