package apple.build.wynnbuilder;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class WynnbuilderItemDB {
    private HashMap<String, RawWynnbuilderItem> items = new HashMap<>();

    public WynnbuilderItemDB(RawWynnbuilderItemDB db) {
        for (RawWynnbuilderItem item : db.items) {
            items.put(item.name, item);
        }
    }

    @Nullable
    public RawWynnbuilderItem get(String name) {
        return items.get(name);
    }
}
