package org.figuramc.figura.utils.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.utils.PlatformUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class PlatformUtilsImpl {
    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static String getFiguraModVersionString() {
        return FabricLoader.getInstance().getModContainer(FiguraMod.MOD_ID).get().getMetadata().getVersion().getFriendlyString();
    }

    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static String getModName(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).get().getMetadata().getName();
    }
    public static String getModVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).get().getMetadata().getVersion().getFriendlyString();
    }

    public static PlatformUtils.ModLoader getModLoader() {
        return PlatformUtils.ModLoader.FABRIC;
    }

    public static Map<String, List<ItemStackAPI>> getCurios(LivingEntity entity) {
        return null;
    }

    public static InputStream loadFileFromRoot(String path) throws FileNotFoundException {
        File file = FabricLoader.getInstance().getModContainer(FiguraMod.MOD_ID).get().findPath(path).get().toFile();
        return new FileInputStream(file);
    }
}
