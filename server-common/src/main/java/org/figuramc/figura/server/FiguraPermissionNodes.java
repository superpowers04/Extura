package org.figuramc.figura.server;

public enum FiguraPermissionNodes {
    FIGURA_AVATARS_IMMORTALIZE("figura.avatars.immortalize"),

    FIGURA_AVATARS_SET("figura.avatars.set"),

    FIGURA_AVATARS_CLEAR("figura.avatars.clear"),

    FIGURA_PINGS_RATELIMIT("figura.pings.ratelimit"),

    FIGURA_PINGS_SIZELIMIT("figura.pings.sizelimit"),

    FIGURA_AVATARS_SIZELIMIT("figura.avatars.sizelimit"),

    FIGURA_AVATARS_COUNTLIMIT("figura.avatars.countlimit");


    private final String node;

    FiguraPermissionNodes(String node) {
        this.node = node;
    }

    public String toString() {
        return node;
    }

    public static FiguraPermissionNodes fromString(String node) {
        for (FiguraPermissionNodes permissionNode : values()) {
            if (permissionNode.node.equalsIgnoreCase(node)) {
                return permissionNode;
            }
        }
        throw new IllegalArgumentException("No permission node found for: " + node);
    }
}
