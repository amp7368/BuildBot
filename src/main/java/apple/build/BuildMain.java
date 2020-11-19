package apple.build;

import apple.build.data.Build;
import apple.build.data.BuildGenerator;
import apple.build.data.constraints.advanced_defense.ConstraintDefense;
import apple.build.data.constraints.filter.BuildConstraintExclusion;
import apple.build.data.ElementSkill;
import apple.build.data.constraints.advanced_damage.ConstraintMainDamage;
import apple.build.data.constraints.advanced_damage.ConstraintSpellDamage;
import apple.build.data.constraints.general.ConstraintHp;
import apple.build.data.constraints.general.ConstraintHpr;
import apple.build.data.constraints.general.ConstraintId;
import apple.build.data.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.sql.GetDB;
import apple.build.sql.VerifyDB;
import apple.build.wynncraft.items.Item;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class BuildMain {

    public static final double NEGATIVE_MAX_ROLL = 0.7;
    public static final double POSITIVE_MAX_ROLL = 1.3;

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        System.out.println("Starting BuildBot");
        initialize();
        System.out.println("Opened database successfully");
//        GetItems.getItems();
//        System.out.println("Inserted items");

        combinations();
        System.out.println("done");
    }

    public static void initialize() throws SQLException, ClassNotFoundException {
        VerifyDB.initialize();
        List<Item> i = GetDB.getAllItems(Item.ItemType.HELMET);
        i = GetDB.getAllItems(Item.ItemType.CHESTPLATE);
        i = GetDB.getAllItems(Item.ItemType.LEGGINGS);
        i = GetDB.getAllItems(Item.ItemType.BOOTS);
        i = GetDB.getAllItems(Item.ItemType.RING);
        i = GetDB.getAllItems(Item.ItemType.BRACELET);
        i = GetDB.getAllItems(Item.ItemType.NECKLACE);
        i = GetDB.getAllItems(Item.ItemType.BOW);
        i = GetDB.getAllItems(Item.ItemType.SPEAR);
        i = GetDB.getAllItems(Item.ItemType.DAGGER);
        i = GetDB.getAllItems(Item.ItemType.WAND);
        i = GetDB.getAllItems(Item.ItemType.RELIK);
    }

    private static void combinations() throws SQLException {
        List<Item> helmets = GetDB.getAllItems(Item.ItemType.HELMET);
        helmets.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> chestplates = GetDB.getAllItems(Item.ItemType.CHESTPLATE);
        chestplates.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> leggings = GetDB.getAllItems(Item.ItemType.LEGGINGS);
        leggings.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> boots = GetDB.getAllItems(Item.ItemType.BOOTS);
        boots.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> rings = GetDB.getAllItems(Item.ItemType.RING);
        rings.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> bracelets = GetDB.getAllItems(Item.ItemType.BRACELET);
        bracelets.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> necklaces = GetDB.getAllItems(Item.ItemType.NECKLACE);
        necklaces.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> weapon = GetDB.getAllItems(Item.ItemType.BOW);
        weapon.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        Item mine = null;
        String name = "tenut";
        for (Item i : necklaces) {
            if (i.name.toLowerCase().contains(name) || i.displayName.toLowerCase().contains(name)) {
                mine = i;
                break;
            }
        }
        helmets.removeIf(item -> item.level < 80);
        chestplates.removeIf(item -> item.level < 80);
        leggings.removeIf(item -> item.level < 80);
        boots.removeIf(item -> item.level < 80);

//        helmets.removeIf(item -> !item.name.equals("Sizzling Shawl"));
//        chestplates.removeIf(item -> !item.name.equals("Boreal-Patterned Aegis"));
//        leggings.removeIf(item -> !item.name.equals("Leggings of Desolation"));
//        boots.removeIf(item -> !item.name.equals("Resurgence"));
//        rings.removeIf(item -> !item.name.equals("Gold Static Ring") && !item.name.equals("Diamond Static Ring"));
//        bracelets.removeIf(item -> !item.name.equals("Diamond Static Bracelet"));
//        necklaces.removeIf(item -> !item.name.equals("Tenuto"));
//        weapon.removeIf(item -> !item.name.equals("Ignis"));
        List[] allItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, weapon};
        BuildGenerator builds = new BuildGenerator(allItems);

        long start = System.currentTimeMillis();
        test(builds);
        finish(builds, start);
    }

    private static void test(BuildGenerator builds) {
        builds.addConstraint(new ConstraintHpr(590));
        builds.addConstraint(new ConstraintHp(14500));
        builds.addConstraint(new ConstraintId("speed", 100));
        builds.addConstraint(new ConstraintMainDamage(11111));
        builds.addConstraint(new ConstraintSpellCost(ConstraintSpellCost.Spell.SMOKE_BOMB, 3));
        builds.addConstraint(new ConstraintSpellDamage(ConstraintSpellCost.Spell.SMOKE_BOMB, 7000));
        for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
            builds.addConstraint(exclusion);
        builds.generate(new HashSet<>() {{
            add(ElementSkill.EARTH);
            add(ElementSkill.FIRE);
            add(ElementSkill.AIR);
        }});
    }

    private static void finish(BuildGenerator builds, long start) {
        int i = 0;
        for (Build build : builds.getBuilds()) {
            if (i++ == 400) break;
            System.out.println(build.toString());
        }
        System.out.println("Total time: " + (System.currentTimeMillis() - start) + " || Size: " + builds.size());
    }
}
