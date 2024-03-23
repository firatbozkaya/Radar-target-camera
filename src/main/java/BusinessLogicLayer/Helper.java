package BusinessLogicLayer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Type;
import java.util.HashMap;

public class Helper {

    public String createStringByJson(String... keyValue) {
        if (keyValue.length % 2 != 0) {
            throw new IllegalArgumentException("key value error");
        }
        JSONObject obj = new JSONObject();
        for (int i = 0; i < keyValue.length; i += 2) {
            String key = keyValue[i];
            String value = keyValue[i + 1];
            obj.put(key, value);
        }
        return obj.toString();
    }

    public JSONObject returnJSONObjectByString(String jsonData) throws ParseException {
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(jsonData);
    }

    public int getItemYPosition(int height, int positionY){
        return height-positionY;
    }

}
