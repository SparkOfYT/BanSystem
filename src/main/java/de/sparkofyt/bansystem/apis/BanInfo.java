package de.sparkofyt.bansystem.apis;

import java.util.UUID;

public class BanInfo {

    /* Variables */
    private final String banID, reason;
    private final UUID bannedPlayerUUID, bannerPlayerUUID;
    private final long timeStampOfBan, timeStampEndOfBan;
    private final boolean permanent;

    /* Constructor */
    public BanInfo(String banID, UUID bannedPlayerUUID, UUID bannerPlayerUUID, String reason, long timeStampOfBan, long timeStampEndOfBan, boolean permanent) {
        this.banID = banID;
        this.bannedPlayerUUID = bannedPlayerUUID;
        this.bannerPlayerUUID = bannerPlayerUUID;
        this.reason = reason;
        this.timeStampOfBan = timeStampOfBan;
        this.timeStampEndOfBan = timeStampEndOfBan;
        this.permanent = permanent;
    }

    /* Getters & Setters */
    public String getBanID() {
        return banID;
    }

    public String getReason() {
        return reason;
    }

    public UUID getBannedPlayerUUID() {
        return bannedPlayerUUID;
    }

    public UUID getBannerPlayerUUID() {
        return bannerPlayerUUID;
    }

    public long getTimeStampOfBan() {
        return timeStampOfBan;
    }

    public long getTimeStampEndOfBan() {
        return timeStampEndOfBan;
    }

    public boolean isPermanent() {
        return permanent;
    }
}
