package org.example.bots.round1;

import java.util.ArrayList;

import org.example.framework.GeierInfo;
import org.example.framework.HolsDerGeierSpieler;

/**
 * Author: Loick Gandonou
 * Date: 29.12.2024
 * Description: <Diese Klasse  befasst sich mit einem Bot, welcher eine Prioitätenstratgie ausführt, aber auch versucht mittels ehuristischer Analyse die Gegnerkartejn in Relation zu den Geierkarten vorherzusgaen>
 */


 @GeierInfo(name = "Loick")
public class Loick extends HolsDerGeierSpieler {
    // unsere RegisterNummer erhalten
    private int meineNummer;
    private final int OBEREGRENZESPIELERDECK = 15;
    private final int UNTEREGRENZESPIELERDECK = 1;


    private ArrayList<Integer> meineKarten = new ArrayList<>();
    private ArrayList<Integer> gegnerKarten = new ArrayList<>();
    private ArrayList<Integer> geierUndMausekarten = new ArrayList<>();

    // wir speichern hier die Zuege von unserem gegner
    private ArrayList<int[]> gegnerZuege = new ArrayList<>();

    public Loick() {
        reset();
    }


    // wir  picken hier die gelegten Karten heraus und speichern dies in unseren Listen, um aufbauen darauf Stratgeien zu entwickeln

    public void analysiereGegner(int geierkarte) {
        // Hole die  zugewiesene RegisterNummer vom HolsDerGeier-Spiel
        meineNummer = getNummer();
        // Ermittelt die Nummer des Gegners durch  Vergleich mit unserer eigenen, um letzten Zug des Gegners folgend abzufragen
        int gegnerNummer = meineNummer == 1 ? 0 : 1;

        // Erhaltet die Information über den letzten Zug von unserem Gegner
        int gegnersLetzterZug = getHdg().letzterZug(gegnerNummer);

        // Entferne die Geier-/Mäusekarte aus unserem SimulationsDeck
        geierUndMausekarten.remove(Integer.valueOf(geierkarte));

        // Um künftig Analyse zu betreiben, speichern wir in Form von Arrays immer eine Geierkarte und die Gegnerkarte
        if (gegnersLetzterZug != -99) { // Nur wenn ein gültiger Zug existiert
            gegnerZuege.add(new int[]{geierkarte, gegnersLetzterZug});

            // Entferne die Karte aus dem Deck des Gegners
            gegnerKarten.remove(Integer.valueOf(gegnersLetzterZug));
        }
    }

    /*
    ich verfolge eine Prioriätsstrategie,
     bei welcher wir die Geier bzw.Mausekarten in  Klassen klassifizieren,
     1. niedrige Priorität
     2. Mittlere Priorität
     3. hohe Priorität
     */

    // Geier-/Mausedeck -5 bis -1 hat eine niedrige Prioität
    public int niedrigePriorität() {
        int karte = meineKarten.remove(0);
        return karte;


    }

    // Geier-/Mausedeck 1 bis 5 hat eine niedrige Prioität
    public int mittlerePriorität() {
        int karte = meineKarten.remove(meineKarten.size() / 2);
        return karte;


    }

    // Geier-/Mausedeck -6 bis 10 hat eine niedrige Prioität
    public int hohePriorität() {
        int karte = meineKarten.remove(meineKarten.size() - 1);
        return karte;

    }


    /*
    der zweite Teil meiner Strategie befasst sich damit über die Runden hinweg
    einen Druchschnitt zu berechnen, welche Karte mein Gegner bei einer Geier/Mausekarte legt,
    um folgend  mindesten eine um 1 größere Karte zu legen
     */

    public int vorhersageGegnerKarte(int geierkarte) {
        int summe = 0;
        int anzahl = 0;

        /*wir durchsuchen hier alle Zuege, welche identisch sind mit der Geierkarte der aktuellen Runde
        und kalkulieren damit einen Durchschnitt
        dier Durchscnitt berechnet sich durch die Summen der einzelnen gelegten Gegnerkarte bei spezifischer Geier/Mausekarte,
        und teilt diesen durch die Anzahl, dieser Fälle, also der absoluten Menge des Auftretens

         */
        for (int[] zug : gegnerZuege) {
            if (zug[0] == geierkarte) { // Gleiche Geierkarte
                summe += zug[1]; // Gegnerzug addieren
                anzahl++;
            }
        }

        // wir geben
        return anzahl > 0 ? (summe / anzahl) : (meineKarten.size() / 2); // Standardwert: Mittlere Karte
    }

    public void resetMeinDeck() {
        meineKarten.clear();
        for (int i = UNTEREGRENZESPIELERDECK; i <= OBEREGRENZESPIELERDECK; i++) {
            meineKarten.add(i);
        }
    }

    public void resetGeierundMauseKarten() {
        geierUndMausekarten.clear();
        for (int i = -5; i < 11; i++) {
            if (i != 0)
                geierUndMausekarten.add(i);

        }
    }

    public void resetGegnerDeck() {
        gegnerKarten.clear();
        for (int i = UNTEREGRENZESPIELERDECK; i <= OBEREGRENZESPIELERDECK; i++) {
            gegnerKarten.add(i);
        }

    }

    /* in Reset sorgen wir dafür, dass unser Deck mit jeder Runde wieder neu und frisch organisiert wird
    dabei nutzen wir unsere  reset Methoden für unsere simulierten Decks aus
     */
    @Override

    public void reset() {
        // meinDeck
        resetMeinDeck();
        //Geier und Mausekarten Deck wird initialisiert
        resetGeierundMauseKarten();
        resetGegnerDeck();

    }

    @Override
    public int gibKarte(int naechsteKarte) {
        int geierOderMausekarte = naechsteKarte;

        // Vorhersage der Karte, die der Gegner wahrscheinlich spielt
        int potentielleVorhersageGegner = vorhersageGegnerKarte(geierOderMausekarte);

        if (-6 < geierOderMausekarte && geierOderMausekarte < 0) { // Für negative Karten
            // Suche nach der Karte, die genau 1 höher ist als die Vorhersage
            for (int karte : meineKarten) {
                // wenn wir eine Analysegrundlage haben, dann können wir potentielleVorhersageGegner +1 zurückgeben, also den durschnittswert  den unser gegner legt +1
                // vielleihct kan ich die if condition mit contain vereinfachen
                if (karte == (potentielleVorhersageGegner + 1)) {
                    meineKarten.remove(Integer.valueOf(karte));
                    return karte;
                }
            }

            // Falls keine Karte 1 höher vorhanden ist, spiele die niedrigste verfügbare Karte
            // dieser Falle solle aber nie eintreffen
            return niedrigePriorität();
        }

        if (0 < geierOderMausekarte && geierOderMausekarte < 6) { // Für neutrale Karten
            return mittlerePriorität();
        }

        if (5 < geierOderMausekarte && geierOderMausekarte < 11) { // Für positive Karten
            return hohePriorität();
        }
// vorher hatte ich -99 ausgegben, habe mich beraten lassen und sollte lieber eine Exception werfen
        throw new IllegalStateException("Ungültige Geier-/Mäusekarte: " + naechsteKarte); // Fehlerfall, sollte nie erreicht werden, würde sowieso eine Exception werfen
    }
}