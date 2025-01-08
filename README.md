# Hols der Geier Turnier

## Übersicht
Ein automatisiertes Turniersystem für das Spiel "Hols der Geier", bei dem Bots gegeneinander antreten. Das System ermöglicht es, mehrere Runden zu spielen und automatisch die Gewinner in die nächste Runde zu befördern.

## Spielregeln
- Jeder Bot hat 15 Karten (1-15)
- In jeder Runde wird eine Punktekarte (-5 bis 10, ohne 0) aufgedeckt
- Bots spielen gleichzeitig eine ihrer Karten aus
- Der Bot mit der höchsten Karte gewinnt die positiven Punkte
- Der Bot mit der niedrigeren Karte gewinnt die negativen Punkte
- Bei Gleichstand werden die Punkte zur nächsten Runde addiert

## Projektstruktur
```
src/main/java/org/example/
├── bots/
│   ├── round1/      # Erste Runde Bots
│   ├── round2/      # Zweite Runde Bots
│   ├── ...
│   └── losers/      # Verlierer
├── framework/       # Spiellogik
└── runnables/      # GUI und Turniersteuerung
```

## Bot Entwicklung
Um einen eigenen Bot zu erstellen:
1. Erstelle eine neue Klasse in `round1`
2. Erweitere `HolsDerGeierSpieler`
3. Implementiere die Methoden:
   - `reset()` - Wird zu Beginn jedes Spiels aufgerufen
   - `gibKarte(int naechsteKarte)` - Wählt die zu spielende Karte

Beispiel:
```java
@GeierInfo(name = "MeinBot")
public class MeinBot extends HolsDerGeierSpieler {
    @Override
    public void reset() {
        // Setze Bot zurück
    }
    
    @Override
    public int gibKarte(int naechsteKarte) {
        // Wähle eine Karte (1-15)
        return kartenWahl;
    }
}
```

## Turniersystem
- Bots spielen mehrere Runden gegeneinander
- Gewinner werden automatisch in die nächste Runde verschoben
- Verlierer kommen in den `losers` Ordner
- Bei Unentschieden kommen beide Bots weiter
- Das System erkennt automatisch neue Bots in den Runden-Ordnern

## GUI Bedienung
1. **Player 1/2**: Fügt zufällige Bots aus der aktuellen Runde hinzu
2. **Reset**: Setzt das Turnier zurück
3. **Start Game**: Startet die Spiele mit der angegebenen Anzahl
4. **Anzahl Spiele**: Legt fest, wie viele Spiele pro Paarung gespielt werden

## Technische Details
- Dynamisches Klassenladen ermöglicht das Verschieben von Bots ohne Neustart
- Automatische Kompilierung der verschobenen Bots
- Thread-sicheres Design für parallele Spiele
- Einfach erweiterbare Architektur

## Anforderungen
- Java JDK 17 oder höher
- Maven für Abhängigkeiten
- IDE (empfohlen: IntelliJ IDEA)

## Installation
1. Projekt klonen
2. Maven Dependencies laden
3. Projekt in IDE öffnen
4. `Main.java` ausführen

## Entwicklung und Beiträge
- Fork des Projekts erstellen
- Feature Branch erstellen (`git checkout -b feature/NeuesFunktion`)
- Änderungen committen (`git commit -am 'Neue Funktion hinzugefügt'`)
- Branch pushen (`git push origin feature/NeuesFunktion`)
- Pull Request erstellen
