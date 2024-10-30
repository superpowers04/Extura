package org.figuramc.figura.server;

import org.figuramc.figura.server.avatars.EHashPair;
import org.figuramc.figura.server.packets.CustomFSBPacket;
import org.figuramc.figura.server.packets.Packet;
import org.figuramc.figura.server.packets.s2c.S2CNotifyPacket;
import org.figuramc.figura.server.utils.Hash;
import org.figuramc.figura.server.utils.IFriendlyByteBuf;
import org.figuramc.figura.server.utils.InputStreamByteBuf;
import org.figuramc.figura.server.utils.OutputStreamByteBuf;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class FiguraUser {
    private final UUID player;
    private boolean online;
    private final PingCounter pingCounter = new PingCounter();
    private final BitSet prideBadges;
    private final HashMap<String, EHashPair> equippedAvatars;

    private final HashMap<String, EHashPair> ownedAvatars;

    public FiguraUser(UUID player, boolean online, boolean allowPings, boolean allowAvatars, BitSet prideBadges, HashMap<String, EHashPair> equippedAvatars, HashMap<String, EHashPair> ownedAvatars) {
        this.player = player;
        this.online = online;
        this.prideBadges = prideBadges;
        this.equippedAvatars = equippedAvatars;
        this.ownedAvatars = ownedAvatars;
    }

    public UUID uuid() {
        return player;
    }

    public boolean online() {
        return online;
    }

    public boolean offline() {
        return !online;
    }

    public PingCounter pingCounter() {
        return pingCounter;
    }

    public BitSet prideBadges() {
        return prideBadges;
    }

    public HashMap<String, EHashPair> equippedAvatars() {
        return equippedAvatars;
    }

    public HashMap<String, EHashPair> ownedAvatars() {
        return ownedAvatars;
    }

    public void sendPacket(Packet packet) {
        FiguraServer.getInstance().sendPacket(player, packet);
    }

    public void sendDeferredPacket(CompletableFuture<? extends Packet> packet) {
        FiguraServer.getInstance().sendDeferredPacket(player, packet);
    }

    public void save(Path file) {
        file.getParent().toFile().mkdirs();
        File playerFile = file.toFile();
        try {
            FileOutputStream fos = new FileOutputStream(playerFile);
            OutputStreamByteBuf buf = new OutputStreamByteBuf(fos);
            save(buf);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(IFriendlyByteBuf buf) {
        byte[] badges = prideBadges.toByteArray();
        buf.writeVarInt(badges.length);
        buf.writeBytes(badges);
        buf.writeVarInt(equippedAvatars.size());
        for (var equippedAvatar : equippedAvatars.entrySet()) {
            buf.writeByteArray(equippedAvatar.getKey().getBytes(UTF_8));
            buf.writeBytes(equippedAvatar.getValue().hash().get());
            buf.writeBytes(equippedAvatar.getValue().ehash().get());
        }
        buf.writeVarInt(ownedAvatars.size());
        for (var ownedAvatar : ownedAvatars.entrySet()) {
            buf.writeByteArray(ownedAvatar.getKey().getBytes(UTF_8));
            buf.writeBytes(ownedAvatar.getValue().hash().get());
            buf.writeBytes(ownedAvatar.getValue().ehash().get());
        }
    }

    public static FiguraUser load(UUID player, Path playerFile) {
        try (FileInputStream fis = new FileInputStream(playerFile.toFile())) {
            InputStreamByteBuf buf = new InputStreamByteBuf(fis);
            return load(player, buf);
        } catch (FileNotFoundException e) {
            return new FiguraUser(player, true, false, false, new BitSet(), new HashMap<>(), new HashMap<>());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FiguraUser load(UUID player, IFriendlyByteBuf buf) {
        int length = buf.readVarInt();
        byte[] arr = buf.readBytes(length);
        BitSet prideBadges = BitSet.valueOf(arr);
        int equippedAvatarsCount = buf.readVarInt();
        HashMap<String, EHashPair> equippedAvatars = new HashMap<>();
        for (int i = 0; i < equippedAvatarsCount; i++) {
            String id = new String(buf.readByteArray(256), UTF_8);
            Hash hash = buf.readHash();
            Hash ehash = buf.readHash();
            equippedAvatars.put(id, new EHashPair(hash, ehash));
        }
        HashMap<String, EHashPair> ownedAvatars = new HashMap<>();
        int ownedAvatarsCount = buf.readVarInt();
        for (int i = 0; i < ownedAvatarsCount; i++) {
            String id = new String(buf.readByteArray(256), UTF_8);
            Hash hash = buf.readHash();
            Hash ehash = buf.readHash();
            ownedAvatars.put(id, new EHashPair(hash, ehash));
        }
        return new FiguraUser(player, false, false, false, prideBadges, equippedAvatars, ownedAvatars);
    }

    public Hash findEHash(Hash hash) {
        for (EHashPair pair: equippedAvatars.values()) {
            if (pair.hash().equals(hash)) return pair.ehash();
        }
        for (EHashPair pair: ownedAvatars.values()) {
            if (pair.hash().equals(hash)) return pair.ehash();
        }
        return null;
    }

    public void update() {

    }

    public void setOnline() {
        online = true;
    }

    public void setOffline() {
        online = false;
    }

    public void removeOwnedAvatar(String avatarId) {
        if (ownedAvatars.containsKey(avatarId)) {
            EHashPair avatar = ownedAvatars.remove(avatarId);
            FiguraServer.getInstance().avatarManager().getAvatarMetadata(avatar.hash()).owners().remove(uuid());
        }
    }

    public void removeEquippedAvatar(String avatarId) {
        if (equippedAvatars.containsKey(avatarId)) {
            EHashPair avatar = equippedAvatars.remove(avatarId);
            FiguraServer.getInstance().avatarManager().getAvatarMetadata(avatar.hash()).equipped().remove(uuid());
        }
    }

    public void replaceOrAddOwnedAvatar(String avatarId, Hash hash, Hash ehash) {
        ownedAvatars.put(avatarId, new EHashPair(hash, ehash));
        FiguraServer.getInstance().avatarManager().getAvatarMetadata(hash).owners().put(uuid(), ehash);
    }

    public void replaceOrAddEquippedAvatar(String avatarId, Hash hash, Hash ehash) {
        equippedAvatars.put(avatarId, new EHashPair(hash, ehash));
        FiguraServer.getInstance().avatarManager().getAvatarMetadata(hash).equipped().put(uuid(), ehash);
        FiguraServer.getInstance().userManager().forEachUser(user -> {
            if (user != this)
                user.sendDeferredPacket(CompletableFuture.supplyAsync(() -> new S2CNotifyPacket(this.uuid())));
        });
    }

    public int getAvatarsCountWithId(String avatarId) {
        return ownedAvatars().size() + (ownedAvatars().containsKey(avatarId) ? 0 : 1);
    }

    public void sendFSBPacket(String id, byte[] data) {
        sendPacket(new CustomFSBPacket(id.hashCode(), data));
    }

    public static class PingCounter {
        private int bytesSent; // Amount of total bytes sent in last 20 ticks
        private int pingsSent; // Amount of pings sent in last 20 ticks

        public int bytesSent() {
            return bytesSent;
        }

        public int pingsSent() {
            return pingsSent;
        }

        public void addPing(int size) {
            pingsSent++;
            bytesSent += size;
        }

        public void reset() {
            bytesSent = 0;
            pingsSent = 0;
        }
    }
}
