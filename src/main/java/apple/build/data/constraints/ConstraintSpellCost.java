package apple.build.data.constraints;

public class ConstraintSpellCost {
    public enum Spells {
        ARROW_STORM(6, 6d, 0d, 0d, 0d, 0d, 0.15),
        ESCAPE,
        BOMB_ARROW,
        ARROW_SHIELD,
        BASH,
        CHARGE,
        UPPERCUT,
        WAR_SCREAM,
        HEAL,
        TELEPORT,
        METEOR,
        ICE_SNAKE,
        SPIN_ATTACK,
        VANISH,
        MULTIHIT,
        SMOKE_BOMB,
        TOTEM,
        HAUL,
        AURA,
        UPROOT;

        private final int mana;

        Spells(int mana, double damage, double thunder, double air, double earth, double water, double fire) {
            this.mana = mana;
            this.damage = damage;
            this.thunder = thunder;
            this.air = air;
            this.earth = earth;
            this.water = water;
            this.fire = fire;
        }
    }
}
