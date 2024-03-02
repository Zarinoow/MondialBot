package fr.zarinoow;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AnnounceProperties {

    private Map<Long, String> channelByLang = new HashMap<>();

    public void setLangForChannel(long channelID, @Nullable String lang) {
        if(lang == null || lang.isBlank()) {
            if(channelByLang.containsKey(channelID)) channelByLang.remove(channelID);
            return;
        }
        channelByLang.put(channelID, lang);
    }

    public String getLangForChannel(long channelID) {
        return channelByLang.get(channelID);
    }

    public Set<Long> getChannels() {
        return channelByLang.keySet();
    }

}
