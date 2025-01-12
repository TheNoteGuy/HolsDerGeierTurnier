package org.example.bots.round1;

import org.example.neuralHelpers.network.*;
import org.example.neuralHelpers.strategy.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import lombok.Getter;

import org.example.neuralHelpers.analysis.*;
import org.example.neuralHelpers.learning.LearningOptimizer;
import org.example.neuralHelpers.learning.RewardSystem;
import org.example.neuralHelpers.memory.*;

import java.io.File;
import java.util.List;

import org.example.framework.*;

@GeierInfo(name = "NeuralGeier")
public class NeuralGeier extends HolsDerGeierSpieler {
    private final FastNeuralNetwork network;
    private final StateManager stateManager;
    private final GameMemory gameMemory;
    private final StrategyManager strategyManager;
    private final PredictionEngine predictionEngine;
    private final RewardSystem rewardSystem;
    private final LearningOptimizer learningOptimizer;
    @Getter
    private final NetworkTrainer trainer;

    private INDArray lastState;
    private int lastPlayedCard;

    public NeuralGeier() {
        this.network = new FastNeuralNetwork();
        this.stateManager = new StateManager();
        this.gameMemory = new GameMemory(stateManager);
        this.strategyManager = new StrategyManager(stateManager, gameMemory);
        this.predictionEngine = new PredictionEngine(stateManager);
        this.lastPlayedCard = -1;
        this.rewardSystem = new RewardSystem();  
        this.learningOptimizer = new LearningOptimizer(network);
        this.lastState = null;
        this.trainer = new NetworkTrainer(network, NetworkConfig.getInstance());

        try {
            File modelFile = new File("trained_models/neural_geier_model.zip");
            if (modelFile.exists()) {
                network.getNetwork().load(modelFile, true);
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden des Modells: " + e.getMessage());
        }
    }

    public double[] getCurrentState() {
        return stateManager.getStateVector();
    }

    @Override
    public void reset() {
        stateManager.reset();
        gameMemory.reset();
        strategyManager.reset();
        predictionEngine.reset();
        lastPlayedCard = -1;
        rewardSystem.reset();
        learningOptimizer.reset();
        lastState = null;
    }

    @Override
    public int gibKarte(int naechsteKarte) {
        try {
            return executeMove(naechsteKarte);
        } catch (Exception e) {
            System.err.println("Fehler in gibKarte: " + e.getMessage());
            return fallbackMove();
        }
    }

    private int executeMove(int naechsteKarte) {
        updateLastMove(naechsteKarte);
        
        INDArray currentState = prepareState(naechsteKarte);
        int predictedMove = predictionEngine.predictNextMove(naechsteKarte);
        int strategicChoice = strategyManager.selectCard(naechsteKarte);
        int networkChoice = network.predict(currentState);
        
        int finalChoice = combinedDecision(strategicChoice, networkChoice, predictedMove, naechsteKarte);
        finalChoice = validateCard(finalChoice);
        
        // Learning
        if (lastState != null) {
            double reward = rewardSystem.calculateReward(lastPlayedCard, super.letzterZug(), naechsteKarte);
            learningOptimizer.optimize(lastState, lastPlayedCard, reward, currentState);
        }
        
        lastState = currentState;
        lastPlayedCard = finalChoice;
        return finalChoice;
    }

        private INDArray prepareState(int naechsteKarte) {
        double[] state = stateManager.getStateVector();
        return Nd4j.create(state);
    }

    private void updateLastMove(int naechsteKarte) {
        if (lastPlayedCard != -1) {
            int lastOpponentCard = super.letzterZug();
            if (lastOpponentCard != -1) {
                updateGameState(lastPlayedCard, lastOpponentCard, naechsteKarte);
            }
        }
    }

        private int fallbackMove() {
        List<Integer> available = stateManager.getAvailableCardsList();
        return available.isEmpty() ? 8 : available.get(0);
    }

    private void updateGameState(int ownCard, int opponentCard, int geierCard) {
        stateManager.recordMove(ownCard, opponentCard, geierCard);
        gameMemory.recordMove(ownCard, opponentCard, geierCard, 
            isWinningMove(ownCard, opponentCard, geierCard),
            calculatePoints(ownCard, opponentCard, geierCard));
        predictionEngine.processMove(opponentCard, geierCard);
    }

    private int combinedDecision(int strategic, int network, int predicted, int geierCard) {
        double networkConfidence = predictionEngine.getOverallConfidence();
        
        if (networkConfidence > 0.8) return network;
        if (shouldCounterPredict(predicted, geierCard)) return findCounterCard(predicted, geierCard);
        
        return strategic;
    }

    private boolean shouldCounterPredict(int predictedCard, int geierCard) {
        return predictionEngine.getOverallConfidence() > 0.7 && 
               stateManager.isCardAvailable(findCounterCard(predictedCard, geierCard));
    }

    private int findCounterCard(int opponentCard, int geierCard) {
        if (geierCard > 0) {
            return Math.min(15, opponentCard + 1);
        } else {
            return Math.max(1, opponentCard - 1);
        }
    }

    private boolean isWinningMove(int own, int opponent, int geier) {
        if (geier > 0) return own > opponent;
        return own < opponent;
    }

    private int calculatePoints(int own, int opponent, int geier) {
        if (isWinningMove(own, opponent, geier)) return Math.abs(geier);
        return 0;
    }

    private int validateCard(int card) {
        if (!stateManager.isCardAvailable(card)) {
            return stateManager.getAvailableCardsList().get(0);
        }
        return card;
    }
}