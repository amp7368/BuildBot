package apple.build.search.constraints.answers;

public class DamageOutput {
    private final int neutralLower;
    private final int neutralUpper;
    private final int[] elementalLower;
    private final int[] elementalUpper;

    private final int neutralLowerCrit;
    private final int neutralUpperCrit;
    private final int[] elementalLowerCrit;
    private final int[] elementalUpperCrit;

    private final float critChance;

    private final double weaponAttackSpeed;
    private final double raw;
    private int dps = -1;

    public DamageOutput(double neutralLower, double neutralUpper, double[] elementalLower, double[] elementalUpper,
                        double neutralLowerCrit, double neutralUpperCrit, double[] elementalLowerCrit, double[] elementalUpperCrit,
                        float critChance, double rawSpell) {
        this.raw = rawSpell;
        this.neutralLower = (int) neutralLower;
        this.neutralUpper = (int) neutralUpper;
        int length = elementalLower.length;
        this.elementalLower = new int[length];
        this.elementalUpper = new int[length];

        this.neutralLowerCrit = (int) neutralLowerCrit;
        this.neutralUpperCrit = (int) neutralUpperCrit;
        this.elementalLowerCrit = new int[length];
        this.elementalUpperCrit = new int[length];
        for (int i = 0; i < length; i++) {
            this.elementalLower[i] = (int) elementalLower[i];
            this.elementalUpper[i] = (int) elementalUpper[i];
            this.elementalLowerCrit[i] = (int) elementalLowerCrit[i];
            this.elementalUpperCrit[i] = (int) elementalUpperCrit[i];
        }
        this.critChance = critChance;
        this.weaponAttackSpeed = -1;
    }

    public DamageOutput(double neutralLower, double neutralUpper, double[] elementalLower, double[] elementalUpper,
                        double neutralLowerCrit, double neutralUpperCrit, double[] elementalLowerCrit, double[] elementalUpperCrit,
                        float critChance, double rawMain, double attackSpeed) {
        this.raw = rawMain;
        this.neutralLower = (int) neutralLower;
        this.neutralUpper = (int) neutralUpper;
        int length = elementalLower.length;
        this.elementalLower = new int[length];
        this.elementalUpper = new int[length];

        this.neutralLowerCrit = (int) neutralLowerCrit;
        this.neutralUpperCrit = (int) neutralUpperCrit;
        this.elementalLowerCrit = new int[length];
        this.elementalUpperCrit = new int[length];
        for (int i = 0; i < length; i++) {
            this.elementalLower[i] = (int) elementalLower[i];
            this.elementalUpper[i] = (int) elementalUpper[i];
            this.elementalLowerCrit[i] = (int) elementalLowerCrit[i];
            this.elementalUpperCrit[i] = (int) elementalUpperCrit[i];
        }

        this.critChance = critChance;
        this.weaponAttackSpeed = attackSpeed;
    }

    public int dps() {
        if (dps == -1) {
            double damageNormal = neutralLower + neutralUpper;
            double damageCrit = neutralLowerCrit + neutralUpperCrit;
            for (int i = 0; i < elementalLower.length; i++) {
                damageNormal += elementalLower[i] + elementalUpper[i];
                damageCrit += elementalLowerCrit[i] + elementalUpperCrit[i];
            }

            damageNormal /= 2;
            damageCrit /= 2;
            double dmg = ((damageNormal * (1 - critChance)) + (damageCrit * (critChance)))+raw;
            if (weaponAttackSpeed == -1) {
                dps = (int) dmg;
            } else {
                dps = (int) (dmg * weaponAttackSpeed);
            }
        }
        return dps;
    }
}
