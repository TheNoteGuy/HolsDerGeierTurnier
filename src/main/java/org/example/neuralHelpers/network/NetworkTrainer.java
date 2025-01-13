package org.example.neuralHelpers.network;

import lombok.RequiredArgsConstructor;

import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.File;
import java.util.*;

@RequiredArgsConstructor
public class NetworkTrainer {
    private final FastNeuralNetwork network;
    private final NetworkConfig config;
    private List<DataSet> trainingData;
    private double currentLoss;

    public void initializeTraining() {
        trainingData = new ArrayList<>();
    }

    public void addTrainingExample(double[] gameState, int playedCard) {
        INDArray input = Nd4j.create(gameState);
        INDArray output = Nd4j.zeros(1, NetworkConfig.getOutputFeatures());
        output.putScalar(0, playedCard - 1, 1.0);
        
        trainingData.add(new DataSet(input, output));
    }

    public void train() {
        if (trainingData.isEmpty()) return;

        Collections.shuffle(trainingData);
        DataSetIterator iterator = new ListDataSetIterator<>(trainingData, config.getBatchSize());

        while (iterator.hasNext()) {
            DataSet batch = iterator.next();
            currentLoss = network.getNetwork().score(batch);
            network.getNetwork().fit(batch);
        }
    }

    public void trainOnline(double[] state, int action, double reward) {
        // Input muss reshapet werden zu [1, 31]
        INDArray input = Nd4j.create(state).reshape(1, state.length);
        INDArray output = Nd4j.zeros(1, NetworkConfig.getOutputFeatures());
        output.putScalar(0, action - 1, reward);
        
        network.getNetwork().fit(input, output);
    }

    public double getCurrentLoss() {
        return currentLoss;
    }

    public void clearTrainingData() {
        trainingData.clear();
        System.gc();
    }

    public void saveModel(String filepath) {
        try {
            File modelFile = new File(filepath);
            File directory = modelFile.getParentFile();
            
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            network.getNetwork().save(modelFile);
        } catch (Exception e) {
            System.err.println("Fehler beim Speichern des Modells: " + e.getMessage());
        }
    }

    public void loadModel(String filepath) {
        try {
            // Korrektur: boolean Parameter hinzugefügt (true = load updater state)
            network.getNetwork().load(new java.io.File(filepath), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}