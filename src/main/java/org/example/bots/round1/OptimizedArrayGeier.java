package org.example.bots.round1;

import org.example.framework.GeierInfo;
import org.example.framework.HolsDerGeierSpieler;
import java.util.*;

@GeierInfo(name="OptimizedArrayGeier")
public class OptimizedArrayGeier extends HolsDerGeierSpieler {
    private List<Integer> nochZuGewinnen;
    private Set<Integer> vomGegnerNochNichtGelegt;
    private Set<Integer> nochNichtGespielt;
    private final Random random = new Random();

    // Reusable arrays for simulations
    private int[] myCardsArray;
    private int[] oppCardsArray;
    private int[] pointCardsArray;
    private int[] tempArray;

    public OptimizedArrayGeier() {
        initializeCollections();
        tempArray = new int[15]; // Max size we'll need
    }

    private void initializeCollections() {
        nochZuGewinnen = new ArrayList<>();
        vomGegnerNochNichtGelegt = new HashSet<>();
        nochNichtGespielt = new HashSet<>();
    }

    @Override
    public void reset() {
        initializeCollections();

        for (int i = 10; i > -6; i--) {
            if (i != 0) nochZuGewinnen.add(i);
        }
        for (int i = 15; i > 0; i--) {
            vomGegnerNochNichtGelegt.add(i);
            nochNichtGespielt.add(i);
        }
    }

    @Override
    public int gibKarte(int naechsteKarte) {
        int letzteKarteGegner = letzterZug();
        if (letzteKarteGegner != -99) {
            vomGegnerNochNichtGelegt.remove(letzteKarteGegner);
        }

        if (nochNichtGespielt.isEmpty()) {
            return 1;
        }

        // Update arrays for this turn
        initializeTurnArrays();

        nochZuGewinnen.remove(Integer.valueOf(naechsteKarte));
        int chosenCard = selectBestCard(naechsteKarte);
        nochNichtGespielt.remove(chosenCard);

        return chosenCard;
    }

    private void initializeTurnArrays() {
        myCardsArray = nochNichtGespielt.stream().mapToInt(Integer::intValue).toArray();
        oppCardsArray = vomGegnerNochNichtGelegt.stream().mapToInt(Integer::intValue).toArray();
        pointCardsArray = nochZuGewinnen.stream().mapToInt(Integer::intValue).toArray();
    }

    private int selectBestCard(int naechsteKarte) {
        int bestCard = myCardsArray[0];
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < myCardsArray.length; i++) {
            int myCard = myCardsArray[i];
            double totalScore = 0;

            for (int sim = 0; sim < 100; sim++) {
                int opponentCard = oppCardsArray[random.nextInt(oppCardsArray.length)];
                double score = calculatePoints(myCard, opponentCard, naechsteKarte);
                score += simulateGameCompletion(myCard);
                totalScore += score;
            }

            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestCard = myCard;
            }
        }

        return bestCard;
    }

    private double simulateGameCompletion(int usedMyCard) {
        // Quick copy arrays to temp space
        int myLen = myCardsArray.length - 1;
        int oppLen = oppCardsArray.length;
        int pointLen = pointCardsArray.length;

        System.arraycopy(pointCardsArray, 0, tempArray, 0, pointLen);

        double totalPoints = 0;
        int myCardCount = myLen;
        int oppCardCount = oppLen;
        int pointCardCount = pointLen;

        while (pointCardCount > 0 && myCardCount > 0) {
            // Use direct swap for point card
            int pointIdx = random.nextInt(pointCardCount);
            int pointCard = tempArray[pointIdx];
            tempArray[pointIdx] = tempArray[--pointCardCount];

            // Pick random cards
            int myCard = myCardsArray[random.nextInt(myCardCount)];
            if (myCard == usedMyCard) {
                myCard = myCardsArray[--myCardCount];
            } else {
                --myCardCount;
            }

            int oppCard = oppCardsArray[random.nextInt(oppCardCount--)];

            totalPoints += calculatePoints(myCard, oppCard, pointCard);
        }

        return totalPoints;
    }

    private double calculatePoints(int myCard, int opponentCard, int points) {
        if (points > 0) {
            if (myCard > opponentCard) return points;
            if (opponentCard > myCard) return -points;
        } else {
            if (myCard > opponentCard) return points;
            if (opponentCard > myCard) return -points;
        }
        return 0;
    }
}