package org.example.neuralHelpers.learning;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nd4j.linalg.api.ndarray.INDArray;
import java.util.*;

public class ExperienceBuffer {
    private final Queue<Experience> buffer;
    private final int maxSize;
    private final Random random;

    @AllArgsConstructor
    @Getter
    public static class Experience {
        private final INDArray state;
        private final int action;
        private final double reward;
        private final INDArray nextState;
    }

    public ExperienceBuffer(int maxSize) {
        this.maxSize = maxSize;
        this.buffer = new LinkedList<>();
        this.random = new Random();
    }

    public void addExperience(INDArray state, int action, double reward, INDArray nextState) {
        Experience experience = new Experience(state, action, reward, nextState);
        
        if (buffer.size() >= maxSize) {
            buffer.poll();
        }
        buffer.offer(experience);
    }

    public List<Experience> sampleBatch(int batchSize) {
        batchSize = Math.min(batchSize, buffer.size());
        List<Experience> experiences = new ArrayList<>(buffer);
        Collections.shuffle(experiences, random);
        return experiences.subList(0, batchSize);
    }

    public int size() {
        return buffer.size();
    }

    public void clear() {
        buffer.clear();
    }
}