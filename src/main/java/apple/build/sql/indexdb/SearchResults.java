package apple.build.sql.indexdb;

import apple.build.sql.GetSql;
import apple.build.wynncraft.items.Item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResults {
    public Map<Item.ItemType, List<String>> results = new HashMap<>();

    public SearchResults(ResultSet response) throws SQLException {
        if (!response.isClosed()) {
            while (response.next()) {
                Item.ItemType itemType = Item.ItemType.valueOf(response.getString("item_type_name"));
                results.putIfAbsent(itemType, new ArrayList<>());
                results.get(itemType).add(GetSql.convertFromSql(response.getString("item_name")));
            }
        }
    }

    public boolean isForClass(Item.ItemType weaponType) {
        return results.containsKey(weaponType);
    }

    public void refine(Map<Item.ItemType, List<String>> refineMeResults) {
        for (Map.Entry<Item.ItemType, List<String>> typeToItems : refineMeResults.entrySet()) {
            List<String> myResults = results.get(typeToItems.getKey());
            if (myResults == null) {
                for (List<String> items : refineMeResults.values()) {
                    items.clear();
                }
                return;
            }
            typeToItems.getValue().removeIf(item -> !myResults.contains(item));
        }
    }
}
