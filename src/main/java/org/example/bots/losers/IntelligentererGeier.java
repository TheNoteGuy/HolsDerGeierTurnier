package org.example.bots.losers;

import org.example.framework.GeierInfo;
import org.example.framework.HolsDerGeierSpieler;

import java.util.ArrayList;
/**
 * Beschreiben Sie hier die Klasse IntelligentererGeier.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */

@GeierInfo(name="IntelligentererGeier")
public class IntelligentererGeier extends HolsDerGeierSpieler {

    private ArrayList<Integer> nochZuGewinnen=new ArrayList<Integer>();
    private ArrayList<Integer> vomGegnerNochNichtGelegt=new ArrayList<Integer>();
    private ArrayList<Integer> nochNichtGespielt=new ArrayList<Integer>();
    
    /**
     * Hier definieren Sie den Konstruktor fuer Objekte Ihrer Klasse (falls Sie einen eigenen brauchen) IntelligentererGeier
    */
    public IntelligentererGeier() {
    }
            
    public void reset() {
        nochZuGewinnen.clear();
        for (int i=10;i>-6;i--)
            nochZuGewinnen.add(i);
        vomGegnerNochNichtGelegt.clear();        
        for (int i=15;i>0;i--)
            vomGegnerNochNichtGelegt.add(i);
        for (int i=15;i>0;i--)            
            nochNichtGespielt.add(i);                                 
    }
    
    public int gibKarte(int naechsteKarte) {
       int ret=-99;
       int letzteKarteGegner=letzterZug();
       if (letzteKarteGegner!=-99)
          vomGegnerNochNichtGelegt.remove(vomGegnerNochNichtGelegt.indexOf(letzteKarteGegner));
       // L�sche dieser Karte
       nochZuGewinnen.remove(nochZuGewinnen.indexOf(naechsteKarte));
       // Wenn die karte negativ ist  => spiele die letzte Karte
       if (naechsteKarte>0) {           
           // Kommt die hoechste Karte und der gener hat die gleiche h�chste => spiele mittlere Karte
           if ((nochZuGewinnen.indexOf(naechsteKarte)==0)&&(vomGegnerNochNichtGelegt.get(0)==nochNichtGespielt.get(0))) {
                   ret=nochNichtGespielt.get(nochZuGewinnen.size()/2);
                   //System.out.println(">>> A");
           // Steht die zu gewinnende Karte in der vorderen Haelfte der Reihenfolge und hat der gegner => Spiele die h�chste Karte
        } else if (nochZuGewinnen.indexOf(naechsteKarte)>nochZuGewinnen.size()/2) {
                   ret=nochNichtGespielt.get(0);  
                   //System.out.println(">>> B");                   
                } else { // sonst greife in die Mitte
                   ret=nochNichtGespielt.get(nochZuGewinnen.size()/2);
                   //System.out.println(">>> C");                   
                }
       } else {
           ret=nochNichtGespielt.get(nochNichtGespielt.size()-1);
           //System.out.println(">>> D");                              
       }
       nochNichtGespielt.remove(nochNichtGespielt.indexOf(ret));
       return ret;
    }
    
}
