package org.example.bots.losers;


import java.util.*;

import org.example.framework.GeierInfo;
import org.example.framework.HolsDerGeier;
import org.example.framework.HolsDerGeierSpieler;

/**
 * Author: Loick Gandonou
 * Date: 29.12.2024
 * Description: <Diese Klasse  befasst sich mit einem Bot, welcher eine Prioitätenstratgie ausführt, aber auch versucht mittels heuristischer Analyse die Gegnerkartejn in Relation zu den Geierkarten vorherzusgaen>
 */

/*
eventuell werde ich den Geier erweitern, um eine aggressivere Spielart zu haben
 */

@GeierInfo(name = "LoickV2")
public class LoickV2 extends HolsDerGeierSpieler {

    private int meineNummer; // RegisterNummer, welche Nummer haben wir im Spiel? 1 oder 0?
    private HolsDerGeier hdg; // Spielreferenz
    private final int OBEREGRENZESPIELERDECK = 15;
    private final int UNTEREGRENZESPIELERDECK = 1;

    private ArrayList<Integer> meineKarten = new ArrayList<>();
    private ArrayList<Integer> gegnerKarten = new ArrayList<>();
    private ArrayList<Integer> geierUndMausekarten = new ArrayList<>();
    // wir speichern hier die Züge von unserem Gegner
    private ArrayList<int[]> gegnerZuege = new ArrayList<>();

    private int zaehleZuege = 0; // Standard 0
    final private int[] negativenKarten = {-5, -4, -3, -2, -1};

    private Map<String, Integer> punkte = new HashMap<>();
    // müssen eingebaut werden, um unsere letzten Züge zu berechnen
    Stack<Integer> meinLetzterZug = new Stack<>();
    Stack<Integer> gegnersLetzterZug = new Stack<>();
    Stack<Integer> letzteGeieroderMausekarte = new Stack<>();

    // Entfernt eine Karte aus "meineKarten" und gibt sie zurück
    private int entferneKarte(int karte) {
        meineKarten.remove(Integer.valueOf(karte)); // Karte entfernen
        return karte; // Karte zurückgeben
    }

    // wir picken hier die gelegten Karten heraus und speichern dies in unseren Listen, um darauf basierende Strategien zu entwickeln
    public void analysiereGegner(int geierMauseKarte) {
        // Hole die zugewiesene RegisterNummer vom HolsDerGeier-Spiel
        meineNummer = getNummer();
        // Ermittelt die Nummer des Gegners durch Vergleich mit unserer eigenen, um den letzten Zug des Gegners abzufragen
        int gegnerNummer = meineNummer == 1 ? 0 : 1;

        // Enthält die Information über den letzten Zug von unserem Gegner
        int gegnersLetzterZug = getHdg().letzterZug(gegnerNummer);

        // Entferne die Geier-/Mäusekarte aus unserem SimulationsDeck
        geierUndMausekarten.remove(Integer.valueOf(geierMauseKarte));

        // Um künftig Analyse zu betreiben, speichern wir in Form von Arrays immer eine Geierkarte und die Gegnerkarte,
        // diese stehen in Beziehung zu unserem Gegner
        if (gegnersLetzterZug != -99) { // Nur wenn ein gültiger Zug existiert, -99 ist der Defaultwert
            gegnerZuege.add(new int[]{geierMauseKarte, gegnersLetzterZug});

            // Entferne die Karte aus dem Deck des Gegners
            gegnerKarten.remove(Integer.valueOf(gegnersLetzterZug));
        }
    }

    /*
    ich verfolge eine Prioritätenstrategie,
    bei welcher wir die Geier- bzw. Mäusekarten in Klassen klassifizieren:
    1. niedrige Priorität
    2. mittlere Priorität
    3. hohe Priorität
    */

    // Geier-/Mausedeck -5 bis -1 hat eine niedrige Priorität
    public int spieleNiedrigeKarte() {
        int karte = meineKarten.remove(0);
        return karte;
    }

    // Geier-/Mausedeck 1 bis 5 hat eine mittlere Priorität
    public int spieleMittlereKarte() {
        int karte = meineKarten.remove(meineKarten.size() / 2);
        return karte;
    }

    // Geier-/Mausedeck 6 bis 10 hat eine hohe Priorität
    public int spieleHoheKarte() {
        int karte = meineKarten.remove(meineKarten.size() - 1);
        return karte;
    }

    // Bluff-Mechanismus einbauen, so dass ich einfach mal sehr hohe Karten lege
    public int verwirreGegnerZufällig() {
        Random random = new Random();
        int zufallsIndex = random.nextInt(meineKarten.size() / 2, meineKarten.size() - 1); // -1, da wir den Index wollen
        return meineKarten.remove(zufallsIndex);
    }

    public int verwirreGegnerAlle5Runden(int anzahlZug) {
        // gehe hier sicher, dass es sich tatsächlich um den 5. Zug handelt
        if (anzahlZug % 5 == 0 && anzahlZug > 0) {
            return spieleHoheKarte();
        }
        // Falls nicht, spiele eine mittlere Karte
        return spieleMittlereKarte();
    }

