package org.figuramc.figura.server.packets;

import org.figuramc.figura.server.packets.c2s.*;
import org.figuramc.figura.server.packets.s2c.*;
import org.figuramc.figura.server.utils.IFriendlyByteBuf;
import org.figuramc.figura.server.utils.Identifier;

import java.util.HashMap;

public interface Packet {
    void write(IFriendlyByteBuf buf);
    Identifier getId();


    interface Deserializer {
        Packet read(IFriendlyByteBuf buf);
    }

    HashMap<Identifier, Deserializer> PACKETS = new HashMap<>() {{
        put(C2SBackendHandshakePacket.PACKET_ID, (buf) -> new C2SBackendHandshakePacket());
        put(C2SDeleteAvatarPacket.PACKET_ID, C2SDeleteAvatarPacket::new);
        put(C2SEquipAvatarsPacket.PACKET_ID, C2SEquipAvatarsPacket::new);
        put(C2SFetchAvatarPacket.PACKET_ID, C2SFetchAvatarPacket::new);
        put(C2SFetchOwnedAvatarsPacket.PACKET_ID, (buf) -> new C2SFetchOwnedAvatarsPacket());
        put(C2SFetchUserdataPacket.PACKET_ID, C2SFetchUserdataPacket::new);
        put(C2SPingPacket.PACKET_ID, C2SPingPacket::new);
        put(C2SUploadAvatarPacket.PACKET_ID, C2SUploadAvatarPacket::new);

        put(S2CRefusedPacket.PACKET_ID, (buf) -> new S2CRefusedPacket());
        put(S2CBackendHandshakePacket.PACKET_ID, S2CBackendHandshakePacket::new);
        put(S2CInitializeAvatarStreamPacket.PACKET_ID, S2CInitializeAvatarStreamPacket::new);
        put(S2COwnedAvatarsPacket.PACKET_ID, S2COwnedAvatarsPacket::new);
        put(S2CPingErrorPacket.PACKET_ID, S2CPingErrorPacket::new);
        put(S2CPingPacket.PACKET_ID, S2CPingPacket::new);
        put(S2CUserdataPacket.PACKET_ID, S2CUserdataPacket::new);
        put(S2CNotifyPacket.PACKET_ID, S2CNotifyPacket::new);

        put(AllowIncomingStreamPacket.PACKET_ID, AllowIncomingStreamPacket::new);
        put(AvatarDataPacket.PACKET_ID, AvatarDataPacket::new);
        put(CloseIncomingStreamPacket.PACKET_ID, CloseIncomingStreamPacket::new);
        put(CloseOutcomingStreamPacket.PACKET_ID, CloseOutcomingStreamPacket::new);
        put(CustomFSBPacket.PACKET_ID, CustomFSBPacket::new);
    }};
}
