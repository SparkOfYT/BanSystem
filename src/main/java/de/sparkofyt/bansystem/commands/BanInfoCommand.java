package de.sparkofyt.bansystem.commands;

import de.sparkofyt.bansystem.BanSystem;
import de.sparkofyt.bansystem.apis.BanAPI;
import de.sparkofyt.bansystem.apis.BanInfo;
import de.sparkofyt.bansystem.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class BanInfoCommand extends Command {

    public BanInfoCommand() {
        super("baninfo");
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
        if(!player.hasPermission(BanSystem.getInstance().getPermConfig().getString("baninfo"))) {
            player.sendMessage(new ComponentBuilder(
                    BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.NO_PERMISSION")
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
            ).create());
            return;
        }

        // make sure syntax is good
        if(args.length == 1) {
            // check if banID is not valid
            if(BanAPI.getBanInfo(args[0]) == null) {
                player.sendMessage(new ComponentBuilder(
                        BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.BAN_ID_IS_INVALID")
                                .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                                .replace("%ban_id%", args[0])
                ).create());
                return;
            }

            // get banInfo
            BanInfo banInfo = BanAPI.getBanInfo(args[0]);

            // send player a message
            sendBanInfo(player, banInfo);
        } else {
            // invalid syntax
            player.sendMessage(new ComponentBuilder(
                    BanSystem.getInstance().getMessagesConfig().getString("BanSystem.commands.unban.SYNTAX")
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
            ).create());
        }
    }

    /* Utils */
    private void sendBanInfo(ProxiedPlayer player, BanInfo banInfo) {
        Configuration configuration = BanSystem.getInstance().getMessagesConfig().getSection("BanSystem.commands.baninfo.PLAYER_MESSAGE");
        for (int i = 0; i < configuration.getKeys().size(); i++) {
            player.sendMessage(new ComponentBuilder(
                    configuration.getString(String.valueOf(i))
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                            .replace("%ban_id%", banInfo.getBanID())
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
        return dateTime.format(dtf);
    }
}
