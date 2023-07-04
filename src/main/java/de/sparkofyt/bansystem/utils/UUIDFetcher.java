package de.sparkofyt.bansystem.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class UUIDFetcher {

    /* Methods */
    public static UUID getUUID(String name) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
            String uuid = (((JsonObject)new JsonParser().parse(in)).get("id")).toString().replaceAll("\"", "");
            uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            in.close();

            return UUID.fromString(uuid);
        } catch (Exception e) {
            System.out.println("Unable to get UUID of: " + name + "!");
            return null;
        }
    }

    public static String getName(UUID uuid) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/"+uuid.toString().replace("-", "")).openStream()));
            String name = (((JsonObject) new JsonParser().parse(in)).get("name").toString());
            in.close();

            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
