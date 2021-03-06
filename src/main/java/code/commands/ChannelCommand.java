package code.commands;

import code.Manager;
import code.bukkitutils.SoundManager;
import code.cache.UserData;
import code.methods.GroupMethod;
import code.methods.player.PlayerMessage;
import code.registry.ConfigManager;
import code.utils.Configuration;
import code.utils.module.ModuleCheck;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChannelCommand implements CommandClass {

    private Manager manager;

    public ChannelCommand(Manager manager) {
        this.manager = manager;
    }

    @Command(names = {"channel", "chn"})
    public boolean onCommand(@Sender Player player, @OptArg String arg1, @OptArg String arg2) {
        PlayerMessage playersender = manager.getPlayerMethods().getSender();

        GroupMethod groupChannel = manager.getPlayerMethods().getGroupMethod();

        SoundManager sound = manager.getManagingCenter().getSoundManager();
        ModuleCheck moduleCheck = manager.getPathManager();

        ConfigManager files = manager.getFiles();

        Configuration config = files.getConfig();
        Configuration command = files.getCommand();
        Configuration messages = files.getMessages();

        if (!(moduleCheck.isCommandEnabled("channel"))) {
            moduleCheck.sendDisableMessage(player, "channel");
            return true;
        }

        UUID playeruuid = player.getUniqueId();

        if (arg1 == null){
            playersender.sendMessage(player, messages.getString("error.no-arg"));
            moduleCheck.getUsage(player, "channel" , "join, quit, list");
            sound.setSound(playeruuid, "sounds.error");
            return true;
        }



        UserData userData = manager.getCache().getPlayerUUID().get(player.getUniqueId());
        if (arg1.equalsIgnoreCase("join")){
            if (arg2 == null){
                playersender.sendMessage(player, messages.getString("error.no-arg"));
                moduleCheck.getUsage(player, "channel" , arg1, "<channel>");
                sound.setSound(playeruuid, "sounds.error");
                return true;
            }

            if (groupChannel.channelNotExists(arg2)){
               playersender.sendMessage(player, messages.getString("error.channel.no-exists"));
               sound.setSound(playeruuid, "sounds.error");
               return true;
            }

            if (!(groupChannel.isChannelEnabled(arg2))){
                playersender.sendMessage(player, messages.getString("error.channel.disabled"));
                sound.setSound(playeruuid, "sounds.error");
                return true;
            }


            if (userData.equalsChannelGroup(arg2)){
                playersender.sendMessage(player, messages.getString("error.channel.joined")
                        .replace("%rank%", userData.getChannelGroup()));
                sound.setSound(playeruuid, "sounds.error");
                return true;
            }

            if (!(groupChannel.hasGroupPermission(player, arg2))){
                playersender.sendMessage(player, messages.getString("error.no-perms"));
                return true;
            }

            playersender.sendMessage(player, command.getString("commands.channel.player.left")
                    .replace("%beforechannel%", userData.getChannelGroup())
                    .replace("%afterchannel%", arg2));

            userData.setChannelGroup(arg2);
            playersender.sendMessage(player, command.getString("commands.channel.player.join")
                    .replace("%channel%", userData.getChannelGroup()));
            return true;
        }
        if (arg1.equalsIgnoreCase("quit")) {

            if (userData.equalsChannelGroup("default")){
                playersender.sendMessage(player, messages.getString("error.channel.default"));
                return true;
            }

            playersender.sendMessage(player, command.getString("commands.channel.player.left")
                    .replace("%channel%", userData.getChannelGroup()));
            userData.setChannelGroup(arg2);
            return true;
        }

        if (arg1.equalsIgnoreCase("list")){
            playersender.sendMessage(player, command.getString("commands.channel.list.message"));
            playersender.sendMessage(player, command.getString("commands.channel.list.space"));
            for (String group : groupChannel.getGroup()){
                if (group.equalsIgnoreCase("default")){
                    playersender.sendMessage(player, command.getString("commands.channel.list.format")
                            .replace("%group%", group)
                            .replace("%mode%", "&e[Default]"));
                    continue;
                }
                if (groupChannel.isChannelEnabled(group)){
                    playersender.sendMessage(player, command.getString("commands.channel.list.format")
                            .replace("%group%", group)
                            .replace("%mode%", "&a[Enabled]"));
                }else{
                    playersender.sendMessage(player, command.getString("commands.channel.list.format")
                            .replace("%group%", group)
                            .replace("%mode%", "&c[Disabled]"));
                }
            }
            playersender.sendMessage(player, command.getString("commands.channel.list.space"));
            return true;
        }else{
            playersender.sendMessage(player, messages.getString("error.unknown-arg"));
            moduleCheck.getUsage(player, "bmsg" , "join, quit, list");
        }
        return true;
    }
}
