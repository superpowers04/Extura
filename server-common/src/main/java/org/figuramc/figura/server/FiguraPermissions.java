package org.figuramc.figura.server;

import org.figuramc.figura.server.utils.Pair;
import static org.figuramc.figura.server.FiguraPermissionNodes.*;

import java.util.List;

public class FiguraPermissions {
    public static final List<Pair<FiguraPermissionNodes, Boolean>> PERMISSIONS_LIST = List.of(
            Pair.of(FIGURA_AVATARS_IMMORTALIZE, false),
            Pair.of(FIGURA_AVATARS_SET, false)
    );
    public static final List<FiguraPermissionNodes> OPTIONS_LIST = List.of(
            FIGURA_PINGS_RATELIMIT,
            FIGURA_PINGS_SIZELIMIT,
            FIGURA_AVATARS_SIZELIMIT,
            FIGURA_AVATARS_COUNTLIMIT
    );
}