package apple.build.search.enums;

public enum SpellPart {
    PART_ARROW_STORM(6, .25, 0, 0, 0, 0.15),
    PART_ESCAPE(1, 0, 0.5, 0, 0, 0),
    PART_BOMB_ARROW(2.5, 0, 0, 0.25, 0, 0.15),
    PART_ARROW_SHIELD(1, 0, 0.3, 0, 0.3, 0),
    PART_BASH(1.3, 0, 0, .4, 0, 0),
    PART_CHARGE(1.5, 0, 0, 0, 0, 0.4),
    PART1_UPPERCUT(3, .1, 0, .2, 0, 0),
    PART2_UPPERCUT(.5, .4, 0, 0, 0, 0),
    PART3_UPPERCUT(.5, .20, 0, 0, 0, 0),
    PART_WAR_SCREAM(0.5, 0, .25, 0, 0, .75),
    PART_HEAL(0, 0, 0, 0, 0, 0),
    PART_TELEPORT(1, 0.4, 0, 0, 0, 0),
    PART_METEOR(5, 0, 0, .3, 0, .3),
    PART_ICE_SNAKE(.7, 0, 0, 0, .5, 0),
    PART_SPIN_ATTACK(1.5, .3, 0, 0, 0, 0),
    PART_VANISH(0, 0, 0, 0, 0, 0),
    PART1_MULTIHIT(2.7, 0, 0, 0, 0, 0),
    PART2_MULTIHIT(1.2, .3, 0, 0, .15, 0),
    PART_SMOKE_BOMB(6, 0, .25, .25, 0, 0),
    PART_TOTEM(1, 0, 0, 0, 0, .2), // there is a second part to totem
    PART_HAUL(1, .2, 0, 0, 0, 0),
    PART_AURA(2, 0, 0, 0, .3, 0),
    PART_UPROOT(1, 0, 0, .3, 0, 0),
    HAWKEYE(8, .25, 0, 0, 0, .15);


    public final double damage; // the base spell dmg
    public final double[] elemental; // the extra thunder

    SpellPart(double damage, double thunder, double air, double earth, double water, double fire) {
        this.damage = damage;
        elemental = new double[ElementSkill.values().length];
        int i = 0;
        for (ElementSkill element : ElementSkill.values()) {
            switch (element) {
                case THUNDER -> elemental[i] = thunder;
                case AIR -> elemental[i] = air;
                case EARTH -> elemental[i] = earth;
                case WATER -> elemental[i] = water;
                case FIRE -> elemental[i] = fire;
            }
            i++;
        }
    }
}
