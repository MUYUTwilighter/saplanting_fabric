package cool.muyucloud.saplanting.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

public class Translation {
    private static final Translation TRANSLATION = new Translation();
    
    private HashMap<String, String> map;
    
    public Translation() {
        String json;
        try {
            json = IOUtils.toString(
                Objects.requireNonNull(
                    Translation.class.getClassLoader()
                        .getResourceAsStream("assets/saplanting/lang/en_us.json")),
                StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new NullPointerException("Can not read default language file!");
        }
        this.map = (new Gson()).fromJson(json, new TypeToken<HashMap<String, String>>() {}.getType());
    }

    public static String translate(String key) {
        if (TRANSLATION.map.containsKey(key)) {
            return TRANSLATION.map.get(key);
        }
        return key;
    }

    public static void updateLanguage(String name) {
        String json;
        try {
            json = IOUtils.toString(
                Objects.requireNonNull(
                    Translation.class.getClassLoader()
                        .getResourceAsStream("assets/saplanting/lang/%s.json".formatted(name))),
                StandardCharsets.UTF_8);
        } catch (Exception e) {
            return;
        }
        TRANSLATION.map = (new Gson()).fromJson(json, new TypeToken<HashMap<String, String>>() {
        }.getType());
    }
}
