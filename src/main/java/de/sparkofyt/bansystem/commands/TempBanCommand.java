package de.sparkofyt.bansystem.commands;

import de.sparkofyt.bansystem.BanSystem;
import de.sparkofyt.bansystem.apis.BanAPI;
import de.sparkofyt.bansystem.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TempBanCommand extends Command {

    public TempBanCommand() {
        super("tempban");
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
        if(!player.hasPermission(BanSystem.getInstance().getPermConfig().getString("tempban"))) {
            player.sendMessage(new ComponentBuilder(
                    BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.NO_PERMISSION")
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
            ).create());
            return;
        }

        // make sure syntax is good
        if(args.length > 2) {
            // check if player wants to ban himself
            if(args[0].equalsIgnoreCase(player.getName())) {
                player.sendMessage(new ComponentBuilder(
                        BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.BAN_YOURSELF")
                                .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                ).create());
                return;
            }

            // get the target proxy player
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);

            // check if target is online
            if(target != null) {
                // error in time string
                if(BanAPI.convertTimeStringToMillis(args[1]) == 0) {
                    player.sendMessage(new ComponentBuilder(
                            BanSystem.getInstance().getMessagesConfig().getString("BanSystem.commands.tempban.SYNTAX")
                                    .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                    ).create());
                }

                // get millis until end of ban
                long millis = System.currentTimeMillis() + BanAPI.convertTimeStringToMillis(args[1]);

                // ban target
                BanAPI.tempBanPlayer(target.getUniqueId(), player.getUniqueId(), millis, getReason(args));

                // send player a message
                player.sendMessage(new ComponentBuilder(
                        BanSystem.getInstance().getMessagesConfig().getString("BanSystem.commands.tempban.PLAYER_BANNED")
                                .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                                .replace("%player%", args[0])
                                .replace("%reason%", getReason(args))
                                .replace("%date%", getDateFormattedString(millis))
                ).create());

                // kick player
                target.disconnect(new ComponentBuilder(
                        BanSystem.getInstance().getMessagesConfig().getString("BanSystem.commands.tempban.KICK_MESSAGE")
                                .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                                .replace("%date%", getDateFormattedString(millis))
                ).create());
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

                // check if target is already banned
                if(BanAPI.isBanned(targetUUID)) {
                    player.sendMessage(new ComponentBuilder(
                            BanSystem.getInstance().getMessagesConfig().getString("BanSystem.errors.ALREADY_BANNED")
                                    .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                    ).create());
                    return;
                }

                // error in time string
                if(BanAPI.convertTimeStringToMillis(args[1]) == 0) {
                    player.sendMessage(new ComponentBuilder(
                            BanSystem.getInstance().getMessagesConfig().getString("BanSystem.commands.tempban.SYNTAX")
                                    .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                    ).create());
                }

                // get millis until end of ban
                long millis = System.currentTimeMillis() + BanAPI.convertTimeStringToMillis(args[1]);

                // ban target
                BanAPI.tempBanPlayer(targetUUID, player.getUniqueId(), millis, getReason(args));

                // send player a message
                player.sendMessage(new ComponentBuilder(
                        BanSystem.getInstance().getMessagesConfig().getString("BanSystem.commands.tempban.PLAYER_BANNED")
                                .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
                                .replace("%player%", args[0])
                                .replace("%reason%", getReason(args))
                                .replace("%date%", getDateFormattedString(millis))
                ).create());
            }
        } else {
            // invalid syntax
            player.sendMessage(new ComponentBuilder(
                    BanSystem.getInstance().getMessagesConfig().getString("BanSystem.commands.ban.SYNTAX")
                            .replace("%prefix%", BanSystem.getInstance().getMessagesConfig().getString("BanSystem.PREFIX"))
            ).create());
        }
    }

    /* Utils */
    private String getReason(String[] args) {
        StringBuilder reason = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if(i != args.length -1)
                reason.append(args[i]).append(" ");
            else
                reason.append(args[i]);
        }

        return reason.toString();
    }

    private String getDateFormattedString(long millis) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");
        ZonedDateTime dateTime = Instant.ofEpochMilli(millis).atZone(ZoneId.of("Europe/Berlin"));
        return dateTime.format(dtf);
    }
}
