package org.figuramc.figura.server.packets.s2c;

import org.figuramc.figura.server.packets.Packet;
import org.figuramc.figura.server.utils.IFriendlyByteBuf;
import org.figuramc.figura.server.utils.Identifier;

public class S2CBackendHandshakePacket implements Packet {
    public static final Identifier PACKET_ID = new Identifier("figura", "s2c/handshake");

    private final int pingsRateLimit;
    private final int pingsSizeLimit;

    private final int maxAvatarSize;
    private final int maxAvatarsCount;

    public S2CBackendHandshakePacket(int pingsRateLimit, int pingsSizeLimit, int maxAvatarSize, int maxAvatarsCount) {
        this.pingsRateLimit = pingsRateLimit;
        this.pingsSizeLimit = pingsSizeLimit;
        this.maxAvatarSize = maxAvatarSize;
        this.maxAvatarsCount = maxAvatarsCount;
    }

    public S2CBackendHandshakePacket(IFriendlyByteBuf source) {
        pingsRateLimit = source.readInt();
        pingsSizeLimit = source.readInt();

        maxAvatarSize = source.readInt();
        maxAvatarsCount = source.readInt();
    }

    public int pingsRateLimit() {
        return pingsRateLimit;
    }

    public int pingsSizeLimit() {
        return pingsSizeLimit;
    }

    public int maxAvatarSize() {
        return maxAvatarSize;
    }

    public int maxAvatarsCount() {
        return maxAvatarsCount;
    }

    @Override
    public void write(IFriendlyByteBuf byteBuf) {
        byteBuf.writeInt(pingsRateLimit);
        byteBuf.writeInt(pingsSizeLimit);

        byteBuf.writeInt(maxAvatarSize);
        byteBuf.writeInt(maxAvatarsCount);
    }

    @Override
    public Identifier getId() {
        return PACKET_ID;
    }
}
