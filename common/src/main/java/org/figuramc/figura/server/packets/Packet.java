package org.figuramc.figura.server.packets;

import org.figuramc.figura.server.packets.c2s.*;
import org.figuramc.figura.server.packets.s2c.*;
import org.figuramc.figura.server.utils.IFriendlyByteBuf;
import org.figuramc.figura.server.utils.Identifier;

import java.util.HashMap;

public interface Packet {
    void write(IFriendlyByteBuf buf);
    Identifier getId();


    interface Deserializer<P extends Packet> {
        P read(IFriendlyByteBuf buf);
    }
}
