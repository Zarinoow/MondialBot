package fr.zarinoow.commands;

import com.deepl.api.DeepLException;
import com.deepl.api.Language;
import com.google.gson.JsonSyntaxException;
import fr.foxelia.tools.jda.json.embed.JSONEmbedBuilder;
import fr.zarinoow.AnnounceProperties;
import fr.zarinoow.GuildProperties;
import fr.zarinoow.JSONFileReader;
import fr.zarinoow.MondialBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class AnnounceCommand extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        // Verify if the command is the announce command
        SlashCommandInteraction command = e.getInteraction();
        if(!command.getName().equalsIgnoreCase("announce")) return;

        // Verify if the user is allowed to use the command
        if(!command.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            command.reply("You don't have the permission to use this command !").setEphemeral(true).queue();
            return;
        }

        // Verify if the command has arguments
        if(command.getSubcommandName() == null) {
            command.reply("You need to specify a subcommand !").setEphemeral(true).queue();
            return;
        }

        // Code by subcommand
        switch(command.getSubcommandName()) {
            case "byid":
                byID(command);
                break;
            case "lastmessage":
                byLastMessage(command);
                break;
            case "embed":
                byEmbed(command);
                break;
            case "setchannel":
                setChannel(command);
                break;
            default:
                command.reply("This subcommand doesn't exist !").setEphemeral(true).queue();
                break;
        }

    }

    private void byLastMessage(SlashCommandInteraction command) {
        // Get the last message
        sendSendingMessage(command);
        command.getMessageChannel().getHistory().retrievePast(1).queue(message ->  {
            sendMessage(message.get(0).getContentRaw(), command);
        });

    }

    private void byID(SlashCommandInteraction command) {
        // Check if the ID is a number
        long messageID;
        try {
            messageID = Long.parseLong(command.getOption("id").getAsString());
        } catch (NumberFormatException e) {
            command.reply("The ID must be a number !").setEphemeral(true).queue();
            return;
        }

        // Search for the message
        command.reply("Searching for the message...").queue();
        command.getChannel().retrieveMessageById(messageID).queue(message -> {
            sendMessage(message.getContentRaw(), command);
        });

    }

    private void byEmbed(SlashCommandInteraction command) {
        // Code
        command.getMessageChannel().getHistory().retrievePast(1).queue(messages -> {
            if(!messages.get(0).getAttachments().isEmpty()) {
                for(Message.Attachment attachment : messages.get(0).getAttachments()) {
                    // Limit size to 1MiB
                    if(attachment.getSize() > 1048576) {
                        command.reply("One of sended file is too big ! The limit is 1MiB").setEphemeral(true).queue();
                        continue;
                    }

                    // Limit to txt and json files
                    if(attachment.getFileExtension().equalsIgnoreCase("txt") || attachment.getFileExtension().equalsIgnoreCase("json")) {
                        attachment.getProxy().download().thenAccept(file -> {
                            // Send the message
                            sendJSONMessage(JSONFileReader.getJSONAsString(file), command);
                        });
                    }
                }
            } else {
                sendJSONMessage(messages.get(0).getContentRaw(), command);
            }
        });
        // Code
    }

    private void sendMessage(String message, SlashCommandInteraction command) {
        GuildProperties gp = MondialBot.getInstance().getGuildProperties(command.getGuild().getIdLong());
        for(Long channelID : gp.getAnnounceProperties().getChannels()) {
            try {
                MondialBot.getInstance().getBot().getJDA().getGuildById(gp.getGuildID()).getNewsChannelById(channelID).sendMessage(MondialBot.getTranslation(message, gp.getAnnounceProperties().getLangForChannel(channelID))).queue();
            } catch (DeepLException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        sendSentMessage(command);
    }

    private void sendJSONMessage(String json, SlashCommandInteraction command) {
        sendSendingMessage(command);
        JSONEmbedBuilder embedBuilder = null;
        try {
            embedBuilder = new JSONEmbedBuilder(json);
        } catch (JsonSyntaxException e) {
            command.getHook().editOriginal("The JSON file is malformed ! You can use https://jsonlint.com/ to check your JSON file").queue();
            return;
        }
        GuildProperties gp = MondialBot.getInstance().getGuildProperties(command.getGuild().getIdLong());
        for(Long channelID : gp.getAnnounceProperties().getChannels()) {
            embedBuilder.setLanguage(gp.getAnnounceProperties().getLangForChannel(channelID));
            MondialBot.getInstance().getBot().getJDA().getGuildById(gp.getGuildID()).getNewsChannelById(channelID).sendMessageEmbeds(embedBuilder.build()).queue();
        }
        sendSentMessage(command);
    }

    private void setChannel(SlashCommandInteraction command) {
        AnnounceProperties properties = MondialBot.getInstance().getGuildProperties(command.getGuild().getIdLong()).getAnnounceProperties();
        long channelID = command.getOption("channel").getAsChannel().getIdLong();
        String lang = command.getOption("lang").getAsString();

        if(!(command.getOption("channel").getAsChannel() instanceof NewsChannel)) {
            command.reply("The channel must be a news channel !").setEphemeral(true).queue();
            return;
        }

        if(lang.equalsIgnoreCase("null")) {
            properties.setLangForChannel(channelID, null);
            MondialBot.getInstance().saveGuildProperties();
            command.reply("Sucessfully removed channel <#" + channelID + "> from the announce command.").queue();
            return;
        }

        try {
            for(Language l : MondialBot.getInstance().getTranslator().getTargetLanguages()) {
                if(l.getCode().equalsIgnoreCase(lang)) {
                    properties.setLangForChannel(channelID, l.getCode());
                    MondialBot.getInstance().saveGuildProperties();
                    command.reply("Sucessfully added channel <#" + channelID + "> to " + l.getCode() + " language.").queue();
                    return;
                }
            }
        } catch (DeepLException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        command.reply("The language " + lang + " doesn't exist !").setEphemeral(true).queue();

    }

    private void sendSentMessage(SlashCommandInteraction command) {
        command.getHook().editOriginal("Message sent !").queue();
    }

    private void sendSendingMessage(SlashCommandInteraction command) {
        command.reply("Sending message...").queue();
    }

}
