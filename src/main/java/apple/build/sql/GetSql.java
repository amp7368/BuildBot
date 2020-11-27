package apple.build.sql;

import org.jetbrains.annotations.Nullable;

public class GetSql {
    @Nullable
    protected static String convertToNullable(@Nullable String s) {
        return s == null ? null : "'" + s + "'";
    }

    @Nullable
    protected static String convertToSql(@Nullable String s) {
        return s == null ? null : s.replace("'", "$");
    }

    @Nullable
    public static String convertFromSql(@Nullable String s) {
        return s == null ? null : s.replace("$", "'");
    }
}
