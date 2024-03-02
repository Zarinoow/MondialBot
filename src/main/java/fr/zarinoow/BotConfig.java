package fr.zarinoow;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.logging.Logger;

public class BotConfig {

    private static BotConfig instance = null;
    public static String BOT_TOKEN;
    public static String DEEPL_API_KEY;
    private JsonObject jsonConfig;

    public BotConfig() {
        instance = this;
        load();
        BOT_TOKEN = getString("bot_token");
        DEEPL_API_KEY = getString("deepl_api_key");
    }

    public void load() {
        File configFile = new File(MondialBot.getBotDirectory(), "config.json");
        if(configFile.exists()) {
            try {
                jsonConfig = JSONFileReader.getJSONAsObject(configFile);
            } catch (IOException e) {
                Logger.getLogger("MondialBot").severe("Can't read the config file !");
            }
        } else {
            // Copy the default config file to the new config file
            try {
                Files.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.json"),
                        configFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Logger.getLogger("MondialBot").severe("Can't copy the default config file to the new config file !");
            }
            load();

        }
    }

    @Nullable
    public static JsonElement getJsonElement(String key) {
        return getJsonElement(getInstance().jsonConfig, key);
    }

    @Nullable
    private static JsonElement getJsonElement(JsonObject current, String key) {
        String[] keys = key.split("\\.");
        if (keys.length == 0) {
            return current;
        } else if (current.has(keys[0])) {
            JsonElement element = current.get(keys[0]);
            if (element.isJsonObject()) {
                return getJsonElement(element.getAsJsonObject(), key.substring(keys[0].length() + 1));
            } else {
                return element;
            }
        } else {
            return null;
        }
    }

    @Nullable
    public static String getString(String key) {
        JsonElement jsonElement = getJsonElement(key);
        return (jsonElement != null && jsonElement.isJsonPrimitive()) ? jsonElement.getAsString() : null;
    }

    private static BotConfig getInstance() {
        if(instance == null) init();
        return instance;
    }

    public static void init() {
        if(instance == null) instance = new BotConfig();
    }

}
