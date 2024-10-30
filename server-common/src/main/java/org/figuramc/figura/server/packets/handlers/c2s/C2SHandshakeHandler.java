package org.figuramc.figura.server.packets.handlers.c2s;

import org.figuramc.figura.server.FiguraServer;
import org.figuramc.figura.server.FiguraUserManager;
import org.figuramc.figura.server.packets.c2s.C2SBackendHandshakePacket;
import org.figuramc.figura.server.packets.s2c.S2CConnectedPacket;
import org.figuramc.figura.server.utils.IFriendlyByteBuf;

import java.util.UUID;

public class C2SHandshakeHandler implements C2SPacketHandler<C2SBackendHandshakePacket> {
    private final FiguraServer parent;

    public C2SHandshakeHandler(FiguraServer parent) {
        this.parent = parent;
    }

    @Override
    public C2SBackendHandshakePacket serialize(IFriendlyByteBuf byteBuf) {
        return new C2SBackendHandshakePacket();
    }

    @Override
    public void handle(UUID sender, C2SBackendHandshakePacket packet) {
        FiguraUserManager manager = parent.userManager();
        if (!manager.isExpected(sender)) return;
        parent.logDebug("Setting up player %s".formatted(sender));
        manager.setupOnlinePlayer(sender);
        parent.logDebug("Player %s is setup".formatted(sender));
    }
}
