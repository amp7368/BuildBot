package apple.build.data.constraints.answers;

public class DamageOutput {
    private final double neutralLower;
    private final double neutralUpper;
    private final double[] elementalLower;
    private final double[] elementalUpper;

    private final double neutralLowerCrit;
    private final double neutralUpperCrit;
    private final double[] elementalLowerCrit;
    private final double[] elementalUpperCrit;

    private final float critChance;

    private final boolean spell;

    public DamageOutput(double neutralLower, double neutralUpper, double[] elementalLower, double[] elementalUpper,
                        double neutralLowerCrit, double neutralUpperCrit, double[] elementalLowerCrit, double[] elementalUpperCrit,
                        float critChance) {
        this.neutralLower = neutralLower;
        this.neutralUpper = neutralUpper;
        this.elementalLower = elementalLower;
        this.elementalUpper = elementalUpper;

        this.neutralLowerCrit = neutralLowerCrit;
        this.neutralUpperCrit = neutralUpperCrit;
        this.elementalLowerCrit = elementalLowerCrit;
        this.elementalUpperCrit = elementalUpperCrit;

        this.critChance = critChance;
        spell = true;
    }

    public int dps() {
        double damageNormal = neutralLower + neutralUpper;
        double damageCrit = neutralLowerCrit + neutralUpperCrit;
        for (int i = 0; i < elementalLower.length; i++) {
            damageNormal += elementalLower[i] + elementalUpper[i];
            damageCrit += elementalLowerCrit[i] + elementalUpperCrit[i];
        }

        damageNormal /= 2;
        damageCrit /= 2;

        if (spell) {
            return (int) ((damageNormal * (1 - critChance)) + (damageCrit * (critChance)));
        } else {
            return 0;
        }
    }
}
