package org.figuramc.figura.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.level.Level;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPacketListener.class, priority = 999)
public class ClientPacketListenerMixin {

    @Inject(at = @At("HEAD"), method = "sendUnsignedCommand", cancellable = true)
    private void sendUnsignedCommand(String command, CallbackInfoReturnable<Boolean> cir) {
        if (command.startsWith(FiguraMod.MOD_ID))
            cir.setReturnValue(false);
    }

    @Inject(at = @At("HEAD"), method = "handleDamageEvent")
    private void handleDamageEvent(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) return;
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        Level level = Minecraft.getInstance().player.level();
        if (avatar == null) return;
        avatar.damageEvent(
        EntityAPI.wrap(level.getEntity(packet.entityId())),
        level.registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolder(packet.sourceTypeId()).get().unwrapKey().get().location().toString(),
        EntityAPI.wrap(level.getEntity(packet.sourceCauseId())),
        EntityAPI.wrap(level.getEntity(packet.sourceDirectId()))
        );
    }
}
