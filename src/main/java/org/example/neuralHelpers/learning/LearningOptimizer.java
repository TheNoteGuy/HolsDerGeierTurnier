package org.example.neuralHelpers.learning;

import lombok.Getter;
import org.example.neuralHelpers.network.FastNeuralNetwork;
import org.example.neuralHelpers.network.NetworkConfig;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class LearningOptimizer {
    private final FastNeuralNetwork network;
    private final ExperienceBuffer experienceBuffer;
    private final NetworkConfig config;
    
    @Getter
    private double currentLearningRate;
    private static final int BATCH_SIZE = 32;
    private static final double MIN_LEARNING_RATE = 0.0001;
    private static final double LEARNING_RATE_DECAY = 0.995;

    public LearningOptimizer(FastNeuralNetwork network) {
        this.network = network;
        this.experienceBuffer = new ExperienceBuffer(1000);
        this.config = NetworkConfig.getInstance();
        this.currentLearningRate = config.getLearningRate();
    }

    public void optimize(INDArray currentState, int action, double reward, INDArray nextState) {
        experienceBuffer.addExperience(currentState, action, reward, nextState);
        
        if (experienceBuffer.size() >= BATCH_SIZE) {
            trainOnBatch();
        }
    }

    private void trainOnBatch() {
        var batch = experienceBuffer.sampleBatch(BATCH_SIZE);
        
        INDArray states = Nd4j.create(BATCH_SIZE, NetworkConfig.getInputFeatures());
        INDArray targets = Nd4j.create(BATCH_SIZE, NetworkConfig.getOutputFeatures());

        for (int i = 0; i < batch.size(); i++) {
            var experience = batch.get(i);
            states.putRow(i, experience.getState());
            targets.putRow(i, calculateTarget(experience));
        }

        network.getNetwork().fit(states, targets);
        updateLearningRate();
    }

    private INDArray calculateTarget(ExperienceBuffer.Experience exp) {
        INDArray target = Nd4j.zeros(NetworkConfig.getOutputFeatures());
        target.putScalar(exp.getAction() - 1, exp.getReward());
        return target;
    }

    private void updateLearningRate() {
        currentLearningRate = Math.max(
            MIN_LEARNING_RATE,
            currentLearningRate * LEARNING_RATE_DECAY
        );
    }

    public void reset() {
        experienceBuffer.clear();
        currentLearningRate = config.getLearningRate();
    }
}