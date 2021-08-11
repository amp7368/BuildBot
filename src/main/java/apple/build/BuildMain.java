package apple.build;

import apple.build.search.Build;
import apple.build.search.BuildGenerator;
import apple.build.search.constraints.advanced_damage.ConstraintMainDamage;
import apple.build.search.constraints.advanced_damage.ConstraintSpellDamage;
import apple.build.search.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.search.constraints.filter.BuildConstraintExclusion;
import apple.build.search.constraints.general.*;
import apple.build.search.enums.ElementSkill;
import apple.build.search.enums.Spell;
import apple.build.wynncraft.items.Item;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class BuildMain {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, LoginException {
        System.out.println("Starting BuildBot");
        initialize();
        System.out.println("Opened database successfully");
        combinations();
//        GetItems.getItems();
//        System.out.println("Inserted items");
        System.out.println("done");
    }

    public static void initialize() throws SQLException, ClassNotFoundException {

    }

    private static void combinations() {
        Item.helmets.removeIf(item -> item.level < 80);
        Item.chestplates.removeIf(item -> item.level < 80);
        Item.leggings.removeIf(item -> item.level < 80);
        Item.boots.removeIf(item -> item.level < 80);

        long start = System.currentTimeMillis();
        BuildGenerator builds = wfaNeptaSpellSpam();
        finish(builds, start);
    }

    /**
     * @return https://wynndata.tk/s/t9vo23
     */
    private static BuildGenerator testMajorIds() {
        ArrayList[] allItems = {Item.helmets, Item.chestplates, Item.leggings, Item.boots, new ArrayList<>(Item.rings), Item.rings, Item.bracelets, Item.necklaces, Item.daggers};
        BuildGenerator builds = new BuildGenerator(allItems, new HashSet<>() {{
            add(ElementSkill.EARTH);
            add(ElementSkill.FIRE);
            add(ElementSkill.AIR);
        }});
        builds.addConstraint(new ConstraintHpr(590));
        builds.addConstraint(new ConstraintId("speed", 100));
        builds.addConstraint(new ConstraintId("manaSteal", 6));
        builds.addConstraint(new ConstraintId("attackSpeedBonus", 1));
        builds.addConstraint(new ConstraintId("damageBonusRaw", 2000));
        builds.addConstraint(new ConstraintId("bonusAirDamage", 100));
        builds.addConstraint(new ConstraintMajorId("GREED"));
        builds.addConstraint(new ConstraintMajorId("MAGNET"));
        builds.addConstraint(new ConstraintMainDamage(12000));
        builds.addConstraint(new ConstraintSpellCost(Spell.SMOKE_BOMB, 3));
        builds.addConstraint(new ConstraintSpellDamage(Spell.SMOKE_BOMB, 9800));
        builds.addConstraint(new ConstraintHp(14500));
        for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
            builds.addConstraint(exclusion);
        builds.generate();
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
        ArrayList[] allItems = {Item.helmets, Item.chestplates, Item.leggings, Item.boots, new ArrayList<>(Item.rings), Item.rings, Item.bracelets, Item.necklaces, Item.bows};
        BuildGenerator builds = new BuildGenerator(allItems, new HashSet<>() {{
            add(ElementSkill.THUNDER);
            add(ElementSkill.WATER);
            add(ElementSkill.FIRE);
        }});
        builds.addConstraint(new ConstraintHpr(0));
        builds.addConstraint(new ConstraintId("manaSteal", 14));
        builds.addConstraint(new ConstraintId("attackSpeedBonus", -4));
//        builds.addConstraint(new ConstraintId("damageBonusRaw", 1745));
//        builds.addConstraint(new ConstraintId("bonusThunderDamage", 102));
//        builds.addConstraint(new ConstraintId("spellDamage", 68));
        builds.addConstraint(new ConstraintJoinedId(Arrays.asList("bonusThunderDamage", "spellDamage"), 170));
//        builds.addConstraint(new ConstraintId("spellDamageRaw", 835));
        builds.addConstraint(new ConstraintDefense(ElementSkill.EARTH, -100));
        builds.addConstraint(new ConstraintDefense(ElementSkill.THUNDER, -100));
        builds.addConstraint(new ConstraintDefense(ElementSkill.WATER, -100));
        builds.addConstraint(new ConstraintDefense(ElementSkill.FIRE, -100));
        builds.addConstraint(new ConstraintDefense(ElementSkill.AIR, -100));
        builds.addConstraint(new ConstraintMainDamage(4600));
        builds.addConstraint(new ConstraintSpellDamage(Spell.ARROW_STORM, 40122));
        builds.addConstraint(new ConstraintSpellCost(Spell.ARROW_STORM, 1));
        builds.addConstraint(new ConstraintHp(12500));
        for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
            builds.addConstraint(exclusion);
        builds.generate();
        return builds;
    }

    /**
     * @return https://wynndata.tk/s/ol8ktd
     */
    private static BuildGenerator test() {
        ArrayList[] allItems = {Item.helmets, Item.chestplates, Item.leggings, Item.boots, new ArrayList<>(Item.rings), Item.rings, Item.bracelets, Item.necklaces, Item.bows};
        BuildGenerator builds = new BuildGenerator(allItems, new HashSet<>() {{
            add(ElementSkill.THUNDER);
            add(ElementSkill.WATER);
            add(ElementSkill.FIRE);
        }});
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
        builds.generate();
        return builds;
    }

    /**
     * @return https://www.wynndata.tk/s/wj4zbg
     * 23159
     */
    private static BuildGenerator wfaNeptaSpellSpam() {
        ArrayList[] allItems = {Item.helmets, Item.chestplates, Item.leggings, Item.boots, new ArrayList<>(Item.rings), Item.rings, Item.bracelets, Item.necklaces, Item.wands};
        BuildGenerator builds = new BuildGenerator(allItems, new HashSet<>() {{
            add(ElementSkill.EARTH);
            add(ElementSkill.WATER);
            add(ElementSkill.FIRE);
        }});
        builds.addConstraint(new ConstraintId("manaRegen", 11));
        builds.addConstraint(new ConstraintId("bonusWaterDamage", 56));
        builds.addConstraint(new ConstraintId("spellDamage", 105));
        builds.addConstraint(new ConstraintId("spellDamageRaw", 850));
        builds.addConstraint(new ConstraintSpellCost(Spell.METEOR, 1));
        builds.addConstraint(new ConstraintSpellDamage(Spell.METEOR, 15500));
        builds.addConstraint(new ConstraintHp(12000));
        for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
            builds.addConstraint(exclusion);
        builds.generate();
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
