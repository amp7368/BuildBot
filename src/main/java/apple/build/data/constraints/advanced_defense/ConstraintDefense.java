package apple.build.data.constraints.advanced_defense;

import apple.build.data.ElementSkill;

public class ConstraintDefense extends BuildConstraintAdvancedDefense {
    private final int index;
    private final int val;

    public ConstraintDefense(ElementSkill name, int val) {
        int i = 0;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            if (name == elementSkill) {
                break;
            }
            i++;
        }
        index = i;
        this.val = val;
    }

    @Override
    public boolean isValid(int[] defenseRaw) {
        return defenseRaw[index] >= val;
    }
}
