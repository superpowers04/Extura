package org.figuramc.figura.server.packets.s2c;

import org.figuramc.figura.server.avatars.EHashPair;
import org.figuramc.figura.server.packets.Packet;
import org.figuramc.figura.server.utils.Hash;
import org.figuramc.figura.server.utils.IFriendlyByteBuf;
import org.figuramc.figura.server.utils.Identifier;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class S2CUserdataPacket implements Packet {
    public static final Identifier PACKET_ID = new Identifier("figura", "s2c/userdata");

    private final UUID target;
    private final BitSet prideBadges;
    private final HashMap<String, EHashPair> avatars;

    public S2CUserdataPacket(UUID target, BitSet prideBadges, HashMap<String, EHashPair> avatars) {
        this.target = target;
        this.prideBadges = prideBadges;
        this.avatars = avatars;
    }

    public S2CUserdataPacket(IFriendlyByteBuf byteBuf) {
        this.target = byteBuf.readUUID();
        this.prideBadges = BitSet.valueOf(byteBuf.readByteArray(Integer.MAX_VALUE));
        avatars = new HashMap<>();
        int avatarsCount = byteBuf.readVarInt();
        for (int i = 0; i < avatarsCount; i++) {
            String avatarId = new String(byteBuf.readByteArray(Integer.MAX_VALUE), UTF_8);
            Hash hash = byteBuf.readHash();
            Hash ehash = byteBuf.readHash();
            avatars.put(avatarId, new EHashPair(hash, ehash));
        }
    }

    public UUID target() {
        return target;
    }

    public BitSet prideBadges() {
        return prideBadges;
    }

    public HashMap<String, EHashPair> avatars() {
        return avatars;
    }

    @Override
    public void write(IFriendlyByteBuf byteBuf) {
        byteBuf.writeUUID(target);
        byteBuf.writeByteArray(prideBadges.toByteArray());
        byteBuf.writeVarInt(avatars.size());
        for (Map.Entry<String, EHashPair> avatar: avatars.entrySet()) {
            byteBuf.writeByteArray(avatar.getKey().getBytes(UTF_8));
            byteBuf.writeBytes(avatar.getValue().hash().get());
            byteBuf.writeBytes(avatar.getValue().ehash().get());
        }
    }

    @Override
    public Identifier getId() {
        return PACKET_ID;
    }
}
