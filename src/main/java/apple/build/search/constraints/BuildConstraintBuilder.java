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
        ConstraintSimplifiedName constraintType = ConstraintSimplifiedName.valueOf(response.getString("name"));
        return new Pair<>(constraintType, constraintType.getCreate().apply(text, val));
    }
}
