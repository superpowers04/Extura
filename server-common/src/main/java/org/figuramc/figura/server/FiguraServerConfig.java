package org.figuramc.figura.server;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public final class FiguraServerConfig {
    @SerializedName("pingsRateLimit")
    private int pingsRateLimit = 32;
    @SerializedName("pingsSizeLimit")
    private int pingsSizeLimit = 1024;
    @SerializedName("avatarSizeLimit")
    private int avatarSizeLimit = 102400;
    @SerializedName("avatarCountLimit")
    private int avatarsCountLimit = 1;

    public int getDefaultPingsRateLimit() {
        return pingsRateLimit;
    }

    public int getDefaultPingsSizeLimit() {
        return pingsSizeLimit;
    }

    public int getDefaultAvatarSizeLimit() {
        return avatarSizeLimit;
    }

    public int getDefaultAvatarsCountLimit() {
        return avatarsCountLimit;
    }


    public int pingsRateLimit(FiguraServer server, UUID player) {
        return Integer.parseInt(server.getOption(player, FiguraPermissionNodes.FIGURA_PINGS_RATELIMIT).orElse(pingsRateLimit + ""));
    }

    public int pingsSizeLimit(FiguraServer server, UUID player) {
        return Integer.parseInt(server.getOption(player, FiguraPermissionNodes.FIGURA_PINGS_SIZELIMIT).orElse(pingsSizeLimit + ""));
    }

    public int avatarSizeLimit(FiguraServer server, UUID player) {
        return Integer.parseInt(server.getOption(player, FiguraPermissionNodes.FIGURA_AVATARS_SIZELIMIT).orElse(avatarSizeLimit + ""));
    }

    public int avatarsCountLimit(FiguraServer server, UUID player) {
        return Integer.parseInt(server.getOption(player, FiguraPermissionNodes.FIGURA_AVATARS_COUNTLIMIT).orElse(avatarsCountLimit + ""));
    }
}
