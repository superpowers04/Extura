package org.figuramc.figura.server.packets.handlers.c2s;

import org.figuramc.figura.server.FiguraServer;
import org.figuramc.figura.server.FiguraUser;
import org.figuramc.figura.server.events.Events;
import org.figuramc.figura.server.events.avatars.AvatarFetchEvent;
import org.figuramc.figura.server.packets.c2s.C2SFetchAvatarPacket;
import org.figuramc.figura.server.utils.Hash;
import org.figuramc.figura.server.utils.IFriendlyByteBuf;
import org.figuramc.figura.server.utils.Utils;

public class C2SFetchAvatarPacketHandler extends AuthorizedC2SPacketHandler<C2SFetchAvatarPacket> {
    public C2SFetchAvatarPacketHandler(FiguraServer parent) {
        super(parent);
    }

    @Override
    protected void handle(FiguraUser sender, C2SFetchAvatarPacket packet) {
        var hash = packet.hash();
        if (!Events.call(new AvatarFetchEvent(hash)).isCancelled()) {
            parent.avatarManager().sendAvatar(hash, sender.uuid(), packet.streamId());
        }
    }

    @Override
    public C2SFetchAvatarPacket serialize(IFriendlyByteBuf byteBuf) {
        return new C2SFetchAvatarPacket(byteBuf);
    }
}