    /*
    Der zweite Teil meiner Strategie befasst sich damit, über die Runden hinweg
    einen Durchschnitt zu berechnen, welche Karte mein Gegner bei einer Geier-/Mäusekarte legt,
    um anschließend mindestens eine um 1 größere Karte zu legen.
    */
    public int vorhersageGegnerKarte(int geierMauseKarte) {
        int summe = 0;
        int anzahl = 0;

        // Berechne Durchschnittswerte
        for (int[] zug : gegnerZuege) {
            if (zug[0] == geierMauseKarte) {
                summe += zug[1];
                anzahl++;
            }
        }

        // Rückgabe des Durchschnittswerts oder Standardwert
        return anzahl > 0 ? (summe / anzahl) : (meineKarten.size() / 2);
    }

    // Prüft offensive Strategie und spielt entsprechende Karte
    public int pressing(int geiderOderMauseKarte) {
        boolean spieleOffensiv = false;
        HashSet<Integer> sammelNegativeKarte = new HashSet<>();

        for (int x : letzteGeieroderMausekarte) {
            if (x > -6 && x < 0) {
                sammelNegativeKarte.add(x);
            }
        }

        // Prüfen, ob alle negativen Karten gespielt wurden
        if (sammelNegativeKarte.containsAll(Arrays.asList(negativenKarten))) {
            spieleOffensiv = true;
        }

        if (spieleOffensiv) {
            switch (geiderOderMauseKarte) {
                case 10:
                    if (meineKarten.contains(15)) return entferneKarte(15);
                    break;
                case 9:
                    if (meineKarten.contains(14)) return entferneKarte(14);
                    break;
                case 8:
                    if (meineKarten.contains(13)) return entferneKarte(13);
                    break;
                case 7:
                    if (meineKarten.contains(12)) return entferneKarte(12);
                    break;
                case 6:
                    if (meineKarten.contains(11)) return entferneKarte(11);
                    break;
                case 5:
                    if (meineKarten.contains(10)) return entferneKarte(10);
                    break;
                case 4:
                    if (meineKarten.contains(9)) return entferneKarte(9);
                    break;
                case 3:
                    if (meineKarten.contains(8)) return entferneKarte(8);
                    break;
                case 2:
                    if (meineKarten.contains(7)) return entferneKarte(7);
                    break;
                case 1:
                    if (meineKarten.contains(6)) return entferneKarte(6);
                    break;
                default:
                    return spieleMittlereKarte();
            }
        }

        return spieleMittlereKarte();
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
            if (i != 0) geierUndMausekarten.add(i);
        }
    }

    public void resetGegnerDeck() {
        gegnerKarten.clear();
        for (int i = UNTEREGRENZESPIELERDECK; i <= OBEREGRENZESPIELERDECK; i++) {
            gegnerKarten.add(i);
        }
    }

    public void resetGespeichertenZüge() {
        meinLetzterZug.clear();
        gegnersLetzterZug.clear();
        letzteGeieroderMausekarte.clear();
        gegnerZuege.clear();
        zaehleZuege = 0;
    }

    @Override
    public void reset() {
        resetMeinDeck();
        resetGeierundMauseKarten();
        resetGegnerDeck();
        resetGespeichertenZüge();
    }

    public void speichereLetzteZuege(int meineKarte, int gegnerKarte, int geierOderMauseKarte) {
        meinLetzterZug.push(meineKarte);
        gegnersLetzterZug.push(gegnerKarte);
        letzteGeieroderMausekarte.push(geierOderMauseKarte);
    }

    private int waehleKarte(int geierOderMauseKarte) {
        if (!meineKarten.isEmpty()) {
            if (zaehleZuege % 5 == 0 && geierOderMauseKarte >= 7) {
                return verwirreGegnerAlle5Runden(zaehleZuege);
            }

            int vorhergesagteGegnerKarte = vorhersageGegnerKarte(geierOderMauseKarte);

            if (-6 < geierOderMauseKarte && geierOderMauseKarte < 0) {
                for (int karte : meineKarten) {
                    if (karte == (vorhergesagteGegnerKarte + 1)) {
                        return entferneKarte(karte);
                    }
                }
                return spieleNiedrigeKarte();
            }

            if (0 < geierOderMauseKarte && geierOderMauseKarte < 6) {
                return spieleMittlereKarte();
            }

            if (5 < geierOderMauseKarte && geierOderMauseKarte < 11) {
                return spieleHoheKarte();
            }
        }
        System.out.println("Spieler: " + meineNummer + " hat keine Karten mehr");
        return -99;
    }


    @Override
    public int gibKarte(int naechsteKarte) {
        hdg=super.getHdg();
        int geierOderMauseKarte = naechsteKarte; // Geier-/Mäusekarte der aktuellen Runde
        zaehleZuege++; // Anzahl der Züge erhöhen

        // Analysiere den letzten Zug des Gegners
        analysiereGegner(geierOderMauseKarte);

        // Verwende pressing, wenn keine negativen Karten mehr existieren, so dass ich festgelegte Karten spiele
        int meineKarte;
        if (letzteGeieroderMausekarte.containsAll(Arrays.asList(negativenKarten))) {
            // Wenn alle negativen Karten bereits gespielt wurden
            meineKarte = pressing(geierOderMauseKarte);
        } else {
            // Andernfalls nutze die Standardstrategie
            meineKarte = waehleKarte(geierOderMauseKarte);
        }

        // Speichere den aktuellen Zug
        speichereLetzteZuege(meineKarte, hdg.letzterZug(meineNummer == 1 ? 0 : 1), geierOderMauseKarte);

        return meineKarte;
    }


}