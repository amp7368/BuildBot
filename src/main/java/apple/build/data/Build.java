package apple.build.data;

import apple.build.wynncraft.items.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Build {

    private final List<Item> items = new ArrayList<>();

    public Build(List<Item>[] allItems) {
        for (int i = 0; i < allItems.length; i++) {
            items.add(allItems[i].get(0));
        }
    }

    @Override
    public String toString() {
        return items.stream().map(item -> {
            String s = item.toString();
            int l = Math.max(0, 20 - s.length());
            return " ".repeat(l) + s;
        }).collect(Collectors.joining(", "));
    }
}
