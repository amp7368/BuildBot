package apple.build.sql.indexdb;

import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.ConstraintSimplified;
import apple.build.search.enums.ElementSkill;
import apple.build.utils.Pair;
import apple.build.wynncraft.items.Item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class GetIndexDB {
    public static long getEnumId(String name) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            ResultSet response = statement.executeQuery(GetIndexSql.Get.constraintEnumId(name));
            if (response.isClosed()) {
                return -1;
            }
            int id = response.getInt("constraint_enum_id");
            response.close();
            statement.close();
            return id;
        }
    }

    public static long getTextValId(String text) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            ResultSet response = statement.executeQuery(GetIndexSql.Get.constraintTextVal(text));
            if (response.isClosed()) {
                return -1;
            }
            int id = response.getInt("text_val_index");
            response.close();
            statement.close();
            return id;
        }
    }

    public static int getItemId(String name) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            ResultSet response = statement.executeQuery(GetIndexSql.Get.itemId(name));
            int itemId;
            if (!response.isClosed()) {
                itemId = response.getInt("item_name_id");
                response.close();
            } else {
                statement.execute(GetIndexSql.Insert.item(name, itemId = VerifyIndexDB.currentItemId++));
            }
            statement.close();
            return itemId;
        }
    }

    public static Pair<Boolean, List<SearchResults>> getMatchingSearches(List<BuildConstraint> constraints,
                                                                         List<ConstraintSimplified> simplifiedConstraints,
                                                                         Set<ElementSkill> archetype,
                                                                         Item.ItemType weaponType) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            String sql = GetIndexSql.ComplexGet.getSearches(simplifiedConstraints);
            ResultSet response = statement.executeQuery(sql);
            Map<Long, SearchConstraints> searches = new HashMap<>();
            if (!response.isClosed()) {
                while (response.next()) {
                    long searchId = response.getLong("search_id");
                    SearchConstraints search;
                    if (searches.containsKey(searchId)) {
                        search = searches.get(searchId);
                    } else {
                        searches.put(searchId, search = new SearchConstraints(searchId));
                    }
                    search.addConstraint(response);
                }
            }
            response.close();
            for (Map.Entry<Long, SearchConstraints> search : searches.entrySet()) {
                response = statement.executeQuery(GetIndexSql.Get.getSearchArchetypeFromId(search.getKey()));
                Set<ElementSkill> searchArchetype = new HashSet<>();
                if (!response.isClosed())
                    while (response.next())
                        searchArchetype.add(ElementSkill.valueOf(response.getString("element")));
                response.close();
                search.getValue().setArchetype(searchArchetype);
            }
            searches.entrySet().removeIf(entry -> !entry.getValue().archetypeMatches(archetype));
            searches.entrySet().removeIf(entry -> !entry.getValue().isLessStrict(constraints));
            boolean isExactMatch = false;
            for (SearchConstraints search : searches.values()) {
                if (search.isExact(constraints)) {
                    isExactMatch = true;
                    break;
                }
            }
            List<SearchResults> searchResults = new ArrayList<>(searches.size());
            for (Long searchId : searches.keySet()) {
                response = statement.executeQuery(GetIndexSql.Get.getSearchFromId(searchId));
                searchResults.add(new SearchResults(response));
            }
            statement.close();
            searchResults.removeIf(search -> !search.isForClass(weaponType));
            return new Pair<>(isExactMatch, searchResults);
        }
    }
}
