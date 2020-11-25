package apple.build.data.constraints.advanced_skill;

import apple.build.data.constraints.BuildConstraint;
import apple.build.wynncraft.items.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class BuildConstraintAdvancedSkills implements BuildConstraint {

    /**
     * checks whether the items satisfy the constraint
     *
     * @param items the items to check against
     * @return true if it satisfies, otherwise false
     */
    boolean isValid(int[] bestSkillsPossible, int extraSkillPoints, int[] extraSkillPerElement, Item... items) {
        return internalIsValid(bestSkillsPossible, extraSkillPoints, extraSkillPerElement, Arrays.asList(items));
    }


    /**
     * checks whether the items satisfy the constraint
     *
     * @param items1 the items to check against
     * @param items2 the items to check against
     * @return true if it satisfies, otherwise false
     */
    public boolean isValid(int[] bestSkillsPossible, int extraSkillPoints, int[] extraSkillPerElement, Collection<Item> items1, Item... items2) {
        List<Item> items = new ArrayList<>();
        items.addAll(items1);
        items.addAll(Arrays.asList(items2));
        return internalIsValid(bestSkillsPossible, extraSkillPoints, extraSkillPerElement, items);
    }

    /**
     * checks whether the items satisfy the constraint
     *
     * @param bestSkillsPossible the best skill points possible to magnify this constraint
     * @param items              the items to check against
     * @return true if it satisfies, otherwise false
     */
    abstract boolean internalIsValid(int[] bestSkillsPossible, int extraSkillPoints, int[] extraSkillPerElement, Collection<Item> items);
}
