package apple.build.data.constraints.answers;

import apple.build.data.ElementSkill;

public class DamageInput {
    public final double spellDamage;
    public final double mainDamage;
    public final int spellDamageRaw;
    public final int mainDamageRaw;
    public final int dexterity;
    public final int strength;
    public final int[] skills;
    public final double[] elemental;
    public final double attackSpeedModifier;
    public boolean hawkeye = false;

    public DamageInput(double spellDamage, double mainDamage, int spellDamageRaw, int mainDamageRaw, int[] skills, int extraSkillPoints, double[] elemental, double attackSpeedModifier) {
        this.spellDamage = spellDamage;
        for (int i = 0; i < skills.length; i++) {
            skills[i] = Math.max(0, skills[i] + extraSkillPoints); // we have to include extra skill points as if it was added to all of them just because we don't know where to assign them
        }
        int i = 0;
        int strength = 0;
        int dexterity = 0;
        for (ElementSkill skill : ElementSkill.values()) {
            if (skill.skill.equals("strength")) {
                strength += skills[i];
                break;
            } else if (skill.skill.equals("dexterity")) {
                dexterity += skills[i];
            }
            i++;
        }
        this.strength = strength;
        this.dexterity = dexterity;
        this.mainDamage = mainDamage;
        this.spellDamageRaw = spellDamageRaw;
        this.mainDamageRaw = mainDamageRaw;
        this.skills = skills;
        this.elemental = elemental;
        this.attackSpeedModifier = attackSpeedModifier;
    }

    public void setHawkeye(boolean hawkeye) {
        this.hawkeye = hawkeye;
    }
}
