package apple.build.sql.indexdb;

import apple.build.data.constraints.ConstraintSimplified;
import apple.build.data.enums.ElementSkill;
import apple.build.sql.GetSql;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GetIndexSql extends GetSql {
    public static class Insert {
        public static String constraintEnum(long id, String name) {
            return String.format("INSERT INTO constraint_enum_ids (constraint_enum_id, name) VALUES (%d,'%s');", id, name);
        }

        public static String constraintTextVal(long index, String text) {
            return String.format("INSERT INTO constraint_text_vals (text_val, text_val_index)\n" +
                    "VALUES ('%s', %d);", text, index);
        }

        public static String constraintId(long constraintId) {
            return String.format("INSERT INTO constraint_ids (constraint_id) VALUES (%d);", constraintId);
        }

        public static String searchId(long searchId) {
            return String.format("INSERT INTO search_ids (search_id) VALUES (%d);", searchId);
        }

        public static String constraint(long constraintId, long enumId, long textValId, ConstraintSimplified constraint) {
            return String.format("INSERT INTO all_constraints (constraint_id, constraint_enum_id, text_val_index, val)\n" +
                            "VALUES (%d, %d, %s, %s);",
                    constraintId,
                    enumId,
                    textValId == -1 ? null : String.valueOf(textValId),
                    constraint.getValSql());
        }

        public static String searchToConstraint(long searchId, long constraintId) {
            return String.format("INSERT INTO search_to_constraints (search_id, constraint_id) VALUES (%d, %d);", searchId, constraintId);
        }

        public static String item(String name, int id) {
            return String.format("INSERT INTO items (item_name_id, item_name) VALUES (%d,'%s');", id, convertToSql(name));
        }

        public static String result(long searchId, int itemNameId, byte itemTypeId) {
            return String.format("INSERT INTO pre_index_results (search_id, item_type_id, item_name_id) VALUES (%d,%d,%d);", searchId, itemTypeId, itemNameId);
        }

        public static String archetype(long searchId, ElementSkill element) {
            return String.format("INSERT INTO archetype (element,search_id) VALUES ('%s',%d);", element.name(), searchId);
        }
    }

    public static class Get {
        public static String constraintEnumId(String name) {
            return String.format("SELECT constraint_enum_id\n" +
                    "FROM constraint_enum_ids\n" +
                    "WHERE name = '%S';", name);
        }

        public static String constraintTextVal(@NotNull String text) {
            return String.format("SELECT text_val_index\n" +
                    "FROM constraint_text_vals\n" +
                    "WHERE text_val = '%s';", text);
        }

        public static String itemId(String name) {
            return String.format("SELECT item_name_id FROM items WHERE item_name = '%s'", convertToSql(name));
        }

        public static String getSearchFromId(long searchId) {
            return String.format("SELECT items.item_name, it.item_type_name\n" +
                    "FROM pre_index_results\n" +
                    "         INNER JOIN items ON pre_index_results.item_name_id = items.item_name_id\n" +
                    "         INNER JOIN item_types it on pre_index_results.item_type_id = it.item_type_id\n" +
                    "WHERE search_id = %d;", searchId);
        }

        public static String getSearchArchetypeFromId(long searchId) {
            return String.format("SELECT element FROM archetype WHERE search_id = %d;", searchId);
        }
    }

    public static class ComplexGet {
        /**
         * @param constraints must have at least 1 entry
         * @return sql to get all similar searches
         */
        public static String getSearchIds(List<ConstraintSimplified> constraints) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT search_ids.search_id\n" +
                    "FROM search_ids\n" +
                    "WHERE search_id NOT IN (\n" +
                    "    -- select all the search ids where the constraints are less narrow than my constraints\n" +
                    "    SELECT search_id\n" +
                    "    FROM all_constraints\n" +
                    "             INNER JOIN search_to_constraints ON all_constraints.constraint_id = search_to_constraints.constraint_id\n" +
                    "             INNER JOIN constraint_enum_ids\n" +
                    "                        ON all_constraints.constraint_enum_id = constraint_enum_ids.constraint_enum_id\n" +
                    "    WHERE (constraint_enum_ids.constraint_enum_id, text_val_index) NOT IN\n" +
                    "          (\n");
            boolean first = true;
            for (ConstraintSimplified constraint : constraints) {
                if (first) first = false;
                else {
                    sql.append("\nUNION\n");
                }
                if (constraint.text == null) {
                    sql.append(String.format("SELECT constraint_enum_ids.constraint_enum_id, NULL\n" +
                            "              FROM constraint_enum_ids\n" +
                            "              WHERE name = '%s'", constraint.getName()));
                } else {
                    sql.append(String.format("SELECT constraint_enum_ids.constraint_enum_id, constraint_text_vals.text_val_index\n" +
                            "              FROM constraint_text_vals\n" +
                            "                       INNER JOIN constraint_enum_ids\n" +
                            "              WHERE text_val = '%s'\n" +
                            "                AND name = '%s'", constraint.text, constraint.getName()));
                }
            }
            sql.append("\n)\n)");


            return sql.toString();
        }

        public static String getSearches(List<ConstraintSimplified> constraints) {
            return String.format("SELECT search_and_constraints.search_id, constraint_enum_ids.name, ctv.text_val, all_constraints.val\n" +
                    "FROM (\n" +
                    "         SELECT search_to_constraints.search_id, search_to_constraints.constraint_id\n" +
                    "         FROM search_to_constraints\n" +
                    "                  INNER JOIN\n" +
                    "              (\n%s\n) as searches on searches.search_id = search_to_constraints.search_id\n" +
                    "     ) AS search_and_constraints\n" +
                    "         INNER JOIN all_constraints ON all_constraints.constraint_id = search_and_constraints.constraint_id\n" +
                    "         INNER JOIN constraint_enum_ids ON all_constraints.constraint_enum_id = constraint_enum_ids.constraint_enum_id\n" +
                    "         INNER JOIN constraint_text_vals ctv on all_constraints.text_val_index = ctv.text_val_index", getSearchIds(constraints));
        }
    }
}
