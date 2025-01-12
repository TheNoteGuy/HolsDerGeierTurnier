package org.example.neuralHelpers.strategy;

import java.util.List;
import org.example.neuralHelpers.memory.GameMemory;
import org.example.neuralHelpers.memory.StateManager;

public class AggressiveStrategy extends BaseStrategy {
    
    public AggressiveStrategy(StateManager stateManager, GameMemory gameMemory) {
        super(Stage.AGGRESSIVE, stateManager, gameMemory);
    }

    @Override
    public int selectCard(int currentGeierCard) {
        List<Integer> available = stateManager.getAvailableCardsList();
        if (available.isEmpty()) return -1;

        if (currentGeierCard > 0) {
            // Bei positiven Werten: höchstmögliche Karte
            return available.stream()
                .max(Integer::compareTo)
                .orElse(available.get(0));
        } else {
            // Bei negativen Werten: strategische Wahl
            if (Math.abs(currentGeierCard) <= 3) {
                // Bei kleinen negativen Werten: mittlere Karte
                return available.stream()
                    .min((a, b) -> Math.abs(8 - a) - Math.abs(8 - b))
                    .orElse(available.get(0));
            } else {
                // Bei hohen negativen Werten: niedrige Karte
                return available.stream()
                    .min(Integer::compareTo)
                    .orElse(available.get(0));
            }
        }
    }
}