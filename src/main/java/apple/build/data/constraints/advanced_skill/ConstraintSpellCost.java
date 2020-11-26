package apple.build.data.constraints.advanced_skill;

import apple.build.data.BuildMath;
import apple.build.data.constraints.ConstraintSimplified;
import apple.build.data.constraints.ConstraintType;
import apple.build.data.enums.ElementSkill;
import apple.build.data.enums.Spell;
import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.NotNull;
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
    boolean internalIsValid(int[] skills, int extraSkillPoints, int[] extraSkillPerElement, Collection<Item> items) {
        int addedCostRaw = 0;
        int addedCostPerc = 0;
        int intelligence = ElementSkill.WATER.getSkill(skills) + Math.min(ElementSkill.WATER.getSkill(extraSkillPerElement), extraSkillPoints);
        for (Item item : items) {
            addedCostRaw += item.getId(idIndexRaw);
            addedCostPerc += item.getId(idIndexPerc);
        }
        return BuildMath.getMana(spell, intelligence, addedCostRaw, addedCostPerc) <= cost;
    }

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
                int rawVal = item.getId(idIndexRaw);
                int percVal = item.getId(idIndexPerc);
                int intelVal = item.getId(INTELLIGENCE_POINTS);
                if (rawVal < bestRawVal) {
                    best = item;
                    bestRawVal = rawVal;
                }
                if (percVal < bestPercVal) {
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

    @Override
    public @NotNull ConstraintType getType() {
        return ConstraintType.TEXT_VAL;
    }

    @Override
    public @NotNull ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_SPELL_COST);
        simple.text = spell.name();
        simple.val = cost;
        return simple;
    }
}
