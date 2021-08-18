package apple.build.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class QueriesSavedForUser {
    public long discordId;
    public String[] queries;

    public QueriesSavedForUser(QuerySaved querySaved, long userId) {
        this.discordId = userId;
        this.queries = new String[]{querySaved.id};
    }

    public void addQuery(QuerySaved querySaved) {
        Set<String> newQueries = new HashSet<>(Arrays.asList(queries));
        newQueries.add(querySaved.id);
        queries = newQueries.toArray(new String[0]);
    }
}
