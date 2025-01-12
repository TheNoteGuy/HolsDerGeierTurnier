package org.example.neuralHelpers.learning;

import lombok.Getter;

public class RewardSystem {
    private static final double WIN_REWARD = 1.0;
    private static final double LOSS_PENALTY = -0.5;
    private static final double DRAW_REWARD = 0.1;
    
    @Getter
    private double totalReward;

    public RewardSystem() {
        this.totalReward = 0.0;
    }

    public double calculateReward(int ownCard, int opponentCard, int geierCard) {
        double reward = 0.0;

        if (geierCard > 0) {
            if (ownCard > opponentCard) reward = WIN_REWARD * Math.abs(geierCard);
            else if (ownCard < opponentCard) reward = LOSS_PENALTY * Math.abs(geierCard);
            else reward = DRAW_REWARD;
        } else {
            if (ownCard < opponentCard) reward = WIN_REWARD * Math.abs(geierCard);
            else if (ownCard > opponentCard) reward = LOSS_PENALTY * Math.abs(geierCard);
            else reward = DRAW_REWARD;
        }

        totalReward += reward;
        return reward;
    }

    public void reset() {
        totalReward = 0.0;
    }
}