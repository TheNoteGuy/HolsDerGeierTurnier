package org.example.bots.round1;
import org.example.framework.GeierInfo;
import org.example.framework.HolsDerGeierSpieler;

/**
 * BitBot - Optimierter Bot für "Hols der Geier"
 * Verwendet Bit-Operationen für maximale Effizienz.
 * Strategie:
 * - Für hohe Punktekarten (≥7): Verwendet höchste Karten (15-11)
 * - Für negative Karten: Verwendet niedrigste Karten (1-5)
 * - Für niedrige positive Karten: Verwendet mittlere Karten (10-6)
 */

@GeierInfo(name = "BitBot")
public class BitBot extends HolsDerGeierSpieler {
    private int bits = (1 << 16) - 2, count = 15;  // Bit-Feld: 0111 1111 1111 1110 für Karten 1-15 / count: Anzahl verbleibender Karten für schnellen Leercheck
    public void reset() { bits = (1 << 16) - 2; count = 15; } // Setzt beide Felder auf Ausgangszustand zurück

    /**
     * Wählt eine Karte basierend auf der aufgedeckten Punktekarte
     * @param n Punktewert der aufgedeckten Karte (-5 bis 10)
     * @return Gewählte Karte (1-15)
     */

    public int gibKarte(int n) {
        if (count == 0) return 1;  // Keine Karten übrig
        if (n >= 7) for (int i = 15; i >= 11; i--) if ((bits & (1 << i)) != 0) { bits &= ~(1 << i); count--; return i; }  // Hohe für hohe
        if (n < 0) for (int i = 1; i <= 5; i++) if ((bits & (1 << i)) != 0) { bits &= ~(1 << i); count--; return i; }     // Niedrige für negative
        for (int i = 10; i >= 6; i--) if ((bits & (1 << i)) != 0) { bits &= ~(1 << i); count--; return i; }               // Mittlere Karten
        for (int i = 1; i <= 15; i++) if ((bits & (1 << i)) != 0) { bits &= ~(1 << i); count--; return i; }               // Fallback
        return 1;
    }
}