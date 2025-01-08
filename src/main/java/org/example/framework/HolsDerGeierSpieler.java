package org.example.framework;

public abstract class HolsDerGeierSpieler {
    protected int nummer;
    protected HolsDerGeier hdg;
    public final int getNummer() { return nummer; }
    public final HolsDerGeier getHdg() { return hdg; }
    public final int letzterZug() { return hdg.letzterZug(nummer); }

    public final void register(HolsDerGeier hdg, int nummer) {
        this.hdg = hdg;
        this.nummer = nummer;
    }

    public abstract void reset();
    public abstract int gibKarte(int naechsteKarte);
}