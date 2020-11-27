package apple.build.data.constraints;

public class ConstraintSimplified {
    private final ConstraintSimplifiedName type;
    public String text = null;
    public int val = Integer.MIN_VALUE;

    public ConstraintSimplified(ConstraintSimplifiedName type) {
        this.type = type;
    }

    public String getValSql() {
        return val == Integer.MIN_VALUE ? null : String.valueOf(val);
    }

    public enum ConstraintSimplifiedName {
        CONSTRAINT_JOINED_ID,
        CONSTRAINT_ID,
        CONSTRAINT_HPR,
        CONSTRAINT_HP,
        CONSTRAINT_DEFENSE,
        CONSTRAINT_EXCLUSION,
        CONSTRAINT_SPELL_COST,
        CONSTRAINT_SPELL_DMG,
        CONSTRAINT_MAIN_DMG
    }

    public String getName() {
        return type.name();
    }
}
