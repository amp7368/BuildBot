package apple.build.search.constraints;


import apple.build.search.constraints.advanced_damage.ConstraintMainDamage;
import apple.build.search.constraints.advanced_damage.ConstraintSpellDamage;
import apple.build.search.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.search.constraints.filter.BuildConstraintExclusion;
import apple.build.search.constraints.general.*;
import apple.build.utils.Pair;
import apple.build.search.constraints.ConstraintSimplified.ConstraintSimplifiedName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiFunction;

public class BuildConstraintBuilder {
    public static Pair<ConstraintSimplifiedName, BuildConstraint> build(ResultSet response) throws SQLException {
        String text = response.getString("text_val");
        int val = response.getInt("val");
        BiFunction<String, Integer, BuildConstraint> create;
        ConstraintSimplifiedName constraintType = ConstraintSimplifiedName.valueOf(response.getString("name"));
        switch (constraintType) {
            case CONSTRAINT_JOINED_ID:
                create = ConstraintJoinedId::new;
                break;
            case CONSTRAINT_ID:
                create = ConstraintId::new;
                break;
            case CONSTRAINT_HPR:
                create = ConstraintHpr::new;
                break;
            case CONSTRAINT_HP:
                create = ConstraintHp::new;
                break;
            case CONSTRAINT_DEFENSE:
                create = ConstraintDefense::new;
                break;
            case CONSTRAINT_EXCLUSION:
                create = BuildConstraintExclusion::new;
                break;
            case CONSTRAINT_SPELL_COST:
                create = ConstraintSpellCost::new;
                break;
            case CONSTRAINT_SPELL_DMG:
                create = ConstraintSpellDamage::new;
                break;
            case CONSTRAINT_MAIN_DMG:
                create = ConstraintMainDamage::new;
                break;
            case CONSTRAINT_MAJOR_ID:
                create = ConstraintMajorId::new;
                break;
            default:
                create = null;
                break;
        }
        return new Pair<>(constraintType, create.apply(text, val));
    }
}
