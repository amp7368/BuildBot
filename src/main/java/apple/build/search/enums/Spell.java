package apple.build.search.enums;

import apple.build.utils.Pretty;

import static apple.build.search.enums.SpellPart.*;

public enum Spell {
    ARROW_STORM(6, 1, PART_ARROW_STORM),
    ESCAPE(3, 2, PART_ESCAPE),
    BOMB_ARROW(8, 3, PART_BOMB_ARROW),
    ARROW_SHIELD(10, 4, PART_ARROW_SHIELD),
    BASH(6, 1, PART_BASH, PART_BASH),
    CHARGE(4, 2, PART_CHARGE),
    UPPERCUT(8, 3, PART1_UPPERCUT, PART2_UPPERCUT, PART3_UPPERCUT),
    WAR_SCREAM(5, 4, PART_WAR_SCREAM),
    HEAL(6, 1, PART_HEAL),
    TELEPORT(4, 2, PART_TELEPORT),
    METEOR(8, 3, PART_METEOR),
    ICE_SNAKE(4, 4, PART_ICE_SNAKE),
    SPIN_ATTACK(6, 1, PART_SPIN_ATTACK),
    VANISH(1, 2, PART_VANISH),
    MULTIHIT(8, 3, PART1_MULTIHIT, PART2_MULTIHIT),
    SMOKE_BOMB(8, 4, PART_SMOKE_BOMB),
    TOTEM(4, 1, PART_TOTEM),
    HAUL(1, 2, PART_HAUL),
    AURA(8, 3, PART_AURA),
    UPROOT(6, 4, PART_UPROOT);

    public final int mana; // the base mana cost
    public final int spellNum; // the spell # (1,2,3,4)
    public SpellPart[] spellParts;
    private double totalDamage = 0;

    Spell(int mana, int spellNum, SpellPart... spellParts) {
        this.mana = mana;
        this.spellNum = spellNum;
        this.spellParts = spellParts;
        for (SpellPart spellPart : spellParts) {
            this.totalDamage += spellPart.damage;
        }
    }

    public String prettyName() {
        return Pretty.uppercaseFirst(this.name());
    }

    public double getTotalDamage() {
        return totalDamage;
    }
}
