package apple.build.data.enums;

public enum Spell {
    ARROW_STORM(6, 1, 6, .25, 0, 0, 0, 0.15),
    ESCAPE(3, 2, 1, 0, 0.5, 0, 0, 0),
    BOMB_ARROW(8, 3, 2.5, 0, 0, 0.25, 0, 0.15),
    ARROW_SHIELD(10, 4, 1, 0, 0.3, 0, 0.3, 0),
    BASH(6, 1, 1.3, 0, 0, .4, 0, 0),
    CHARGE(4, 2, 1.5, 0, 0, 0, 0, 0.4),
    UPPERCUT(8, 3, 4, .25, 0, .15, 0, 0),
    WAR_SCREAM(5, 4, 0.3, 0, .25, 0, 0, .75),
    HEAL(6, 1, 0, 0, 0, 0, 0, 0),
    TELEPORT(4, 2, 1, 0.4, 0, 0, 0, 0),
    METEOR(8, 3, 5, 0, 0, .3, 0, .3),
    ICE_SNAKE(4, 4, .7, 0, 0, 0, .5, 0),
    SPIN_ATTACK(6, 1, 1.5, .3, 0, 0, 0, 0),
    VANISH(1, 2, 0, 0, 0, 0, 0, 0),
    MULTIHIT(8, 3, 3, .3, 0, 0, .3, 0),
    SMOKE_BOMB(8, 4, 3, 0, .25, .25, 0, 0),
    TOTEM(4, 1, .2, 0, .2, 0, 0, .2),
    HAUL(1, 2, 1, 0, 0, 0, 0, 0),
    AURA(8, 3, 2, 0, 0, 0, .3, 0),
    UPROOT(6, 4, .75, 0, 0, .3, 0, 0);

    public final int mana; // the base mana cost
    public final int spellNum; // the spell # (1,2,3,4)
    public final double damage; // the base spell dmg
    public final double[] elemental; // the extra thunder

    Spell(int mana, int spellNum, double damage, double thunder, double air, double earth, double water, double fire) {
        this.mana = mana;
        this.spellNum = spellNum;
        this.damage = damage;
        elemental = new double[ElementSkill.values().length];
        int i = 0;
        for (ElementSkill element : ElementSkill.values()) {
            switch (element) {
                case THUNDER:
                    elemental[i] = thunder;
                    break;
                case AIR:
                    elemental[i] = air;
                    break;
                case EARTH:
                    elemental[i] = earth;
                    break;
                case WATER:
                    elemental[i] = water;
                    break;
                case FIRE:
                    elemental[i] = fire;
                    break;
            }
            i++;
        }
    }
}
