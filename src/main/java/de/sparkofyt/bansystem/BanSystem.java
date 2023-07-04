package de.sparkofyt.bansystem;

import de.sparkofyt.bansystem.apis.BanAPI;
import de.sparkofyt.bansystem.commands.*;
import de.sparkofyt.bansystem.mongodb.MongoConnector;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public final class BanSystem extends Plugin {

    /* Variables */
    public static BanSystem instance;

    /* Configs*/
    private Configuration databaseConfig;
    private Configuration permConfig;
    private Configuration messagesConfig;

    /* Database */
    private MongoConnector mongoConnector;

    /* Methods */
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        setupMongoConfig();
        setupMessagesConfig();
        setupPermissionConfig();
        setupCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void setupMongoConfig() {
        File dir = new File(this.getDataFolder().getAbsolutePath());
        if(!dir.exists()) dir.mkdirs();

        File mongoConfigFile = new File(this.getDataFolder().getAbsolutePath() + "/mongo.yml");

        try {
            if(!mongoConfigFile.exists()) mongoConfigFile.createNewFile();
            databaseConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(mongoConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(databaseConfig != null) {
            if(databaseConfig.get("uri") == null) {
                databaseConfig.set("useURI", true);
                databaseConfig.set("uri", "");
                databaseConfig.set("host", "");
                databaseConfig.set("port", 27017);
                databaseConfig.set("username", "");
                databaseConfig.set("password", "");
                databaseConfig.set("cluster", "");
            }

            try {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(databaseConfig, mongoConfigFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mongoConnector = new MongoConnector(
                    databaseConfig.getString("host"),
                    databaseConfig.getInt("port"),
                    databaseConfig.getString("username"),
                    databaseConfig.getString("password"),
                    databaseConfig.getString("cluster"),
                    databaseConfig.getString("uri")
            );

            mongoConnector.connect();
            BanAPI.createCollections();
        }
    }

    private void setupMessagesConfig() {
        File dir = new File(this.getDataFolder().getAbsolutePath());
        if(!dir.exists()) dir.mkdirs();

        File messagesConfigFile = new File(this.getDataFolder().getAbsolutePath() + "/messages.yml");

        try {
            if(!messagesConfigFile.exists()) messagesConfigFile.createNewFile();
            messagesConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(messagesConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(messagesConfig != null) {
            if(messagesConfig.get("BanSystem.PREFIX") == null) {
                messagesConfig.set("BanSystem.PREFIX", "§8[§1Ban§3System§8]§r");
                messagesConfig.set("BanSystem.errors.NO_PERMISSION", "%prefix% §cDazu hast du keine Rechte!");
                messagesConfig.set("BanSystem.errors.SENDER_IS_NOT_A_PLAYER", "%prefix% §cDu musst ein Spieler sein, um diesen Befehl auszuführen!");
                messagesConfig.set("BanSystem.errors.UNKNOWN_PLAYER", "%prefix% §cEs gibt keinen Spieler mit dem Namen '§8%player§c'");
                messagesConfig.set("BanSystem.errors.BAN_YOURSELF", "%prefix% §cDu willst dich doch nicht selber Bannen?");
                messagesConfig.set("BanSystem.errors.ALREADY_BANNED", "%prefix% §cDieser Spieler wurde bereits gebannt!");
                messagesConfig.set("BanSystem.errors.NOT_BANNED", "%prefix% §cDieser Spieler wurde nicht gebannt!");
                messagesConfig.set("BanSystem.errors.BAN_ID_IS_INVALID", "%prefix% §cDie BanID §8'%ban_id%' §cexistiert nicht!");
                messagesConfig.set("BanSystem.errors.NEVER_BANNED", "%prefix% §cDieser Spieler wurde noch nie gebannt!");

                messagesConfig.set("BanSystem.commands.ban.SYNTAX", "%prefix% §8Syntax: §3/ban [Spieler] [Grund]");
                messagesConfig.set("BanSystem.commands.ban.PLAYER_BANNED", "%prefix% §8Der Spieler §3%player% §8wurde für '§3%reason%§8' permanent gebannt!");
                messagesConfig.set("BanSystem.commands.ban.KICK_MESSAGE", "%prefix% §cDu wurdest permanent gebannt!");

                messagesConfig.set("BanSystem.commands.tempban.SYNTAX", "%prefix% §8Syntax: §3/tempban [Spieler] [Zeit] [Grund]");
                messagesConfig.set("BanSystem.commands.tempban.PLAYER_BANNED", "%prefix% §8Der Spieler §3%player% §8wurde für '§3%reason%§8' bis §3%date% §8gebannt!");
                messagesConfig.set("BanSystem.commands.tempban.KICK_MESSAGE", "%prefix% §cDu wurdest bis %date% gebannt!");

                messagesConfig.set("BanSystem.commands.unban.SYNTAX", "%prefix% §8Syntax: §3/unban [Spieler]");
                messagesConfig.set("BanSystem.commands.unban.PLAYER_UNBANNED", "%prefix% §8Der Spieler §3%player% §8wurde entbannt!");

                messagesConfig.set("BanSystem.commands.baninfo.SYNTAX", "%prefix% §8Syntax: §3/baninfo [Ban ID]");
                messagesConfig.set("BanSystem.commands.baninfo.PLAYER_MESSAGE.1", "%prefix% §8Ban Info für %ban_id%!");
                messagesConfig.set("BanSystem.commands.baninfo.PLAYER_MESSAGE.2", "%prefix% §8Gebannter Spieler: §3%banned_player%");
                messagesConfig.set("BanSystem.commands.baninfo.PLAYER_MESSAGE.3", "%prefix% §8Gebannt von: §3%banned_by%");
                messagesConfig.set("BanSystem.commands.baninfo.PLAYER_MESSAGE.3", "%prefix% §8Gebannt am: §3%date_of_ban%");
                messagesConfig.set("BanSystem.commands.baninfo.PLAYER_MESSAGE.4", "%prefix% §8Gebannt bis: §3%date_end_of_ban%");
                messagesConfig.set("BanSystem.commands.baninfo.PLAYER_MESSAGE.5", "%prefix% §8Grund: §3%reason%");
                messagesConfig.set("BanSystem.commands.baninfo.PLAYER_MESSAGE.6", "%prefix% §8Permanent: §3%permanent2%");

                messagesConfig.set("BanSystem.commands.history.SYNTAX", "%prefix% §8Syntax: §3/history [Spieler]");
                messagesConfig.set("BanSystem.commands.history.PLAYER_MESSAGE.1", "§8--------------------");
                messagesConfig.set("BanSystem.commands.history.PLAYER_MESSAGE.2", "%prefix% §8Gebannter Spieler: §3%banned_player%");
                messagesConfig.set("BanSystem.commands.history.PLAYER_MESSAGE.3", "%prefix% §8Gebannt von: §3%banned_by%");
                messagesConfig.set("BanSystem.commands.history.PLAYER_MESSAGE.3", "%prefix% §8Gebannt am: §3%date_of_ban%");
                messagesConfig.set("BanSystem.commands.history.PLAYER_MESSAGE.4", "%prefix% §8Gebannt bis: §3%date_end_of_ban%");
                messagesConfig.set("BanSystem.commands.history.PLAYER_MESSAGE.5", "%prefix% §8Grund: §3%reason%");
                messagesConfig.set("BanSystem.commands.history.PLAYER_MESSAGE.6", "%prefix% §8Permanent: §3%permanent2%");
                messagesConfig.set("BanSystem.commands.history.PLAYER_MESSAGE.7", "§8--------------------");
            }

            try {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(messagesConfig, messagesConfigFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupPermissionConfig() {
        File dir = new File(this.getDataFolder().getAbsolutePath());
        if(!dir.exists()) dir.mkdirs();

        File permissionsConfigFile = new File(this.getDataFolder().getAbsolutePath() + "/permissions.yml");

        try {
            if(!permissionsConfigFile.exists()) permissionsConfigFile.createNewFile();
            permConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(permissionsConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(permConfig != null) {
            if(permConfig.get("ban") == null) {
                permConfig.set("ban", "ban_system.ban");
                permConfig.set("tempban", "ban_system.tempban");
                permConfig.set("unban", "ban_system.unban");
                permConfig.set("history", "ban_system.history");
                permConfig.set("baninfo", "ban_system.ban_info");
            }

            try {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(permConfig, permissionsConfigFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupCommands() {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BanCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new TempBanCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new UnbanCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BanInfoCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new HistoryCommand());
    }

    /* Getters & Setters */
    public static BanSystem getInstance() {
        return instance;
    }

    public Configuration getDatabaseConfig() {
        return databaseConfig;
    }

    public Configuration getMessagesConfig() {
        return messagesConfig;
    }

    public Configuration getPermConfig() {
        return permConfig;
    }

    public MongoConnector getMongoConnector() {
        return mongoConnector;
    }
}
