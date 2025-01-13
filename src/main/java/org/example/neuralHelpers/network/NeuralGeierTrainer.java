package org.example.neuralHelpers.network;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.example.bots.round1.NeuralGeier;
import org.example.framework.HolsDerGeier;
import org.example.framework.HolsDerGeierSpieler;

public class NeuralGeierTrainer {
    private static final String MODEL_PATH = "trained_models/neural_geier_model.zip";
    private final NeuralGeier bot;
    private final NetworkTrainer networkTrainer;
    private final Random random;
    private final int trainingGames;
    private static final int SAVE_INTERVAL = 100;

    private List<HolsDerGeierSpieler> opponents;

    private final HolsDerGeier hdg;

    public NeuralGeierTrainer(int trainingGames, HolsDerGeier hdg) {
        this.bot = new NeuralGeier();
        this.networkTrainer = bot.getTrainer();
        this.trainingGames = trainingGames;
        this.random = new Random();
        this.opponents = new ArrayList<>();
        this.hdg = hdg;
    }

    public void addOpponents(HolsDerGeierSpieler... opponents) {
        for (HolsDerGeierSpieler opponent : opponents) {
            this.opponents.add(opponent);
        }
    }

    private void ensureDirectoryExists() {
        File directory = new File("trained_models");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void startTraining(boolean trainAgainstBots) {

        ensureDirectoryExists();

        if(!trainAgainstBots) {
            for (int game = 0; game < trainingGames; game++) {
                simulateGame();
                if (game % SAVE_INTERVAL == 0) {
                    networkTrainer.saveModel(MODEL_PATH);
                    System.out.printf("Training Progress: %d/%d Games%n", game, trainingGames);
                }
            }
        }else {
            int gamesPerBot = trainingGames / opponents.size();
            for(HolsDerGeierSpieler opponent : opponents) {
                for(int game = 0; game < gamesPerBot; game++) {

                gameAgainstBot(opponent);
                }
            }

        }
        networkTrainer.saveModel(MODEL_PATH);
    }

    private void simulateGame() {
        bot.reset();
        for (int round = 0; round < 15; round++) {
            int geierCard = generateGeierCard();
            int opponentCard = generateOpponentMove();
            double[] gameState = bot.getCurrentState();
            
            int botCard = bot.gibKarte(geierCard);
            double reward = calculateReward(botCard, opponentCard, geierCard);
            
            networkTrainer.trainOnline(gameState, botCard, reward);
        }
    }

    private void gameAgainstBot(HolsDerGeierSpieler opponent) {
        hdg.neueSpieler(bot, opponent);
        bot.reset();
        opponent.reset();
        try {
            hdg.newGame();
            System.out.println(hdg.getWinsPlayer1().size() + " : " + hdg.getWinsPlayer2().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int generateGeierCard() {
        int[] possibleCards = {-5,-4,-3,-2,-1,1,2,3,4,5,6,7,8,9,10};
        return possibleCards[random.nextInt(possibleCards.length)];
    }

    private int generateOpponentMove() {
        return random.nextInt(15) + 1;
    }

    private double calculateReward(int botCard, int opponentCard, int geierCard) {
        boolean win = (geierCard > 0) ? botCard > opponentCard : botCard < opponentCard;
        return win ? Math.abs(geierCard) : -Math.abs(geierCard) * 0.5;
    }
}
