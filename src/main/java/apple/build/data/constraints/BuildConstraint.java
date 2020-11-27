package apple.build.data.constraints;


public interface BuildConstraint {
    /**
     * checks if other is more strict than me
     *
     * @param obj the other to check against
     * @return true if other is more or equal strict, false if these are incompatible, false otherwise
     */
    boolean isMoreStrict(BuildConstraint obj);

    ConstraintSimplified.ConstraintSimplifiedName getSimplifiedName();
}
