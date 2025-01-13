package org.example.neuralHelpers.network;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.example.neuralHelpers.exceptions.NetworkExecutionException;
import org.example.neuralHelpers.exceptions.NetworkInitializationException;
import org.example.neuralHelpers.exceptions.PredictionTimeoutException;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.memory.conf.WorkspaceConfiguration;
import org.nd4j.linalg.api.memory.enums.SpillPolicy;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class FastNeuralNetwork {
    @Getter
    private final MultiLayerNetwork network;
    private final NetworkConfig config;
    private final Map<String, CacheEntry> predictionCache;
    private static final int CACHE_SIZE = 100;
    private static final long CACHE_EXPIRY = 1000;
    private static final String WORKSPACE_NAME = "NEURAL_WORKSPACE";
    private boolean isInitialized = false;
    private static final int INPUT_SIZE = NetworkConfig.getInputFeatures();

    private static class CacheEntry {
        final int prediction;
        final long timestamp;
        final double confidence;

        CacheEntry(int prediction, double confidence) {
            this.prediction = prediction;
            this.timestamp = System.currentTimeMillis();
            this.confidence = confidence;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY;
        }
    }


    public FastNeuralNetwork() {
        this.config = NetworkConfig.getInstance();
        this.predictionCache = new ConcurrentHashMap<>(CACHE_SIZE);
        initializeWorkspace();
        this.network = initializeNetwork();
    }


    private String generateCacheKey(INDArray input) {
        return String.valueOf(input.hashCode());
    }

    private void initializeWorkspace() {
        if (!isInitialized) {
            Nd4j.getWorkspaceManager().createNewWorkspace(
                WorkspaceConfiguration.builder()
                    .initialSize(10 * 1024L * 1024L)
                    .maxSize(100 * 1024L * 1024L)
                    .overallocationLimit(0.3)
                    .policySpill(SpillPolicy.EXTERNAL)
                    .build());
            isInitialized = true;
        }
    }

    private MultiLayerNetwork initializeNetwork() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(config.getLearningRate()))
                .trainingWorkspaceMode(WorkspaceMode.ENABLED)
                .inferenceWorkspaceMode(WorkspaceMode.ENABLED)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(NetworkConfig.getInputFeatures())
                        .nOut(NetworkConfig.getHiddenLayer1())
                        .activation(Activation.RELU)
                        .dropOut(config.getDropoutRate())
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(NetworkConfig.getHiddenLayer1())
                        .nOut(NetworkConfig.getHiddenLayer2())
                        .activation(Activation.RELU)
                        .dropOut(config.getDropoutRate())
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(NetworkConfig.getHiddenLayer2())
                        .nOut(NetworkConfig.getOutputFeatures())
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        return net;
    }

    public int predict(INDArray input) {
        validateInput(input);
        
        if (!isInitialized) {
            throw new NetworkInitializationException("Netzwerk nicht initialisiert");
        }

        String cacheKey = generateCacheKey(input);
        try {
            CacheEntry cached = predictionCache.get(cacheKey);
            if (isValidCacheEntry(cached)) {
                return cached.prediction;
            }

            return executePrediction(input, cacheKey);
        } catch (Exception e) {
            logError("Vorhersagefehler", e);
            return handlePredictionError();
        }
    }

    private void validateInput(INDArray input) {
        if (input == null) {
            throw new IllegalArgumentException("Input darf nicht null sein");
        }
        if (input.length() != INPUT_SIZE) {
            throw new IllegalArgumentException(
                String.format("Ungültige Input-Dimension: %d (erwartet: %d)", 
                input.length(), INPUT_SIZE)
            );
        }
    }

    private boolean isValidCacheEntry(CacheEntry entry) {
        return entry != null && !entry.isExpired() && entry.confidence > 0.8;
    }

    private int executePrediction(INDArray input, String cacheKey) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Reshape input to matrix format [1,31]
            INDArray reshapedInput = input.reshape(1, input.length());
            INDArray output = network.output(reshapedInput, false);
            
            if (System.currentTimeMillis() - startTime > config.getInferenceTimeout()) {
                throw new PredictionTimeoutException("Zeitüberschreitung bei Vorhersage");
            }
            
            double confidence = output.maxNumber().doubleValue();
            int prediction = Nd4j.argMax(output, 1).getInt(0) + 1;
            
            updateCache(cacheKey, prediction, confidence);
            return prediction;
        } catch (Exception e) {
            throw new NetworkExecutionException("Fehler bei Netzwerk-Ausführung", e);
        }
    }

    private int handlePredictionError() {
        return 8; // Sichere Standardvorhersage
    }

    private void updateCache(String key, int prediction, double confidence) {
        if (predictionCache.size() >= CACHE_SIZE) {
            cleanupCache();
        }
        predictionCache.put(key, new CacheEntry(prediction, confidence));
    }

    private void cleanupCache() {
        predictionCache.entrySet().removeIf(e -> e.getValue().isExpired());
        if (predictionCache.size() >= CACHE_SIZE) {
            // Wenn noch immer zu voll, entferne Einträge mit niedrigster Konfidenz
            predictionCache.entrySet().stream()
                .sorted((a, b) -> Double.compare(a.getValue().confidence, b.getValue().confidence))
                .limit(CACHE_SIZE / 4)
                .forEach(e -> predictionCache.remove(e.getKey()));
        }
    }

    public void dispose() {
        if (isInitialized) {
            MemoryWorkspace workspace = Nd4j.getWorkspaceManager().getWorkspaceForCurrentThread(WORKSPACE_NAME);
            if (workspace != null) {
                workspace.close();
            }
            isInitialized = false;
        }
        predictionCache.clear();
        System.gc();
    }


    public void storeInMemory() {
        Nd4j.getMemoryManager().setAutoGcWindow(10000);
        System.gc();
    }

    public void prepareForInference() {
        if (config.isUseGPU()) {
            Nd4j.getAffinityManager().allowCrossDeviceAccess(false);
        }
    }


    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
    }
    
}