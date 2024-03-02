package fr.zarinoow;

import com.deepl.api.DeepLException;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MondialBot {

    private static MondialBot instance;
    private static File botDir = /**/new File(Thread.currentThread().getContextClassLoader().getResource("").getPath());/*/ new File(System.getProperty("user.home"), "Desktop");*/
    private Translator translator = new Translator(BotConfig.DEEPL_API_KEY);
    private DiscordBot bot;
    private Map<Long, GuildProperties> guildPropertiesMap = new HashMap<>();

    public static void main(String[] args) {
        BotConfig.init();
        instance = new MondialBot();
    }

    public MondialBot() {
        load();
        bot = new DiscordBot(BotConfig.BOT_TOKEN);
        bot.start();
        bot.setupCommands();
    }

    public static MondialBot getInstance() {
        return instance;
    }

    public DiscordBot getBot() {
        return bot;
    }

    public Translator getTranslator() {
        return translator;
    }

    public GuildProperties getGuildProperties(long guildID) {
        if(!guildPropertiesMap.containsKey(guildID)) guildPropertiesMap.put(guildID, new GuildProperties(guildID));
        return guildPropertiesMap.get(guildID);
    }

    public static String getTranslation(String message, String language) throws DeepLException, InterruptedException {
        TextResult result = getInstance().getTranslator().translateText(message, null, language);
        return result.getText();
    }

    public static File getBotDirectory() {
        return botDir;
    }

    // Saving system
    public void save() {
        saveGuildProperties();
    }

    public void load() {
        loadGuildProperties();
    }

    public void saveGuildProperties() {
        JsonObject guildProperties = new JsonObject();
        guildPropertiesMap.forEach((guildID, gp) -> {
            JsonObject guild = new JsonObject();
            guild.addProperty("guildID", guildID);
            JsonObject announceProperties = new JsonObject();
            gp.getAnnounceProperties().getChannels().forEach(channelID -> {
                announceProperties.addProperty(String.valueOf(channelID), gp.getAnnounceProperties().getLangForChannel(channelID));
            });
            guild.add("announceProperties", announceProperties);
            guildProperties.add(String.valueOf(guildID), guild);
        });

        File file = new File(getBotDirectory(), "guildProperties.json");
        if(file.exists()) file.delete();

        try (FileWriter fileWriter = new FileWriter(file)) {
            Gson gson = new Gson();
            gson.toJson(guildProperties, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadGuildProperties() {
        File file = new File(getBotDirectory(), "guildProperties.json");
        if(!file.exists()) return;

        try {
            JsonObject guildProperties = new Gson().fromJson(JSONFileReader.getJSONAsString(file), JsonObject.class);
            guildProperties.entrySet().forEach(entry -> {
                long guildID = Long.parseLong(entry.getKey());
                JsonObject guild = entry.getValue().getAsJsonObject();
                GuildProperties gp = getGuildProperties(guildID);
                JsonObject announceProperties = guild.get("announceProperties").getAsJsonObject();
                announceProperties.entrySet().forEach(channel -> {
                    gp.getAnnounceProperties().setLangForChannel(Long.parseLong(channel.getKey()), channel.getValue().getAsString());
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}