package apple.build.wynnbuilder;

import java.util.ArrayList;
import java.util.Arrays;

public class RawWynnbuilderItemDB {
    public ArrayList<RawWynnbuilderItem> items;
    @Override
    public String toString() {
        return "RawWynnbuilderItemDB{" +
                "items=" + Arrays.toString(items.toArray()) +
                '}';
    }
}
