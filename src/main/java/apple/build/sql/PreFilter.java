package apple.build.sql;

import apple.build.data.BuildGenerator;
import apple.build.sql.indexdb.GetIndexDB;
import apple.build.sql.indexdb.SearchResults;
import apple.build.utils.Pair;
import apple.build.wynncraft.items.Item;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PreFilter {
    public static boolean filterItemPool(BuildGenerator generator, Item.ItemType weaponType) {
        List<SearchResults> searches;
        boolean exactMatch;
        try {
            Pair<Boolean, List<SearchResults>> result = GetIndexDB.getMatchingSearches(generator.getConstraints(), generator.getSimplifiedConstraints(), generator.getArchetype(), weaponType);
            exactMatch = result.getKey();
            searches = result.getValue();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        if (searches == null || searches.isEmpty()) return false;
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
        return exactMatch;
    }
}
