package org.example.neuralHelpers.strategy;

import org.example.neuralHelpers.memory.GameMemory;
import org.example.neuralHelpers.memory.StateManager;
import java.util.List;

public class PassiveStrategy extends BaseStrategy {
    
    public PassiveStrategy(StateManager stateManager, GameMemory gameMemory) {
        super(Stage.PASSIVE, stateManager, gameMemory);
    }

    @Override
    public int selectCard(int currentGeierCard) {
        List<Integer> available = stateManager.getAvailableCardsList();
        if (available.isEmpty()) return -1;

        if (currentGeierCard < 0) {
            // Bei negativen Geierkarten: niedrigste Karte wählen
            return available.stream()
                .min(Integer::compareTo)
                .orElse(available.get(0));
        } else if (currentGeierCard <= 3) {
            // Bei kleinen positiven Werten: mittlere Karte
            return available.stream()
                .min((a, b) -> Math.abs(8 - a) - Math.abs(8 - b))
                .orElse(available.get(0));
        } else {
            // Bei höheren Werten: defensive Strategie
            return available.stream()
                .filter(card -> card <= 10)
                .max(Integer::compareTo)
                .orElse(available.get(0));
        }
    }
}