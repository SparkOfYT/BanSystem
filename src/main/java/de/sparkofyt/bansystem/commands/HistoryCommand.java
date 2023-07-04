package de.sparkofyt.bansystem.commands;

import de.sparkofyt.bansystem.BanSystem;
import de.sparkofyt.bansystem.apis.BanAPI;
import de.sparkofyt.bansystem.apis.BanInfo;
import de.sparkofyt.bansystem.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class HistoryCommand extends Command {

    public HistoryCommand() {
        super("history");
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
        if(!player.hasPermission(BanSystem.getInstance().getPermConfig().getString("history"))) {
            player.sendMessage(new ComponentBuilder(
                    BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.NO_PERMISSION")
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
            ).create());
            return;
        }

        // make sure syntax is good
        if(args.length == 1) {
            // get the target proxy player
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);

            // check if target is online
            if(target != null) {
                // never got banned
                if(BanAPI.getPlayerBanHistory(target.getUniqueId()).size() == 0) {
                    player.sendMessage(new ComponentBuilder(
                            BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.NEVER_BANNED")
                                    .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                    ).create());
                    return;
                }

                 // get ban history of target
                List<BanInfo> banHistory = BanAPI.getPlayerBanHistory(target.getUniqueId());

                // send player messages
                for(BanInfo ban : banHistory) {
                    sendBanInfo(player, ban);
                }
            } else {
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

                // never got banned
                if(BanAPI.getPlayerBanHistory(targetUUID).size() == 0) {
                    player.sendMessage(new ComponentBuilder(
                            BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.NEVER_BANNED")
                                    .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                    ).create());
                    return;
                }

                // get ban history of target
                List<BanInfo> banHistory = BanAPI.getPlayerBanHistory(targetUUID);

                // send player messages
                for(BanInfo ban : banHistory) {
                    sendBanInfo(player, ban);
                }
            }
        } else {
            // invalid syntax
            player.sendMessage(new ComponentBuilder(
                    BanSystem.getInstance().getMessagesConfig().getString("BanSystem.commands.history.SYNTAX")
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
            ).create());
        }
    }

    /* Utils */
    private void sendBanInfo(ProxiedPlayer player, BanInfo banInfo) {
        Configuration configuration = BanSystem.getInstance().getMessagesConfig().getSection("BanSystem.commands.history.PLAYER_MESSAGE");
        for (int i = 0; i < configuration.getKeys().size(); i++) {
            player.sendMessage(new ComponentBuilder(
                    configuration.getString(String.valueOf(i + 1))
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                            .replace("%banned_player%", UUIDFetcher.getName(banInfo.getBannedPlayerUUID()))
                            .replace("%banned_by%", UUIDFetcher.getName(banInfo.getBannerPlayerUUID()))
                            .replace("%date_of_ban%", getDateFormattedString(banInfo.getTimeStampOfBan()))
                            .replace("%date_end_of_ban%", getDateFormattedString(banInfo.getTimeStampEndOfBan()))
                            .replace("%reason%", banInfo.getReason())
                            .replace("%permanent%", convertBoolToString(banInfo.isPermanent()))
            ).create());
        }
    }

    private String convertBoolToString(boolean bool) {
        if(bool) return "True";
        else return "False";
    }

    private String getDateFormattedString(long millis) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");
        ZonedDateTime dateTime = Instant.ofEpochMilli(millis).atZone(ZoneId.of("Europe/Berlin"));

        if(dateTime.format(dtf).equalsIgnoreCase("01:00:00 01.01.1970"))
            return "PERMANENT";

        return dateTime.format(dtf);
    }
}
