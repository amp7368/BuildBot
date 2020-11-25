package apple.build;

import apple.build.data.BuildGenerator;
import apple.build.data.constraints.advanced_damage.ConstraintSpellDamage;
import apple.build.data.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.data.constraints.filter.BuildConstraintExclusion;
import apple.build.data.constraints.general.ConstraintHp;
import apple.build.data.constraints.general.ConstraintHpr;
import apple.build.data.constraints.general.ConstraintId;
import apple.build.data.enums.ElementSkill;
import apple.build.data.enums.Spell;
import apple.build.wynncraft.items.Item;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    public static void preIndex() throws IOException {
        helmets.removeIf(item -> item.level < 80);
        chestplates.removeIf(item -> item.level < 80);
        leggings.removeIf(item -> item.level < 80);
        boots.removeIf(item -> item.level < 80);
        List[] bowItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, wands};
        List<Set<ElementSkill>> archetypes = Arrays.asList(EWF, EWA, TWF, TWA, WFA);
        for (Set<ElementSkill> archetype : archetypes) {
            BuildGenerator builds = new BuildGenerator(bowItems);
            builds.addConstraint(new ConstraintHpr(0));
            builds.addConstraint(new ConstraintId("speed", 0));
            builds.addConstraint(new ConstraintId("manaRegen", 12));
            builds.addConstraint(new ConstraintSpellDamage(Spell.METEOR, 10000));
            builds.addConstraint(new ConstraintSpellCost(Spell.METEOR, 2));
            builds.addConstraint(new ConstraintHp(11000));
            for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
                builds.addConstraint(exclusion);
            builds.generate(archetype);
            Set<Item>[] items = builds.getItemsInBuilds();
            if (items == null) continue;
            File file = new File("searches/" + archetype.stream().map(Enum::name).collect(Collectors.joining(""))+".search");
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writeItems(writer, items);
            writer.close();
        }
    }

    private static void writeItems(BufferedWriter writer, Set<Item>[] items) throws IOException {
        for (Set<Item> item : items) {
            writer.write(item.stream().map(Item::toString).collect(Collectors.joining(",")) + "\n");
        }
    }
}
