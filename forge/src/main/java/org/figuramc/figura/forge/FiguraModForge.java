package org.figuramc.figura.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("figura")
public class FiguraModForge {
    // dummy empty mod class, we are client only
    public FiguraModForge() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> FiguraModClientForge::initClient);
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> FiguraModServerForge::initServer);  
    }
}
