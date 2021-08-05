package apple.build.sql.indexdb;

import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.BuildConstraintBuilder;
import apple.build.search.constraints.ConstraintSimplified.ConstraintSimplifiedName;
import apple.build.search.enums.ElementSkill;
import apple.build.utils.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

class SearchConstraints {
    final long id;
    private Map<ConstraintSimplifiedName, List<BuildConstraint>> constraints = new HashMap<>();
    private Set<ElementSkill> archetype;

    SearchConstraints(long id) {
        this.id = id;
    }

    public void addConstraint(ResultSet response) {
        try {
            Pair<ConstraintSimplifiedName, BuildConstraint> constraint = BuildConstraintBuilder.build(response);
            constraints.putIfAbsent(constraint.getKey(), new ArrayList<>());
            constraints.get(constraint.getKey()).add(constraint.getValue());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * check if this search is more strict than the arguments
     *
     * @param constraints the constraints to check against
     * @return true if this search is less strict, otherwise if equal or greater strictness false
     */
    public boolean isLessStrict(List<BuildConstraint> constraints) {
        Map<ConstraintSimplifiedName, List<BuildConstraint>> constraintsOtherMap = new HashMap<>();
        for (BuildConstraint constraint : constraints) {
            ConstraintSimplifiedName name = constraint.getSimplifiedName();
            constraintsOtherMap.putIfAbsent(name, new ArrayList<>());
            constraintsOtherMap.get(name).add(constraint);
        }
        for (Map.Entry<ConstraintSimplifiedName, List<BuildConstraint>> constraintsMine : this.constraints.entrySet()) {
            List<BuildConstraint> constraintsOther = constraintsOtherMap.get(constraintsMine.getKey());
            // if the I have a constraint that they don't have that means im strictly more strict
            if (constraintsOther == null) continue;

            // try to return true for this constraintsMine
            for (BuildConstraint constraintMine : constraintsMine.getValue()) {
                // try to return true for this constraintMine
                boolean isMineLessStrict = false;
                for (BuildConstraint constraintOther : constraintsOther) {
                    if (constraintMine.isMoreStrict(constraintOther)) {
                        isMineLessStrict = true;
                        break;
                    }
                }
                if (!isMineLessStrict) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setArchetype(Set<ElementSkill> archetype) {
        this.archetype = archetype;
    }

    public boolean archetypeMatches(Set<ElementSkill> archetype) {
        for (ElementSkill element : this.archetype) {
            if (!archetype.contains(element)) return false;
        }
        return true;
    }

    public boolean isExact(List<BuildConstraint> constraints) {
        for (BuildConstraint constraint : constraints) {
            ConstraintSimplifiedName name = constraint.getSimplifiedName();
            List<BuildConstraint> myConstraints = this.constraints.get(name);
            if (myConstraints == null) return false;
            boolean isMatched = false;
            for (BuildConstraint myConstraint : myConstraints) {
                if (myConstraint.isExact(constraint)) {
                    isMatched = true;
                    break;
                }
            }
            if (!isMatched) return false;
        }
        return true;
    }
}
