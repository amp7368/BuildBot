package apple.build.sql;

import apple.build.data.BuildGenerator;
import apple.build.sql.indexdb.GetIndexDB;
import apple.build.sql.indexdb.SearchResults;
import apple.build.wynncraft.items.Item;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PreFilter {
    public static void filterItemPool(BuildGenerator generator, Item.ItemType weaponType) {
        List<SearchResults> searches = null;
        try {
            searches = GetIndexDB.getMatchingSearches(generator.getConstraints(), generator.getSimplifiedConstraints(), generator.getArchetype(), weaponType);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (searches == null || searches.isEmpty()) return;
        Map<Item.ItemType, List<String>> results = searches.get(0).results;
        boolean isFirst = true;
        for (SearchResults search : searches) {
            if (isFirst) {
                isFirst = false;
                continue;
            }
            search.refine(results);
        }
        generator.refineItemPoolTo(results);
    }
}
