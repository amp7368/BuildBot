package apple.build.utils;

import java.util.HashMap;
import java.util.Map;

public class OneToOneMap<K, V> {
    private final Map<K, V> map = new HashMap<>();
    private final Map<V, K> backwardsMap = new HashMap<>();

    public void put(K k, V v) {
        if (!map.containsKey(k) && !backwardsMap.containsKey(v)) {
            map.put(k, v);
            backwardsMap.put(v, k);
        }
    }

    public V getFromKey(K k) {
        return map.get(k);
    }

    public K getFromVal(V v) {
        return backwardsMap.get(v);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
