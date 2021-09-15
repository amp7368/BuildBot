package apple.build.search.constraints.answers;

import apple.build.search.enums.ElementSkill;
import org.jetbrains.annotations.Nullable;

public class DamageOutput {
    public final int neutralLower;
    public final int neutralUpper;
    private int[] skills;
    public final int[] elementalLower;
    public final int[] elementalUpper;

    public final int neutralLowerCrit;
    public final int neutralUpperCrit;
    public final int[] elementalLowerCrit;
    public final int[] elementalUpperCrit;

    private final float critChance;

    private final double weaponAttackSpeed;
    public final double raw;
    private double dps = -1;
    private int dpsNormal;
    private int dpsCrit;

    public DamageOutput(double neutralLower, double neutralUpper, double[] elementalLower, double[] elementalUpper,
                        double neutralLowerCrit, double neutralUpperCrit, double[] elementalLowerCrit, double[] elementalUpperCrit,
                        float critChance, double rawSpell, int[] skills) {
        this.raw = rawSpell;
        this.neutralLower = (int) neutralLower;
        this.neutralUpper = (int) neutralUpper;
        this.skills = skills;
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
                        float critChance, double rawMain, double attackSpeed, int[] skills) {
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

    private void verifyCalculated() {
        if (dps == -1) {
            double damageNormal = neutralLower + neutralUpper;
            double damageCrit = neutralLowerCrit + neutralUpperCrit;
            for (int i = 0; i < elementalLower.length; i++) {
                damageNormal += elementalLower[i] + elementalUpper[i];
                damageCrit += elementalLowerCrit[i] + elementalUpperCrit[i];
            }

            damageNormal /= 2;
            damageCrit /= 2;
            this.dpsNormal = (int) damageNormal;
            this.dpsCrit = (int) damageCrit;
            if (weaponAttackSpeed != -1) {
                dpsNormal = (int) (dpsNormal * weaponAttackSpeed);
                dpsCrit = (int) (dpsCrit * weaponAttackSpeed);
            }
            double dmg = ((damageNormal * (1 - critChance)) + (damageCrit * (critChance)));
            if (weaponAttackSpeed == -1) {
                dps = dmg;
            } else {
                dps = (dmg * weaponAttackSpeed);
            }
        }
    }

    public int dpsWithRaw() {
        verifyCalculated();
        return (int) (dps + raw);
    }

    public int dpsNoRaw() {
        verifyCalculated();
        return (int) (dps);
    }


    public int getDpsCrit() {
        verifyCalculated();
        return this.dpsCrit;
    }

    public int getDpsNormal() {
        verifyCalculated();
        return this.dpsNormal;
    }

    public double getCritChance() {
        return critChance;
    }

    public double getRaw() {
        return this.raw;
    }

    public int getLowerAvg(@Nullable ElementSkill element) {
        int normal, crit;
        if (element == null) {
            normal = neutralLower;
            crit = neutralLowerCrit;
        } else {
            normal = elementalLower[element.ordinal()];
            crit = elementalLowerCrit[element.ordinal()];
        }
        return (int) ((normal * (1 - critChance)) + (crit * (critChance)));
    }

    public int getUpperAvg(@Nullable ElementSkill element) {
        int normal, crit;
        if (element == null) {
            normal = neutralUpper;
            crit = neutralUpperCrit;
        } else {
            normal = elementalUpper[element.ordinal()];
            crit = elementalUpperCrit[element.ordinal()];
        }
        return (int) ((normal * (1 - critChance)) + (crit * (critChance)));
    }

    public int[] getSkills() {
        return skills;
    }
}
