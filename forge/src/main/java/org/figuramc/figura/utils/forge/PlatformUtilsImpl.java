package org.figuramc.figura.utils.forge;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.utils.PlatformUtils;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlatformUtilsImpl {
    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.relative();
    }

    public static String getFiguraModVersionString() {
        return ModList.get().getModContainerById(FiguraMod.MOD_ID).get().getModInfo().getVersion().toString();
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.relative();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static String getModVersion(String modId) {
        return ModList.get().getModContainerById(modId).get().getModInfo().getVersion().getQualifier();
    }

    public static String getModName(String modId) {
        return ModList.get().getModContainerById(modId).get().getModInfo().getDisplayName();
    }

    public static Map<String, List<ItemStackAPI>> getCurios(LivingEntity entity) {
        Map<String, List<ItemStackAPI>> curio = new HashMap<>();
        CuriosApi.getCuriosInventory(entity).ifPresent(curiosInventory -> {
            Map<String, ICurioStacksHandler> curios = curiosInventory.getCurios();
            curios.forEach((identifier, slotInventory) -> {
                if (slotInventory != null) {
                    List<ItemStackAPI> items = new ArrayList<>();
                    IDynamicStackHandler stacks = slotInventory.getStacks();
                    for (int i = 0; i < slotInventory.getSlots(); i++) {
                        items.add(ItemStackAPI.verify(stacks.getStackInSlot(i)));
                    }
                    curio.put(identifier, items);
                }
            });
        });
        return curio;
    }

    public static PlatformUtils.ModLoader getModLoader() {
        return PlatformUtils.ModLoader.FORGE;
    }

    public static InputStream loadFileFromRoot(String path) throws FileNotFoundException {
        File file = ModList.get().getModFileById(FiguraMod.MOD_ID).getFile().findResource(path).toFile();
        return new FileInputStream(file);
    }
}
