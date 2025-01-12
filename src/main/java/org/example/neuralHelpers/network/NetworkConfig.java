package org.example.neuralHelpers.network;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NetworkConfig {
    // Netzwerk-Struktur
    private static final int INPUT_FEATURES = 31;  // 15 eigene + 15 gegner + 1 geierkarte
    private static final int HIDDEN_LAYER_1 = 64;
    private static final int HIDDEN_LAYER_2 = 32;
    private static final int OUTPUT_FEATURES = 15;  // Mögliche Karten 1-15

    // Training Parameter
    @Builder.Default private double learningRate = 0.001;
    @Builder.Default private double dropoutRate = 0.2;
    @Builder.Default private int batchSize = 32;

    // Strategie Parameter
    @Builder.Default private double aggressiveThreshold = 0.7;
    @Builder.Default private double passiveThreshold = 0.3;
    @Builder.Default private double bluffProbability = 0.15;

    // Performance
    @Builder.Default private boolean useGPU = false;
    @Builder.Default private int inferenceTimeout = 100; // ms

    // Singleton Instance
    private static NetworkConfig instance;

    public static NetworkConfig getInstance() {
        if (instance == null) {
            instance = NetworkConfig.builder().build();
        }
        return instance;
    }

    public static void updateConfig(NetworkConfig newConfig) {
        instance = newConfig;
    }

    // Getter für konstante Werte
    public static int getInputFeatures() {
        return INPUT_FEATURES;
    }

    public static int getHiddenLayer1() {
        return HIDDEN_LAYER_1;
    }

    public static int getHiddenLayer2() {
        return HIDDEN_LAYER_2;
    }

    public static int getOutputFeatures() {
        return OUTPUT_FEATURES;
    }
}