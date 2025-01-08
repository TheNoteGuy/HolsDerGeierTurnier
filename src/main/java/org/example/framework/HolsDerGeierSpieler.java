package org.example.framework;

public abstract class HolsDerGeierSpieler {
    protected int nummer;  // Protected for direct access in subclasses
    protected HolsDerGeier hdg;  // Protected for direct access in subclasses

    // Fast inline getters
    public final int getNummer() { return nummer; }
    public final HolsDerGeier getHdg() { return hdg; }

    // Made final to prevent override and allow JVM optimization
    public final int letzterZug() { return hdg.letzterZug(nummer); }

    // Made final to ensure registration can't be altered by subclasses
    public final void register(HolsDerGeier hdg, int nummer) {
        this.hdg = hdg;
        this.nummer = nummer;
    }

    // Abstract methods that subclasses must implement
    public abstract void reset();
    public abstract int gibKarte(int naechsteKarte);
}