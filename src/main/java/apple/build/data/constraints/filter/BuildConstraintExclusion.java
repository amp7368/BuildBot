package apple.build.data.constraints.filter;

import apple.build.data.constraints.BuildConstraint;
import apple.build.data.constraints.ConstraintSimplified;
import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BuildConstraintExclusion implements BuildConstraint {
    public final static List<BuildConstraintExclusion> all = new ArrayList<>();

    static {
        for (BuildConstraintExclusionTypes type : BuildConstraintExclusionTypes.values()) {
            all.add(new BuildConstraintExclusion(type));
        }
    }

    private final Set<String> excluded;
    private final String type;

    public BuildConstraintExclusion(BuildConstraintExclusionTypes type) {
        this.excluded = new HashSet<>(type.exclusion);
        this.type = type.name();
    }

    public BuildConstraintExclusion(String text, Integer val) {
        BuildConstraintExclusionTypes type = BuildConstraintExclusionTypes.valueOf(text);
        this.type = type.name();
        this.excluded = new HashSet<>(type.exclusion);
    }


    public void filter(List<Item> itemsToFilter, List<Item> knownItems) {
        for (Item item : knownItems) {
            if (excluded.contains(item.name)) {
                itemsToFilter.removeIf(i -> excluded.contains(i.name));
                return;
            }
        }
    }

    public boolean isValid(List<Item> items) {
        boolean exists = false;
        for (Item item : items) {
            if (excluded.contains(item.name)) {
                if (exists) return false;
                exists = true;
            }
        }
        return true;
    }

    private enum BuildConstraintExclusionTypes {
        ORNATE(Arrays.asList("Ornate Shadow Cloud", "Ornate Shadow Cover", "Ornate Shadow Garb", "Ornate Shadow Cowl")),
        HIVE_MASTER(Arrays.asList(
                "Infused Hive Dagger", "Infused Hive Spear", "Infused Hive Wand", "Infused Hive Relik", "Infused Hive Bow",
                "Gaea-Hewn Boots", "Hephaestus-Forged Sabatons",
                "Hephaestus-Forged Greaves", "Chaos-Woven Greaves", "Abyss-Imbued Leggings",
                "Boreal-Patterned Aegis", "Elysium-Engraved Aegis", "Twilight-Gilded Cloak",
                "Anima-Infused Helmet", "Obsidian-Framed Helmet",
                "Intensity", "Prowess", "Contrast"
        )),
        HIVE_THUNDER(Arrays.asList("Sparkling Visor", "Thunderous Step", "Static-charged Leggings", "Insulated Plate Mail", "Bottled Thunderstorm", "Lightning Flash")),
        HIVE_AIR(Arrays.asList("Pride of the Aerie", "Flashstep", "Turbine Greaves", "Gale's Freedom", "Breezehands", "Vortex Bracer")),
        HIVE_EARTH(Arrays.asList("Ambertoise Shell", "Humbark Moccasins", "Elder Oak Roots", "Beetle Aegis", "Subur Clip", "Golemlus Core")),
        HIVE_WATER(Arrays.asList("Whitecap Crown", "Stillwater Blue", "Trench Scourer", "Silt of the Seafloor", "Coral Ring", "Moon Pool Circlet")),
        HIVE_FIRE(Arrays.asList("Clockwork", "Dupliblaze", "Mantlewalkers", "Cinderchain", "Soulflare", "Sparkweaver"));
        private final List<String> exclusion;

        BuildConstraintExclusionTypes(List<String> exclusion) {
            this.exclusion = exclusion;
        }
    }

    /**
     * @return the database ready version of this constraint
     */
    @NotNull
    public ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_EXCLUSION);
        simple.text = this.type;
        return simple;
    }

    @Override
    public boolean isMoreStrict(BuildConstraint obj) {
        if (obj instanceof BuildConstraintExclusion) {
            BuildConstraintExclusion other = (BuildConstraintExclusion) obj;
            return other.type.equals(this.type);
        }
        return false;
    }

    @Override
    public ConstraintSimplified.ConstraintSimplifiedName getSimplifiedName() {
        return ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_EXCLUSION;
    }

    @Override
    public boolean isExact(BuildConstraint constraint) {
        return constraint instanceof BuildConstraintExclusion && ((BuildConstraintExclusion) constraint).type.equals(this.type);
    }
}
