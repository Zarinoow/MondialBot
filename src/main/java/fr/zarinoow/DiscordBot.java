package fr.zarinoow;

import fr.zarinoow.commands.AnnounceCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;
import java.util.logging.Logger;

public class DiscordBot {

    private final String token;

    private JDA bot;

    public DiscordBot(String token) {
        this.token = token;
    }

    private void buildBot() {
        JDABuilder builder = JDABuilder.createDefault(token, GatewayIntent.MESSAGE_CONTENT).disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS);
        try {
            builder.setActivity(Activity.of(Activity.ActivityType.valueOf(BotConfig.getString("activity.type").toUpperCase()), BotConfig.getString("activity.message")));
        } catch (IllegalArgumentException e) {
            Logger.getLogger("MondialBot").warning("Invalid activity type, setting to default...");
            builder.setActivity(Activity.competing("MondialBot"));
        }
        builder.addEventListeners(new AnnounceCommand());
        bot = builder.build();
    }

    public void start() {
        buildBot();
    }

    public void stop() {
        bot.shutdown();
    }

    public JDA getJDA() {
        return bot;
    }

    public void setupCommands() {
        List<Command> cmds = bot.retrieveCommands().complete();
        List<String> cmdNames = cmds.stream().map(Command::getName).toList();

        // cmds.forEach(cmd -> bot.deleteCommandById(cmd.getId()).queue());

        if(!cmdNames.contains("announce")) {
            SubcommandData byID = new SubcommandData("byid", "Copy the raw message by ID").addOption(OptionType.STRING, "id", "The ID of the message", true);
            SubcommandData lastMessage = new SubcommandData("lastmessage", "Copy the raw message of the last message");
            SubcommandData embed = new SubcommandData("embed", "Send an embed message");
            SubcommandData setChannel = new SubcommandData("setchannel", "Set the channel for the announce command").addOption(OptionType.CHANNEL, "channel", "The channel to send the announce", true).addOption(OptionType.STRING, "lang", "The language of the announce", true);
            getJDA().upsertCommand("announce", "Make an announcement and translate it in all languages").addSubcommands(byID, lastMessage, embed, setChannel).queue();
            Logger.getLogger("MondialBot").info("Added announce command !");
        }

    }

}
