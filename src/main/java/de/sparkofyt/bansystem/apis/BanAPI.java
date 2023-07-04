package de.sparkofyt.bansystem.apis;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import de.sparkofyt.bansystem.BanSystem;
import de.sparkofyt.bansystem.utils.UUIDFetcher;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BanAPI {

    /* Variables */
    private static final String DATABASE_NAME = "ban_system";
    private static final String PERM_COLLECTION_NAME = "permanent_banned_players";
    private static final String TEMP_COLLECTION_NAME = "temp_banned_player";
    private static final String ALL_COLLECTION_NAME = "all_player_bans";

    /* Setup Methods */
    public static void createCollections() {
        MongoDatabase db = BanSystem.getInstance().getMongoConnector().getMongoClient().getDatabase(DATABASE_NAME);

        try {
            db.createCollection(PERM_COLLECTION_NAME);
            db.createCollection(TEMP_COLLECTION_NAME);
            db.createCollection(ALL_COLLECTION_NAME);
        } catch (MongoException ignored) {}
    }

    /* API Methods */
    public static boolean isBanned(UUID uuid) {
        return (isTempBanned(uuid) || isPermBanned(uuid));
    }
    public static boolean isTempBanned(UUID uuid) {
        // get 'ban_system' database
        MongoDatabase db = BanSystem.getInstance().getMongoConnector().getMongoClient().getDatabase(DATABASE_NAME);

        // get both the permanent and temporarily collection
        MongoCollection<Document> tempCollection = db.getCollection(TEMP_COLLECTION_NAME);

        // init our search query
        Document searchQuery = new Document();
        searchQuery.put("banned_player_details.uuid", uuid.toString());

        // get all documents with query
        FindIterable<Document> tempCursor = tempCollection.find(searchQuery);

        // check if we found something
        try(final MongoCursor<Document> cursorIterator = tempCursor.cursor()) {
            if(cursorIterator.hasNext()) return true;
        }

        // found nothing
        return false;
    }
    public static boolean isPermBanned(UUID uuid) {
        // get 'ban_system' database
        MongoDatabase db = BanSystem.getInstance().getMongoConnector().getMongoClient().getDatabase(DATABASE_NAME);

        // get both the permanent and temporarily collection
        MongoCollection<Document> permCollection = db.getCollection(PERM_COLLECTION_NAME);

        // init our search query
        Document searchQuery = new Document();
        searchQuery.put("banned_player_details.uuid", uuid.toString());

        // get all documents with query
        FindIterable<Document> permCursor = permCollection.find(searchQuery);

        // check if we found something
        try(final MongoCursor<Document> cursorIterator = permCursor.cursor()) {
            if(cursorIterator.hasNext()) return true;
        }

        // found nothing
        return false;
    }

    public static void permaBanPlayer(UUID playerToBan, UUID playerWhoBanned, String reason) {
        if(!isBanned(playerToBan)) {
            // get 'ban_system' database
            MongoDatabase db = BanSystem.getInstance().getMongoConnector().getMongoClient().getDatabase(DATABASE_NAME);

            // get both the permanent and all collection
            MongoCollection<Document> permaCollection = db.getCollection(PERM_COLLECTION_NAME);
            MongoCollection<Document> allCollection = db.getCollection(ALL_COLLECTION_NAME);

            // store our generated unique ban id
            String banID = generateUniqueBanID();

            // generate our documents, and check for errors which happens when any player is null
            Document permaDoc = generatePermaDocument(banID, playerToBan, playerWhoBanned, reason);
            if(permaDoc == null) {
                System.out.println("BanSystem, ERROR in 'permaBanPlayer'");
                return;
            }

            Document allDoc = generateAllDocument(banID, playerToBan, playerWhoBanned, reason, 0L, true);
            if(allDoc == null) {
                System.out.println("BanSystem, ERROR in 'permaBanPlayer'");
                return;
            }

            // insert documents
            permaCollection.insertOne(permaDoc);
            allCollection.insertOne(allDoc);
        }
    }

    public static void tempBanPlayer(UUID playerToBan, UUID playerWhoBanned, long banUntilTimeStamp, String reason) {
        if(!isBanned(playerToBan)) {
            // Get 'ban_system' database
            MongoDatabase db = BanSystem.getInstance().getMongoConnector().getMongoClient().getDatabase(DATABASE_NAME);

            // get both the temp and all collection
            MongoCollection<Document> tempCollection = db.getCollection(TEMP_COLLECTION_NAME);
            MongoCollection<Document> allCollection = db.getCollection(ALL_COLLECTION_NAME);

            // store our generated unique ban id
            String banID = generateUniqueBanID();

            // generate our documents, and check for errors which happens when any player is null
            Document tempDoc = generateTempDocument(banID, playerToBan, playerWhoBanned, banUntilTimeStamp, reason);
            if(tempDoc == null) {
                System.out.println("BanSystem, ERROR in 'banPlayer'");
                return;
            }

            Document allDoc = generateAllDocument(banID, playerToBan, playerWhoBanned, reason, banUntilTimeStamp, false);
            if(allDoc == null) {
                System.out.println("BanSystem, ERROR in 'banPlayer'");
                return;
            }

            // insert documents
            tempCollection.insertOne(tempDoc);
            allCollection.insertOne(allDoc);
        }
    }

    public static void removeTempBan(UUID playerWhoGotBanned) {
        if(isBanned(playerWhoGotBanned)) {
            // get 'ban_system' database
            MongoDatabase db = BanSystem.getInstance().getMongoConnector().getMongoClient().getDatabase(DATABASE_NAME);

            // get the temp collection
            MongoCollection<Document> collection = db.getCollection(TEMP_COLLECTION_NAME);

            // init our search query
            Document searchQuery = new Document();
            searchQuery.put("banned_player_details.uuid", playerWhoGotBanned.toString());

            // get all documents with query
            FindIterable<Document> cursor = collection.find(searchQuery);

            // check if we found something
            try(final MongoCursor<Document> cursorIterator = cursor.cursor()) {
                if(cursorIterator.hasNext()) {
                    collection.deleteOne(searchQuery);
                }
            }
        }
    }

    public static void removePermBan(UUID playerWhoGotBanned) {
        if(isBanned(playerWhoGotBanned)) {
            // get 'ban_system' database
            MongoDatabase db = BanSystem.getInstance().getMongoConnector().getMongoClient().getDatabase(DATABASE_NAME);

            // get the temp collection
            MongoCollection<Document> collection = db.getCollection(PERM_COLLECTION_NAME);

            // init our search query
            Document searchQuery = new Document();
            searchQuery.put("banned_player_details.uuid", playerWhoGotBanned.toString());

            // get all documents with query
            FindIterable<Document> cursor = collection.find(searchQuery);

            // check if we found something
            try(final MongoCursor<Document> cursorIterator = cursor.cursor()) {
                if(cursorIterator.hasNext()) {
                    collection.deleteOne(searchQuery);
                }
            }
        }
    }

    public static List<BanInfo> getPlayerBanHistory(UUID player){
        // init ban info list
        List<BanInfo> bans = new ArrayList<>();

        // get 'ban_system' database
        MongoDatabase db = BanSystem.getInstance().getMongoConnector().getMongoClient().getDatabase(DATABASE_NAME);

        // get the temp collection
        MongoCollection<Document> collection = db.getCollection(ALL_COLLECTION_NAME);

        // init our search query
        Document searchQuery = new Document();
        searchQuery.put("banned_player_details.uuid", player.toString());

        // get all documents with query
        FindIterable<Document> cursor = collection.find(searchQuery);

        // check if we found something
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                Document doc = cursorIterator.next();

                JSONObject documentObject = new JSONObject(doc.toJson());
                JSONObject bannedPlayerDetailsObject = documentObject.getJSONObject("banned_player_details");
                JSONObject bannerPlayerDetailsObject = documentObject.getJSONObject("banner_player_details");
                JSONObject banDetailsObject = documentObject.getJSONObject("ban_details");

                String ban_id = documentObject.getString("ban_id");
                UUID bannedPlayerUUID = UUID.fromString(bannedPlayerDetailsObject.getString("uuid"));
                UUID bannerPlayerUUID = UUID.fromString(bannerPlayerDetailsObject.getString("uuid"));
                String reason = banDetailsObject.getString("reason");
                long timeStampOfBan = Long.parseLong(banDetailsObject.getJSONObject("ban_timestamp").getString("$numberLong"));
                long timeStampEndOfBan = Long.parseLong(banDetailsObject.getJSONObject("ban_until_timestamp").getString("$numberLong"));
                boolean permanent = banDetailsObject.getBoolean("permanent");

                bans.add(
                        new BanInfo(ban_id, bannedPlayerUUID, bannerPlayerUUID, reason, timeStampOfBan, timeStampEndOfBan, permanent)
                );
            }
        }

        return bans;
    }

    public static BanInfo getBanInfo(String banID) {
        // get 'ban_system' database
        MongoDatabase db = BanSystem.getInstance().getMongoConnector().getMongoClient().getDatabase(DATABASE_NAME);

        // get the temp collection
        MongoCollection<Document> collection = db.getCollection(ALL_COLLECTION_NAME);

        // init our search query
        Document searchQuery = new Document();
        searchQuery.put("ban_id", banID);

        // get all documents with query
        FindIterable<Document> cursor = collection.find(searchQuery);

        // check if we found something
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            if(cursorIterator.hasNext()) {
                Document doc = cursorIterator.next();

                JSONObject documentObject = new JSONObject(doc.toJson());
                JSONObject bannedPlayerDetailsObject = documentObject.getJSONObject("banned_player_details");
                JSONObject bannerPlayerDetailsObject = documentObject.getJSONObject("banner_player_details");
                JSONObject banDetailsObject = documentObject.getJSONObject("ban_details");

                String ban_id = documentObject.getString("ban_id");
                UUID bannedPlayerUUID = UUID.fromString(bannedPlayerDetailsObject.getString("uuid"));
                UUID bannerPlayerUUID = UUID.fromString(bannerPlayerDetailsObject.getString("uuid"));
                String reason = banDetailsObject.getString("reason");
                long timeStampOfBan = Long.parseLong(banDetailsObject.getJSONObject("ban_timestamp").getString("$numberLong"));
                long timeStampEndOfBan = Long.parseLong(banDetailsObject.getJSONObject("ban_until_timestamp").getString("$numberLong"));
                boolean permanent = banDetailsObject.getBoolean("permanent");

                return new BanInfo(ban_id, bannedPlayerUUID, bannerPlayerUUID, reason, timeStampOfBan, timeStampEndOfBan, permanent);
            }
        }

        return null;
    }

    /* Utils Methods */
    public static int convertTimeStringToMillis(String time) {
        Pattern pattern = Pattern.compile("(\\d+)([ydhms])");
        Matcher matcher = pattern.matcher(time);

        int total = 0;
        while(matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "y" -> total += value * 365 * 24 * 60 * 60;
                case "d" -> total += value * 24 * 60 * 60;
                case "h" -> total += value * 60 * 60;
                case "m" -> total += value * 60;
                case "s" -> total += value;
            }
        }

        if(total == 0) return 0;
        return total * 1000;
    }
    public static String generateUniqueBanID() {
        MongoDatabase db = BanSystem.getInstance().getMongoConnector().getMongoClient().getDatabase(DATABASE_NAME);
        MongoCollection<Document> collection = db.getCollection(ALL_COLLECTION_NAME);

        List<String> banIDs = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                banIDs.add(doc.getString("ban_id"));
            }
        }

        String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String randomBanID;
        do {
            StringBuilder stringBuilder = new StringBuilder(5);
            Random random = new Random();

            for (int i = 0; i < 5; i++) {
                char randomChar = ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length()));
                stringBuilder.append(randomChar);
            }

            randomBanID = stringBuilder.toString();
        } while (banIDs.contains(randomBanID));

        return randomBanID;
    }
    public static Document generatePermaDocument(String ban_id, UUID playerToBan, UUID playerWhoBanned, String reason) {
        if(UUIDFetcher.getName(playerToBan) == null || UUIDFetcher.getName(playerWhoBanned) == null) {
            System.out.println("BanSystem, ERROR in 'generatePermaDocument'");
            return null;
        }

        Document document = new Document();
        document.append("ban_id", ban_id);

        BasicDBObject bannedPlayerDetails = new BasicDBObject();
        bannedPlayerDetails.append("uuid", playerToBan.toString());
        bannedPlayerDetails.append("name", "" + UUIDFetcher.getName(playerToBan));
        document.append("banned_player_details", bannedPlayerDetails);

        BasicDBObject bannerPlayerDetails = new BasicDBObject();
        bannerPlayerDetails.append("uuid", playerWhoBanned.toString());
        bannerPlayerDetails.append("name", "" + UUIDFetcher.getName(playerWhoBanned));
        document.append("banner_player_details", bannerPlayerDetails);

        BasicDBObject banDetails = new BasicDBObject();
        banDetails.append("reason", reason);
        banDetails.append("ban_timestamp", System.currentTimeMillis());
        document.append("ban_details", banDetails);

        return document;
    }
    public static Document generateTempDocument(String ban_id, UUID playerToBan, UUID playerWhoBanned, long banUntilTimeStamp, String reason) {
        if(UUIDFetcher.getName(playerToBan) == null || UUIDFetcher.getName(playerWhoBanned) == null) {
            System.out.println("BanSystem, ERROR in 'generatePermaDocument'");
            return null;
        }

        Document document = new Document();
        document.append("ban_id", ban_id);

        BasicDBObject bannedPlayerDetails = new BasicDBObject();
        bannedPlayerDetails.append("uuid", playerToBan.toString());
        bannedPlayerDetails.append("name", "" + UUIDFetcher.getName(playerToBan));
        document.append("banned_player_details", bannedPlayerDetails);

        BasicDBObject bannerPlayerDetails = new BasicDBObject();
        bannerPlayerDetails.append("uuid", playerWhoBanned.toString());
        bannerPlayerDetails.append("name", "" + UUIDFetcher.getName(playerWhoBanned));
        document.append("banner_player_details", bannerPlayerDetails);

        BasicDBObject banDetails = new BasicDBObject();
        banDetails.append("reason", reason);
        banDetails.append("ban_timestamp", System.currentTimeMillis());
        banDetails.append("ban_until_timestamp", banUntilTimeStamp);
        document.append("ban_details", banDetails);

        return document;
    }
    public static Document generateAllDocument(String ban_id, UUID playerToBan, UUID playerWhoBanned, String reason, long banUntilTimeStamp, boolean perma) {
        if(UUIDFetcher.getName(playerToBan) == null || UUIDFetcher.getName(playerWhoBanned) == null) {
            System.out.println("BanSystem, ERROR in 'generatePermaDocument'");
            return null;
        }

        Document document = new Document();
        document.append("ban_id", ban_id);

        BasicDBObject bannedPlayerDetails = new BasicDBObject();
        bannedPlayerDetails.append("uuid", playerToBan.toString());
        bannedPlayerDetails.append("name", "" + UUIDFetcher.getName(playerToBan));
        document.append("banned_player_details", bannedPlayerDetails);

        BasicDBObject bannerPlayerDetails = new BasicDBObject();
        bannerPlayerDetails.append("uuid", playerWhoBanned.toString());
        bannerPlayerDetails.append("name", "" + UUIDFetcher.getName(playerWhoBanned));
        document.append("banner_player_details", bannerPlayerDetails);

        BasicDBObject banDetails = new BasicDBObject();
        banDetails.append("reason", reason);
        banDetails.append("ban_timestamp", System.currentTimeMillis());
        banDetails.append("ban_until_timestamp", banUntilTimeStamp);
        banDetails.append("permanent", perma);
        document.append("ban_details", banDetails);

        return document;
    }
}
