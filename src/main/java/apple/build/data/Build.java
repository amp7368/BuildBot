package apple.build.data;

import apple.build.wynncraft.items.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Build {

    final List<Item> items = new ArrayList<>();

    public Build(List<Item>[] allItems) {
        for (List<Item> allItem : allItems) {
            items.add(allItem.get(0));
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
