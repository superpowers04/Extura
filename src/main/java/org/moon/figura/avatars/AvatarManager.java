package org.moon.figura.avatars;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.providers.LocalAvatarLoader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

/**
 * Manages all the avatars that are currently loaded in memory, and also
 * handles getting the avatars of entities. If an entity does not have a loaded avatar,
 * the AvatarManager will fetch the avatar and cache it.
 */
public class AvatarManager {

    private static final HashMap<UUID, Avatar> LOADED_AVATARS = new HashMap<>();
    public static boolean localUploaded = true; //init as true :3

    //player will also attempt to load from network, if possible
    public static Avatar getAvatarForPlayer(UUID player) {
        if (!LOADED_AVATARS.containsKey(player))
            fetchBackend(player);

        return LOADED_AVATARS.get(player);
    }

    //tries to get data from an entity
    public static Avatar getAvatar(Entity entity) {
        UUID uuid = entity.getUUID();

        //load from player (fetch backend) if is a player
        if (entity instanceof Player)
            return getAvatarForPlayer(uuid);

        //otherwise, just normally load it
        return LOADED_AVATARS.get(uuid);
    }

    //removes an loaded avatar
    public static void clearAvatar(UUID id) {
        LOADED_AVATARS.remove(id);
    }

    //load the local player avatar
    public static void loadLocalAvatar(Path path) {
        //clear
        UUID id = FiguraMod.getLocalPlayerUUID();
        clearAvatar(id);

        //mark as not uploaded
        localUploaded = false;

        //load
        try {
            CompoundTag nbt = LocalAvatarLoader.loadAvatar(path);
            if (nbt != null) {
                LOADED_AVATARS.put(id, new Avatar(nbt));
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load avatar from " + path, e);
        }
    }

    //get avatar from the backend
    //mark as uploaded if local
    private static void fetchBackend(UUID id) {
        //TODO
    }
}
