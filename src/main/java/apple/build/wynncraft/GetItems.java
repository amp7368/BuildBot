package apple.build.wynncraft;


import apple.build.sql.itemdb.InsertItemDB;
import apple.build.wynncraft.items.Item;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GetItems {
    public static void getItems() throws IOException, SQLException {
        BufferedReader itemsJsonReader = new BufferedReader(new FileReader(new File("data/items.json")));
        JSONObject itemsJsonResponse = new JSONObject(itemsJsonReader.readLine());
        itemsJsonReader.close();
        JSONArray allItemsJson = itemsJsonResponse.getJSONArray("items");
        Item[] items = new Item[allItemsJson.length()];
        int i = 0;
        for (Object itemJsonAsObject : allItemsJson) {
            JSONObject itemJson = (JSONObject) itemJsonAsObject;
            Map<String, Object> itemMap = new HashMap<>();
            for (String key : itemJson.keySet()) {
                itemMap.put(key, itemJson.get(key));
            }
            items[i++] = Item.makeItem(itemMap);
        }
        InsertItemDB.insertItems(items);
        System.out.println("done");
    }
}
