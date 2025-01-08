package org.example.framework;

import org.example.framework.HolsDerGeierSpieler;

/**
 * Beschreiben Sie hier die Klasse Geier.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class Geier extends HolsDerGeierSpieler {
    /**
    /**
     * Hier definieren Sie den Konstruktor fuer Objekte Ihrer Klasse (falls Sie einen eigenen brauchen) Geier
    */


   public void reset () {
    }
   
    public int gibKarte(int naechsteKarte) {
        System.out.println(this.getClass());
        if (naechsteKarte<0)
            return naechsteKarte+6;
        return naechsteKarte+5;
        
    }
    
    
}
