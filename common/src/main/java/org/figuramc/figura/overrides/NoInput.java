package org.figuramc.figura.overrides;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.Input;
import net.minecraft.world.phys.Vec2;

@Environment(EnvType.CLIENT)
public class NoInput extends Input {

    @Override
    public Vec2 getMoveVector() {
        return new Vec2(0, 0);
    }

}
