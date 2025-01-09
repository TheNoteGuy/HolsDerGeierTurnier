package org.example.bots.losers;

import org.example.framework.GeierInfo;
import org.example.framework.HolsDerGeierSpieler;
import java.util.*;
import com.github.alexeyr.pcg.Pcg32;

/**
 * Optimierte version mit array-basierter monte-carlo simulation.
 *
 * @author Nico
 * @version v3.0.1
 */

@GeierInfo(name="Testbot")
public class TestBot extends HolsDerGeierSpieler
{
    // Hauptsammlungen
    private List<Integer> nochZuGewinnen;
    private Set<Integer> vomGegnerNochNichtGelegt;
    private Set<Integer> nochNichtGespielt;
    private final Pcg32 random = new Pcg32();

    // Arrays für schnellere Simulationen
    private int[] myCardsArray;
    private int[] oppCardsArray;
    private int[] pointCardsArray;
    private int[] tempArray;

    public TestBot()
    {
        initializeCollections();
        tempArray = new int[15];
    }

    // Initialisierung der Sammlungen
    private void initializeCollections()
    {
        nochZuGewinnen = new ArrayList<>();
        vomGegnerNochNichtGelegt = new HashSet<>();
        nochNichtGespielt = new HashSet<>();
    }

    // Spielstart: Alle Karten neu verteilen
    @Override
    public void reset()
    {
        initializeCollections();

        for (int i = 10; i > -6; i--)
        {
            if (i != 0) nochZuGewinnen.add(i);
        }
        for (int i = 15; i > 0; i--)
        {
            vomGegnerNochNichtGelegt.add(i);
            nochNichtGespielt.add(i);
        }

        // Arrays initialisieren
        myCardsArray = nochNichtGespielt.stream().mapToInt(Integer::intValue).toArray();
        oppCardsArray = vomGegnerNochNichtGelegt.stream().mapToInt(Integer::intValue).toArray();
        pointCardsArray = nochZuGewinnen.stream().mapToInt(Integer::intValue).toArray();
    }

    // Hauptlogik für Kartenwahl
    @Override
    public int gibKarte(int naechsteKarte)
    {
        int letzteKarteGegner = letzterZug();
        if (letzteKarteGegner != -99)
        {
            vomGegnerNochNichtGelegt.remove(letzteKarteGegner);
        }

        if (nochNichtGespielt.isEmpty())
        {
            return 1;
        }

        updateTurnArrays();

        nochZuGewinnen.remove(Integer.valueOf(naechsteKarte));
        int chosenCard = selectBestCard(naechsteKarte);
        nochNichtGespielt.remove(chosenCard);

        return chosenCard;
    }

    // Konvertiert Sammlungen zu Arrays für Simulation
    private void updateTurnArrays()
    {
        myCardsArray = nochNichtGespielt.stream().mapToInt(Integer::intValue).toArray();
        oppCardsArray = vomGegnerNochNichtGelegt.stream().mapToInt(Integer::intValue).toArray();
        pointCardsArray = nochZuGewinnen.stream().mapToInt(Integer::intValue).toArray();
    }

    // Monte Carlo Simulation für beste Kartenwahl
    private int selectBestCard(int naechsteKarte)
    {
        int bestCard = myCardsArray[0];
        double bestScore = Double.NEGATIVE_INFINITY;

        int simulations = 150;
        int[] randomIndices = new int[simulations];
        for (int j = 0; j < simulations; j++) {
            randomIndices[j] = random.nextInt(oppCardsArray.length);
        }

        for (int i = 0; i < myCardsArray.length; i++)
        {
            int myCard = myCardsArray[i];
            double totalScore = 0;

            for (int sim = 0; sim < simulations; sim++)
            {
                int opponentCard = oppCardsArray[randomIndices[sim]];
                double score = calculatePoints(myCard, opponentCard, naechsteKarte);
                score += simulateGameCompletion(myCard);
                totalScore += score;
            }

            if (totalScore > bestScore)
            {
                bestScore = totalScore;
                bestCard = myCard;
            }
        }

        return bestCard;
    }

    // Simuliert restliches Spiel
    private double simulateGameCompletion(int usedMyCard)
    {
        int myLen = myCardsArray.length - 1;
        int oppLen = oppCardsArray.length;
        int pointLen = pointCardsArray.length;

        System.arraycopy(pointCardsArray, 0, tempArray, 0, pointLen);

        double totalPoints = 0;
        int myCardCount = myLen;
        int oppCardCount = oppLen;
        int pointCardCount = pointLen;

        while (pointCardCount > 0 && myCardCount > 0)
        {
            int pointIdx = random.nextInt(pointCardCount);
            int pointCard = tempArray[pointIdx];
            int myCard = myCardsArray[random.nextInt(myCardCount)];
            tempArray[pointIdx] = tempArray[--pointCardCount];

            if (myCard == usedMyCard)
            {
                myCard = myCardsArray[--myCardCount];
            }

            else
            {
                --myCardCount;
            }

            int oppCard = oppCardsArray[random.nextInt(oppCardCount--)];

            totalPoints += calculatePoints(myCard, oppCard, pointCard);
        }

        return totalPoints;
    }

    // Punkte für einen Spielzug berechnen

    private double calculatePoints(int myCard, int opponentCard, int points) {
        double score = 0;

        // Base scoring for winning/losing the round
        if (points > 0) {
            if (myCard > opponentCard) {
                score = points;
                // Bonus for efficient card usage (not overbidding too much)
                score += (10 - (myCard - opponentCard)) * 0.1;
            } else if (opponentCard > myCard) {
                score = -points;
                // Penalty for losing with a high card
                score -= (myCard * 0.2);
            }
        } else {
            // Negative points (mice) are handled differently
            if (myCard > opponentCard) {
                score = points;
                // Smaller bonus for efficient card usage on negative points
                score += (5 - (myCard - opponentCard)) * 0.1;
            } else if (opponentCard > myCard) {
                score = -points;
                // Smaller penalty for losing negative points
                score -= (myCard * 0.1);
            }
        }

        // Strategic card value preservation
        if (points > 7 || points < -4) {
            // Penalize using high cards (13-15) on low-value points
            if (myCard >= 13 && Math.abs(points) < 5) {
                score -= 2.0;
            }

            // Bonus for saving low cards for negative points
            if (points < 0 && myCard <= 5) {
                score += 1.0;
            }
        }

        // Early game strategy
        if (myCardsArray.length > 10) {  // First third of the game
            // Encourage keeping high cards for later
            if (myCard >= 13) {
                score -= 1.5;
            }
            // Encourage using middle-value cards early
            if (myCard >= 6 && myCard <= 9) {
                score += 0.5;
            }
        }

        // Late game strategy
        if (myCardsArray.length <= 5) {  // Last third of the game
            // More aggressive high card usage if we have them
            if (points > 5 && myCard >= 12) {
                score += 2.0;
            }
            // Desperate defense against negative points
            if (points < -3 && myCard >= 10) {
                score += 1.0;
            }
        }

        return score;
    }
}
