package apple.build.wynncraft;


import apple.build.BuildMain;
import apple.build.sql.itemdb.InsertItemDB;
import apple.build.wynnbuilder.ServiceWynnbuilderItemDB;
import apple.build.wynncraft.items.Item;
import apple.utilities.request.AppleRequest;
import apple.utilities.request.AppleRequestVoid;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetItems {
    private static final File fileToSave;

    static {
        List<String> path = List.of(BuildMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        fileToSave = new File(String.join("/", path.subList(0, path.size() - 1)) + "/data/items.json");
    }

    public static void getItems(boolean shouldCall) throws IOException, SQLException {
        if (shouldCall) ServiceWynnbuilderItemDB.getInstance().queue(() -> {
            final String url = "https://api.wynncraft.com/public_api.php?action=itemDB&category=all";
            try (BufferedReader input = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave));
                int c;
                while ((c = input.read()) != -1) {
                    writer.write(c);
                }
                writer.close();
            } catch (MalformedURLException e) {
                throw new AppleRequest.AppleRequestException("url is not valid");
            } catch (IOException | JsonIOException | JsonSyntaxException e) {
                throw new AppleRequest.AppleRequestException(url + " had an IOException", e);
            }
        }).complete();
        BufferedReader itemsJsonReader = new BufferedReader(new FileReader("data/items.json"));
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
        System.out.println("get item DB");
    }
}
