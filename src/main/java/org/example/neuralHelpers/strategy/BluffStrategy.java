package org.example.neuralHelpers.strategy;

import java.util.List;
import java.util.Random;
import org.example.neuralHelpers.memory.GameMemory;
import org.example.neuralHelpers.memory.StateManager;

public class BluffStrategy extends BaseStrategy {
    private final Random random = new Random();
    
    public BluffStrategy(StateManager stateManager, GameMemory gameMemory) {
        super(Stage.NEUTRAL, stateManager, gameMemory);
    }

    @Override
    public int selectCard(int currentGeierCard) {
        List<Integer> available = stateManager.getAvailableCardsList();
        if (available.isEmpty()) return -1;

        if (shouldBluff()) {
            // Zufällige Karte für Täuschung
            return available.get(random.nextInt(available.size()));
        }

        // Normale Strategie basierend auf Spielverlauf
        double successRate = gameMemory.getCardSuccessRate(currentGeierCard);
        if (successRate > 0.6) {
            // Bewährte Strategie fortsetzen
            return available.stream()
                .max((a, b) -> Double.compare(
                    gameMemory.getCardSuccessRate(a),
                    gameMemory.getCardSuccessRate(b)))
                .orElse(available.get(0));
        }
        
        // Ausgewogene Wahl
        return available.get(available.size() / 2);
    }
}