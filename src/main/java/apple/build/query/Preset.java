package apple.build.query;

public class Preset {
    public String name;

    public Preset() {

    }

    public Preset(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
