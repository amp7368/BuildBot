package apple.build;

import apple.build.data.Build;
import apple.build.data.BuildGenerator;
import apple.build.data.constraints.general.ConstraintDefense;
import apple.build.data.enums.Spell;
import apple.build.data.constraints.filter.BuildConstraintExclusion;
import apple.build.data.enums.ElementSkill;
import apple.build.data.constraints.advanced_damage.ConstraintMainDamage;
import apple.build.data.constraints.advanced_damage.ConstraintSpellDamage;
import apple.build.data.constraints.general.ConstraintHp;
import apple.build.data.constraints.general.ConstraintHpr;
import apple.build.data.constraints.general.ConstraintId;
import apple.build.data.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.sql.GetDB;
import apple.build.sql.VerifyDB;
import apple.build.wynncraft.items.Item;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.*;

public class BuildMain {

    public static final double NEGATIVE_MAX_ROLL = 0.7;
    public static final double POSITIVE_MAX_ROLL = 1.3;
    public static List<Item> helmets;
    public static List<Item> chestplates;
    public static List<Item> leggings;
    public static List<Item> boots;
    public static List<Item> rings;
    public static List<Item> bracelets;
    public static List<Item> necklaces;
    public static List<Item> bows;
    public static List<Item> spears;
    public static List<Item> daggers;
    public static List<Item> wands;
    public static List<Item> reliks;

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
//        PrintStream out = new PrintStream(new File("data/out.out"));
//        System.setOut(out);
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
        helmets = GetDB.getAllItems(Item.ItemType.HELMET);
        chestplates = GetDB.getAllItems(Item.ItemType.CHESTPLATE);
        leggings = GetDB.getAllItems(Item.ItemType.LEGGINGS);
        boots = GetDB.getAllItems(Item.ItemType.BOOTS);
        rings = GetDB.getAllItems(Item.ItemType.RING);
        bracelets = GetDB.getAllItems(Item.ItemType.BRACELET);
        necklaces = GetDB.getAllItems(Item.ItemType.NECKLACE);
        bows = GetDB.getAllItems(Item.ItemType.BOW);
        spears = GetDB.getAllItems(Item.ItemType.SPEAR);
        daggers = GetDB.getAllItems(Item.ItemType.DAGGER);
        wands = GetDB.getAllItems(Item.ItemType.WAND);
        reliks = GetDB.getAllItems(Item.ItemType.RELIK);
        helmets.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        chestplates.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        leggings.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        boots.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        rings.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        bracelets.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        necklaces.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        bows.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        spears.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        daggers.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        wands.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        reliks.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
    }

    private static void combinations() throws SQLException {
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

        long start = System.currentTimeMillis();
        BuildGenerator builds = benchmark();
        finish(builds, start);
    }

    /**
     * @return https://wynndata.tk/s/t9vo23
     */
    private static BuildGenerator testMajorIds() {
        List[] allItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, daggers};
        BuildGenerator builds = new BuildGenerator(allItems);
        builds.addConstraint(new ConstraintHpr(590));
        builds.addConstraint(new ConstraintId("speed", 100));
        builds.addConstraint(new ConstraintId("manaSteal", 6));
        builds.addConstraint(new ConstraintId("attackSpeedBonus", 1));
        builds.addConstraint(new ConstraintId("damageBonusRaw", 2000));
        builds.addConstraint(new ConstraintId("bonusAirDamage", 100));
        builds.addConstraint(new ConstraintMainDamage(12900));
        builds.addConstraint(new ConstraintSpellCost(Spell.SMOKE_BOMB, 3));
        builds.addConstraint(new ConstraintSpellDamage(Spell.SMOKE_BOMB, 9900));
        builds.addConstraint(new ConstraintHp(14500));
        for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
            builds.addConstraint(exclusion);
        builds.generate(new HashSet<>() {{
            add(ElementSkill.EARTH);
            add(ElementSkill.FIRE);
            add(ElementSkill.AIR);
        }});
        return builds;
    }

    /**
     * times:
     * nothing === Total time: 64572 || Size: 3227
     * defense === Total time: 65015 || Size: 1
     * defenseOptimized === Total time: 13541 || Size: 3
     *
     * @return https://wynndata.tk/s/ykeag2
     */
    private static BuildGenerator benchmark() {
        List[] allItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, bows};
        BuildGenerator builds = new BuildGenerator(allItems);
        builds.addConstraint(new ConstraintHpr(0));
        builds.addConstraint(new ConstraintId("manaSteal", 14));
        builds.addConstraint(new ConstraintId("attackSpeedBonus", -4));
        builds.addConstraint(new ConstraintId("damageBonusRaw", 1745));
        builds.addConstraint(new ConstraintId("bonusThunderDamage", 102));
        builds.addConstraint(new ConstraintId("spellDamage", 68));
        builds.addConstraint(new ConstraintId("spellDamageRaw", 835));
        builds.addConstraint(new ConstraintDefense(ElementSkill.EARTH, -100));
        builds.addConstraint(new ConstraintDefense(ElementSkill.THUNDER, -100));
        builds.addConstraint(new ConstraintDefense(ElementSkill.WATER, -100));
        builds.addConstraint(new ConstraintDefense(ElementSkill.FIRE, -100));
        builds.addConstraint(new ConstraintDefense(ElementSkill.AIR, -100));
        builds.addConstraint(new ConstraintMainDamage(4600));
        builds.addConstraint(new ConstraintSpellDamage(Spell.ARROW_STORM, 47122));
        builds.addConstraint(new ConstraintSpellCost(Spell.ARROW_STORM, 1));
        builds.addConstraint(new ConstraintHp(12500));
        for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
            builds.addConstraint(exclusion);
        builds.generate(new HashSet<>() {{
            add(ElementSkill.THUNDER);
            add(ElementSkill.WATER);
            add(ElementSkill.FIRE);
        }});
        return builds;
    }

    /**
     * TODO
     *
     * @return https://wynndata.tk/s/ol8ktd
     */
    private static BuildGenerator test() {
        List[] allItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, bows};
        BuildGenerator builds = new BuildGenerator(allItems);
        builds.addConstraint(new ConstraintHpr(0));
        builds.addConstraint(new ConstraintId("manaSteal", 11));
        builds.addConstraint(new ConstraintId("bonusThunderDamage", 172));
        builds.addConstraint(new ConstraintId("spellDamage", 83));
        builds.addConstraint(new ConstraintDefense(ElementSkill.EARTH, -250));
        builds.addConstraint(new ConstraintDefense(ElementSkill.THUNDER, 275));
        builds.addConstraint(new ConstraintDefense(ElementSkill.WATER, -190));
        builds.addConstraint(new ConstraintDefense(ElementSkill.FIRE, 180));
        builds.addConstraint(new ConstraintDefense(ElementSkill.AIR, 0));
        builds.addConstraint(new ConstraintSpellCost(Spell.ARROW_STORM, 2));
        builds.addConstraint(new ConstraintSpellCost(Spell.BOMB_ARROW, 3));
        builds.addConstraint(new ConstraintSpellCost(Spell.ARROW_SHIELD, 4));
        builds.addConstraint(new ConstraintSpellDamage(Spell.ARROW_STORM, 55000));
        builds.addConstraint(new ConstraintHp(11500));
        for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
            builds.addConstraint(exclusion);
        builds.generate(new HashSet<>() {{
            add(ElementSkill.THUNDER);
            add(ElementSkill.WATER);
            add(ElementSkill.FIRE);
        }});
        return builds;
    }

    /**
     * TODO
     *
     * @return https://www.wynndata.tk/s/wj4zbg
     */
    private static BuildGenerator wfaNeptaSpellSpam() {
        List[] allItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, wands};
        BuildGenerator builds = new BuildGenerator(allItems);
        builds.addConstraint(new ConstraintId("manaRegen", 11));
        builds.addConstraint(new ConstraintId("bonusWaterDamage", 76));
        builds.addConstraint(new ConstraintId("spellDamage", 105));
        builds.addConstraint(new ConstraintId("spellDamageRaw", 850));
        builds.addConstraint(new ConstraintSpellCost(Spell.METEOR, 1));
        builds.addConstraint(new ConstraintSpellDamage(Spell.METEOR, 15500));
        builds.addConstraint(new ConstraintHp(12000));
        for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
            builds.addConstraint(exclusion);
        builds.generate(new HashSet<>() {{
            add(ElementSkill.EARTH);
            add(ElementSkill.WATER);
            add(ElementSkill.FIRE);
        }});
        return builds;
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
