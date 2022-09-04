package cool.muyucloud.saplanting.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Translation {
    private static HashMap<String, String> MAP;

    public static String translate(String key) {
        if (MAP.containsKey(key)) {
            return MAP.get(key);
        }
        return key;
    }

    public static boolean updateLanguage(String name) {
        String json;
        try {
            json = IOUtils.toString(
                Objects.requireNonNull(
                    Translation.class.getClassLoader()
                        .getResourceAsStream("assets/saplanting/lang/%s.json".formatted(name))),
                StandardCharsets.UTF_8);
        } catch (Exception e) {
            return false;
        }
        MAP = (new Gson()).fromJson(json, new TypeToken<HashMap<String, String>>() {
        }.getType());
        return true;
    }
}
