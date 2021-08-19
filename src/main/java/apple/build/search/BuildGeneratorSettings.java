package apple.build.search;

import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.advanced_damage.BuildConstraintAdvancedDamage;
import apple.build.search.constraints.advanced_skill.BuildConstraintAdvancedSkills;
import apple.build.search.constraints.filter.BuildConstraintExclusion;
import apple.build.search.constraints.general.BuildConstraintGeneral;
import apple.build.search.enums.ElementSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class BuildGeneratorSettings {
    private final List<BuildConstraintGeneral> constraintsGeneral;
    private final List<BuildConstraintExclusion> constraintsExclusion;
    private final List<BuildConstraintAdvancedSkills> constraintsAdvancedSkill;
    private final List<BuildConstraintAdvancedDamage> constraintsAdvancedDamage;
    private final Set<ElementSkill> archetype;
    private List<BuildConstraint> allConstraints = new ArrayList<>();

    private final AtomicLong maxTimeToStop = new AtomicLong();
    private AtomicLong desiredTimeToStop = new AtomicLong();

    public BuildGeneratorSettings(Set<ElementSkill> archetype) {
        this.constraintsGeneral = new ArrayList<>();
        this.constraintsAdvancedSkill = new ArrayList<>();
        this.constraintsAdvancedDamage = new ArrayList<>();
        this.constraintsExclusion = new ArrayList<>();
        this.archetype = archetype;
    }

    public void addConstraint(BuildConstraintGeneral constraint) {
        this.constraintsGeneral.add(constraint);
        this.allConstraints.add(constraint);
    }

    public void addConstraint(BuildConstraintAdvancedSkills constraint) {
        this.constraintsAdvancedSkill.add(constraint);
        this.allConstraints.add(constraint);
    }

    public void addConstraint(BuildConstraintAdvancedDamage constraint) {
        this.constraintsAdvancedDamage.add(constraint);
        this.allConstraints.add(constraint);
    }

    public void addConstraint(BuildConstraintExclusion constraint) {
        this.constraintsExclusion.add(constraint);
        this.allConstraints.add(constraint);
    }

    public Iterable<? extends BuildConstraint> getConstraintsAll() {
        return allConstraints;
    }

    public List<BuildConstraintGeneral> getConstraintsGeneral() {
        return constraintsGeneral;
    }

    public List<BuildConstraintExclusion> getConstraintsExclusion() {
        return constraintsExclusion;
    }

    public List<BuildConstraintAdvancedSkills> getConstraintsAdvancedSkill() {
        return constraintsAdvancedSkill;
    }

    public List<BuildConstraintAdvancedDamage> getConstraintsAdvancedDamage() {
        return constraintsAdvancedDamage;
    }

    public List<BuildConstraint> getAllConstraints() {
        return allConstraints;
    }

    public AtomicLong getMaxTimeToStop() {
        return maxTimeToStop;
    }

    public AtomicLong getDesiredTimeToStop() {
        return desiredTimeToStop;
    }

    public Set<ElementSkill> getArchetype() {
        return archetype;
    }
}
