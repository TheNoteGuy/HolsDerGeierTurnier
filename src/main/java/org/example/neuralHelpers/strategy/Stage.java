package org.example.neuralHelpers.strategy;

import lombok.Getter;

@Getter
public enum Stage {
    PASSIVE(0.3, 0.1, "Defensive Spielweise, minimiert Verluste"),
    NEUTRAL(0.5, 0.2, "Ausgewogene Spielweise, balanciert Risiko"),
    AGGRESSIVE(0.8, 0.3, "Offensive Spielweise, maximiert Gewinne");

    private final double riskFactor;
    private final double bluffProbability;
    private final String description;

    Stage(double riskFactor, double bluffProbability, String description) {
        this.riskFactor = riskFactor;
        this.bluffProbability = bluffProbability;
        this.description = description;
    }

    public static Stage fromRiskLevel(double risk) {
        if (risk < 0.4) return PASSIVE;
        if (risk > 0.6) return AGGRESSIVE;
        return NEUTRAL;
    }
}