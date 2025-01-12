package org.example.neuralHelpers.memory;

import lombok.Getter;
import java.util.*;

import org.example.neuralHelpers.exceptions.StatemanagementException;

public class StateManager {
    @Getter
    private final boolean[] availableCards;
    @Getter
    private final List<Integer> playedCards;
    @Getter
    private final List<Integer> opponentCards;
    @Getter
    private final List<Integer> geierCards;
    private int currentStage;

    private static final int MIN_CARD = 1;
    private static final int MAX_CARD = 15;
    private static final int MIN_GEIER = -5;
    private static final int MAX_GEIER = 10;  // 0=passive, 1=neutral, 2=aggressive

    public StateManager() {
        availableCards = new boolean[15];
        Arrays.fill(availableCards, true);
        playedCards = new ArrayList<>();
        opponentCards = new ArrayList<>();
        geierCards = new ArrayList<>();
        currentStage = 1;  // Start neutral
    }

    public void recordMove(int ownCard, int opponentCard, int geierCard) {
        validateCard("Eigene Karte", ownCard);
        validateCard("Gegner-Karte", opponentCard);
        validateGeierCard(geierCard);
        validateCardAvailable(ownCard);

        try {
            availableCards[ownCard - 1] = false;
            playedCards.add(ownCard);
            opponentCards.add(opponentCard);
            geierCards.add(geierCard);
            updateStage();
        } catch (Exception e) {
            throw new StatemanagementException("Fehler beim Aufzeichnen des Zuges", e);
        }
    }

    private void validateCard(String cardType, int card) {
        if (card < MIN_CARD || card > MAX_CARD) {
            throw new IllegalArgumentException(
                String.format("%s ungültig: %d (erlaubt: %d-%d)", 
                cardType, card, MIN_CARD, MAX_CARD)
            );
        }
    }

    private void validateGeierCard(int geierCard) {
        if (geierCard < MIN_GEIER || geierCard > MAX_GEIER || geierCard == 0) {
            throw new IllegalArgumentException(
                String.format("Geierkarte ungültig: %d (erlaubt: %d bis %d, ohne 0)", 
                geierCard, MIN_GEIER, MAX_GEIER)
            );
        }
    }

    private void validateCardAvailable(int card) {
        if (!isCardAvailable(card)) {
            throw new IllegalStateException(
                String.format("Karte %d wurde bereits gespielt", card)
            );
        }
    }

    public boolean isCardAvailable(int card) {
        return availableCards[card - 1];
    }

    public List<Integer> getAvailableCardsList() {
        List<Integer> cards = new ArrayList<>();
        for (int i = 0; i < availableCards.length; i++) {
            if (availableCards[i]) {
                cards.add(i + 1);
            }
        }
        return cards;
    }

    private void updateStage() {
        int roundsPlayed = playedCards.size();
        if (roundsPlayed >= 5) {
            // Analyse der letzten 5 Züge für Strategiewechsel
            double avgValue = geierCards.subList(roundsPlayed-5, roundsPlayed)
                                      .stream()
                                      .mapToInt(Integer::intValue)
                                      .average()
                                      .orElse(0.0);
            if (avgValue > 5) currentStage = 2;  // Aggressive
            else if (avgValue < -2) currentStage = 0;  // Passive
            else currentStage = 1;  // Neutral
        }
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void reset() {
        Arrays.fill(availableCards, true);
        playedCards.clear();
        opponentCards.clear();
        geierCards.clear();
        currentStage = 1;
    }

    public double[] getStateVector() {
        double[] state = new double[31];  // 15 + 15 + 1
        // Eigene verfügbare Karten
        for (int i = 0; i < 15; i++) {
            state[i] = availableCards[i] ? 1.0 : 0.0;
        }
        // Gegner gespielte Karten
        for (int i = 0; i < opponentCards.size(); i++) {
            state[15 + opponentCards.get(i) - 1] = 1.0;
        }
        // Aktuelle Geierkarte (normalisiert)
        if (!geierCards.isEmpty()) {
            state[30] = geierCards.get(geierCards.size() - 1) / 10.0;
        }
        return state;
    }
}