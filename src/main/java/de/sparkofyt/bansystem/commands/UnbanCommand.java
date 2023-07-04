package de.sparkofyt.bansystem.commands;

import de.sparkofyt.bansystem.BanSystem;
import de.sparkofyt.bansystem.apis.BanAPI;
import de.sparkofyt.bansystem.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class UnbanCommand extends Command {

    public UnbanCommand() {
        super("unban");
    }

    /* Methods */
    @Override
    public void execute(CommandSender sender, String[] args) {
        // check if sender instanceof player
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new ComponentBuilder(
                    BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.SENDER_IS_NOT_A_PLAYER")
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
            ).create());
            return;
        }

        // get the proxy player
        ProxiedPlayer player = (ProxiedPlayer) sender;

        // check the permission
        if(!player.hasPermission(BanSystem.getInstance().getPermConfig().getString("unban"))) {
            player.sendMessage(new ComponentBuilder(
                    BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.NO_PERMISSION")
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
            ).create());
            return;
        }

        // make sure syntax is good
        if(args.length == 1) {
            // check if player wants to ban himself
            if(args[0].equalsIgnoreCase(player.getName())) {
                player.sendMessage(new ComponentBuilder(
                        BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.BAN_YOURSELF")
                                .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                ).create());
                return;
            }

            // fetch target uuid
            UUID targetUUID = UUIDFetcher.getUUID(args[0]);

            // if fetched uuid == null, target doesn't exist | send error message
            if(targetUUID == null) {
                player.sendMessage(new ComponentBuilder(
                        BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.UNKNOWN_PLAYER")
                                .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                                .replace("%player%", args[0])
                ).create());
                return;
            }

            // check if target is not banned
            if(!BanAPI.isBanned(targetUUID)) {
                player.sendMessage(new ComponentBuilder(
                        BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.NOT_BANNED")
                                .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                ).create());
                return;
            }

            // check if target is perm or temp banned, then remove out of this section
            if(BanAPI.isTempBanned(targetUUID))
                BanAPI.removeTempBan(targetUUID);
            if(BanAPI.isPermBanned(targetUUID))
                BanAPI.removePermBan(targetUUID);

            // send player a message
            player.sendMessage(new ComponentBuilder(
                    BanSystem.getInstance().getMessagesConfig().getString("BanSystem.commands.unban.PLAYER_UNBANNED")
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                            .replace("%player%", args[0])
            ).create());
        } else {
            // invalid syntax
            player.sendMessage(new ComponentBuilder(
                    BanSystem.getInstance().getMessagesConfig().getString("BanSystem.commands.unban.SYNTAX")
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
            ).create());
        }
    }
}
