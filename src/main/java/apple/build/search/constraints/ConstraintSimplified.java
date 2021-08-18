package apple.build.search.constraints;

import apple.build.search.constraints.advanced_damage.ConstraintMainDamage;
import apple.build.search.constraints.advanced_damage.ConstraintSpellDamage;
import apple.build.search.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.search.constraints.filter.BuildConstraintExclusion;
import apple.build.search.constraints.general.*;

import java.util.function.BiFunction;

public class ConstraintSimplified {
    private final ConstraintSimplifiedName type;
    public String text = null;
    public int val = Integer.MIN_VALUE;

    public ConstraintSimplified(ConstraintSimplifiedName type) {
        this.type = type;
    }

    public String getValSql() {
        return val == Integer.MIN_VALUE ? null : String.valueOf(val);
    }

    public enum ConstraintSimplifiedName {
        CONSTRAINT_JOINED_ID(ConstraintJoinedId::new),
        CONSTRAINT_ID(ConstraintId::new),
        CONSTRAINT_HPR(ConstraintHpr::new),
        CONSTRAINT_HP(ConstraintHp::new),
        CONSTRAINT_DEFENSE(ConstraintDefense::new),
        CONSTRAINT_EXCLUSION(BuildConstraintExclusion::new),
        CONSTRAINT_SPELL_COST(ConstraintSpellCost::new),
        CONSTRAINT_SPELL_DMG(ConstraintSpellDamage::new),
        CONSTRAINT_MAIN_DMG(ConstraintMainDamage::new),
        CONSTRAINT_MAJOR_ID(ConstraintMajorId::new),
        CONSTRAINT_MIN_ATTACK_SPEED(ConstraintMinAttackSpeed::new);
        private BiFunction<String, Integer, BuildConstraint> create;

        ConstraintSimplifiedName(BiFunction<String, Integer, BuildConstraint> create) {

            this.create = create;
        }

        public BiFunction<String, Integer, BuildConstraint> getCreate() {
            return create;
        }
    }

    public String getName() {
        return type.name();
    }
}
