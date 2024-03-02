package fr.zarinoow;

import fr.zarinoow.AnnounceProperties;

public class GuildProperties {

    private final Long guildID;

    private AnnounceProperties announceProperties = new AnnounceProperties();

    public GuildProperties(Long guildID) {
        this.guildID = guildID;
    }

    public Long getGuildID() {
        return guildID;
    }

    public AnnounceProperties getAnnounceProperties() {
        return announceProperties;
    }

}
