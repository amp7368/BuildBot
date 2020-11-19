package apple.build.data.constraints.advanced_skill;

import apple.build.data.BuildMath;
import apple.build.data.ElementSkill;
import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ConstraintSpellCost extends BuildConstraintAdvancedSkills {
    public static final String SPELL_COST_RAW_NAME = "spellCostRaw";
    public static final String SPELL_COST_PERC_NAME = "spellCostPct";
    public static final int INTELLIGENCE_POINTS = Item.getIdIndex("intelligencePoints");
    private final int cost;
    private final Spell spell;
    private final int idIndexRaw;
    private final int idIndexPerc;

    public ConstraintSpellCost(Spell spell, int cost) {
        this.cost = cost;
        this.spell = spell;
        this.idIndexRaw = Item.getIdIndex(SPELL_COST_RAW_NAME + spell.spellNum);
        this.idIndexPerc = Item.getIdIndex(SPELL_COST_PERC_NAME + spell.spellNum);
    }

    @Override
    boolean internalIsValid(int[] bestSkillsPossible, int extraSkillPoints, Collection<Item> items) {
        int addedCostRaw = 0;
        int addedCostPerc = 0;
        int intelligence = bestSkillsPossible[ElementSkill.WATER.ordinal()] + extraSkillPoints;
        for (Item item : items) {
            addedCostRaw += item.getId(idIndexRaw);
            addedCostPerc += item.getId(idIndexPerc);
        }
        return BuildMath.getMana(spell, intelligence, addedCostRaw, addedCostPerc) <= cost;
    }

    @Override
    public @Nullable Item getBest(List<Item> items) {
        Item best = null;
        int bestRawVal = 0;
        int bestPercVal = 0;
        int bestIntelVal = 0;
        for (Item item : items) {
            if (best == null) {
                best = item;
                bestRawVal = best.getId(idIndexRaw);
                bestPercVal = best.getId(idIndexPerc);
                bestIntelVal = best.getId(INTELLIGENCE_POINTS);
            } else {
                int rawVal = best.getId(idIndexRaw);
                int percVal = best.getId(idIndexPerc);
                int intelVal = best.getId(INTELLIGENCE_POINTS);
                if (rawVal > bestRawVal) {
                    best = item;
                    bestRawVal = rawVal;
                }
                if (percVal > bestPercVal) {
                    bestPercVal = percVal;
                }
                if (intelVal > bestIntelVal) {
                    bestIntelVal = intelVal;
                }
            }
        }
        Item newItem = Item.makeItem(best);
        newItem.ids.put(idIndexPerc, bestPercVal);
        newItem.ids.put(INTELLIGENCE_POINTS, bestIntelVal);
        return newItem;
    }

    @Override
    public boolean contributes(Item item) {
        return item.getId(idIndexRaw) < 0 ||
                item.getId(idIndexPerc) < 0 ||
                item.getId(INTELLIGENCE_POINTS) > 0; // we want negative spell cost
    }

    /**
     * compares two items with this constraint
     *
     * @param item1 the first item to compare
     * @param item2 the second item to compare
     * @return positive if first is better, negative if second is better, otherwise 0
     */
    @Override
    public int compare(Item item1, Item item2) {
        int perc1 = item1.getId(idIndexPerc);
        int raw1 = item1.getId(idIndexRaw);
        int intel1 = item1.getId(INTELLIGENCE_POINTS);
        int perc2 = item1.getId(idIndexPerc);
        int raw2 = item1.getId(idIndexRaw);
        int intel2 = item1.getId(INTELLIGENCE_POINTS);
        if (perc1 > perc2) {
            if (raw1 > raw2 && intel1 > intel2) {
                return 1;
            }
        } else if (raw1 < raw2 && intel1 < intel2) {
            return -1;
        }
        return 0;
    }

    public enum Spell {
        ARROW_STORM(6, 1, 6, .25, 0, 0, 0, 0.15),
        ESCAPE(3, 2, 1, 0, 0.5, 0, 0, 0),
        BOMB_ARROW(8, 3, 2.5, 0, 0, 0.25, 0, 0.15),
        ARROW_SHIELD(10, 4, 1, 0, 0.3, 0, 0.3, 0),
        BASH(6, 1, 1.3, 0, 0, .4, 0, 0),
        CHARGE(4, 2, 1.5, 0, 0, 0, 0, 0.4),
        UPPERCUT(8, 3, 4, .25, 0, .15, 0, 0),
        WAR_SCREAM(5, 4, 0.3, 0, .25, 0, 0, .75),
        HEAL(6, 1, 0, 0, 0, 0, 0, 0),
        TELEPORT(4, 2, 1, 0.4, 0, 0, 0, 0),
        METEOR(8, 3, 5, 0, 0, .3, 0, .3),
        ICE_SNAKE(4, 4, .7, 0, 0, 0, .5, 0),
        SPIN_ATTACK(6, 1, 1.5, .3, 0, 0, 0, 0),
        VANISH(1, 2, 0, 0, 0, 0, 0, 0),
        MULTIHIT(8, 3, 3, .3, 0, 0, .3, 0),
        SMOKE_BOMB(8, 4, 3, 0, .25, .25, 0, 0),
        TOTEM(4, 1, .2, 0, .2, 0, 0, .2),
        HAUL(1, 2, 1, 0, 0, 0, 0, 0),
        AURA(8, 3, 2, 0, 0, 0, .3, 0),
        UPROOT(6, 4, .75, 0, 0, .3, 0, 0);

        public final int mana; // the base mana cost
        private final int spellNum; // the spell # (1,2,3,4)
        public final double damage; // the base spell dmg
        public final double[] elemental; // the extra thunder

        Spell(int mana, int spellNum, double damage, double thunder, double air, double earth, double water, double fire) {
            this.mana = mana;
            this.spellNum = spellNum;
            this.damage = damage;
            elemental = new double[ElementSkill.values().length];
            int i = 0;
            for (ElementSkill element : ElementSkill.values()) {
                switch (element) {
                    case THUNDER:
                        elemental[i] = thunder;
                        break;
                    case AIR:
                        elemental[i] = air;
                        break;
                    case EARTH:
                        elemental[i] = earth;
                        break;
                    case WATER:
                        elemental[i] = water;
                        break;
                    case FIRE:
                        elemental[i] = fire;
                        break;
                }
                i++;
            }
        }
    }
}
