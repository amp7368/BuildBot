package apple.build.data.enums;

public enum Powder {
    THUNDER(.28, .22, .17, .13, .11, .09, 5, 3, 3, 2, 1, 1, 40, 32, 24, 18, 13, 8),
    AIR(.35, .28, .22, .17, .14, .11, 16, 13, 9, 7, 4, 2, 18, 17, 13, 10, 9, 6),
    EARTH(.46, .38, .31, .25, .21, .17, 18, 15, 11, 8, 6, 3, 22, 18, 16, 14, 9, 6),
    WATER(.32, .26, .21, .17, .15, .13, 13, 11, 8, 6, 4, 3, 17, 14, 12, 10, 7, 4),
    FIRE(.37, .30, .24, .19, .16, .14, 15, 12, 9, 6, 4, 2, 19, 16, 13, 10, 8, 5);

    private static final int MAX_POWDER = 6;
    public static final int MAX_POWDER_INDEX = MAX_POWDER - 1;
    private final double[] percs;
    private final int[] lowers;
    private final int[] uppers;

    Powder(double perc6, double perc5, double perc4, double perc3, double perc2, double perc1,
           int lower6, int lower5, int lower4, int lower3, int lower2, int lower1,
           int upper6, int upper5, int upper4, int upper3, int upper2, int upper1) {
        this.percs = new double[]{perc1, perc2, perc3, perc4, perc5, perc6};
        this.lowers = new int[]{lower1, lower2, lower3, lower4, lower5, lower6};
        this.uppers = new int[]{upper1, upper2, upper3, upper4, upper5, upper6};
    }

    public ElementSkill getElement() {
        return ElementSkill.valueOf(name());
    }

    public double getPerc() {
        return percs[MAX_POWDER_INDEX];
    }

    public int getLower() {
        return lowers[MAX_POWDER_INDEX];
    }

    public int getUpper() {
        return uppers[MAX_POWDER_INDEX];
    }

    public double getPerc(int powderLvl) {
        return percs[powderLvl];
    }

    public int getLower(int powderLvl) {
        return lowers[powderLvl];
    }

    public int getUpper(int powderLvl) {
        return uppers[powderLvl];
    }
}
