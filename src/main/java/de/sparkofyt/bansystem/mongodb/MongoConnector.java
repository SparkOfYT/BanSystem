package de.sparkofyt.bansystem.mongodb;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.sparkofyt.bansystem.BanSystem;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.conversions.Bson;

import java.util.Collections;
import java.util.List;

public class MongoConnector {

    /* Variables */
    private MongoClient mongoClient;
    private final String host;
    private final String username;
    private final String password;
    private final String cluster;
    private final String uri;
    private final int port;

    /* Constructor */
    public MongoConnector(String host, int port, String username, String password, String cluster, String uri) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.cluster = cluster;
        this.uri = uri;
    }

    /* Methods */
    public void connect() {
        if(BanSystem.getInstance().getDatabaseConfig().getBoolean("useURI")) {
            String uri = BanSystem.getInstance().getDatabaseConfig().getString("uri");

            ConnectionString connectionString = new ConnectionString(uri);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            mongoClient = MongoClients.create(settings);
        } else {
            MongoCredential credential = MongoCredential.createCredential(username, cluster, password.toCharArray());
            List<ServerAddress> serverAddresses = Collections.singletonList(new ServerAddress(host, port));

            MongoClientSettings settings = MongoClientSettings.builder()
                    .credential(credential)
                    .applyToClusterSettings(builder -> builder.hosts(serverAddresses).build())
                    .build();

            mongoClient = MongoClients.create(settings);
        }

        testConnection();
    }

    public void testConnection() {
        MongoDatabase database = mongoClient.getDatabase("admin");
        Bson command = new BsonDocument("ping", new BsonInt64(1));

        try {
            database.runCommand(command);
        } catch (MongoException e) {
            mongoClient.close();
        }
    }

    /* Getters & Setters */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCluster() {
        return cluster;
    }

    public String getUri() {
        return uri;
    }

    public int getPort() {
        return port;
    }
}
