package apple.build;

import apple.build.search.BuildGenerator;
import apple.build.search.constraints.advanced_damage.ConstraintSpellDamage;
import apple.build.search.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.search.constraints.filter.BuildConstraintExclusion;
import apple.build.search.constraints.general.ConstraintHp;
import apple.build.search.constraints.general.ConstraintHpr;
import apple.build.search.constraints.general.ConstraintId;
import apple.build.search.enums.ElementSkill;
import apple.build.search.enums.Spell;
import apple.build.sql.indexdb.InsertIndexDB;

import java.sql.SQLException;
import java.util.*;

import static apple.build.BuildMain.*;

public class Preindexing {

    private static final Set<ElementSkill> EWF = new HashSet<>() {{
        add(ElementSkill.EARTH);
        add(ElementSkill.WATER);
        add(ElementSkill.FIRE);
    }};
    private static final Set<ElementSkill> EWA = new HashSet<>() {{
        add(ElementSkill.EARTH);
        add(ElementSkill.WATER);
        add(ElementSkill.AIR);
    }};
    private static final Set<ElementSkill> TWF = new HashSet<>() {{
        add(ElementSkill.THUNDER);
        add(ElementSkill.WATER);
        add(ElementSkill.FIRE);
    }};
    private static final Set<ElementSkill> TWA = new HashSet<>() {{
        add(ElementSkill.THUNDER);
        add(ElementSkill.WATER);
        add(ElementSkill.AIR);
    }};
    private static final Set<ElementSkill> WFA = new HashSet<>() {{
        add(ElementSkill.WATER);
        add(ElementSkill.FIRE);
        add(ElementSkill.AIR);
    }};

    public static void preIndex() {
        helmets.removeIf(item -> item.level < 80);
        chestplates.removeIf(item -> item.level < 80);
        leggings.removeIf(item -> item.level < 80);
        boots.removeIf(item -> item.level < 80);
        List[] bowItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, wands};
        List<Set<ElementSkill>> archetypes = Arrays.asList(EWF, EWA, TWF, TWA, WFA);
        for (Set<ElementSkill> archetype : archetypes) {
            BuildGenerator builds = new BuildGenerator(bowItems,archetype);
            builds.addConstraint(new ConstraintHpr(0));
            builds.addConstraint(new ConstraintId("speed", 0));
            builds.addConstraint(new ConstraintId("manaRegen", 12));
            builds.addConstraint(new ConstraintSpellDamage(Spell.METEOR, 10000));
            builds.addConstraint(new ConstraintSpellCost(Spell.METEOR, 2));
            builds.addConstraint(new ConstraintHp(11000));
            for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
                builds.addConstraint(exclusion);
            builds.generate();
            try {
                InsertIndexDB.insertResults(builds);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public static void saveResult(BuildGenerator generator) {
        try {

            InsertIndexDB.insertResults(generator);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
