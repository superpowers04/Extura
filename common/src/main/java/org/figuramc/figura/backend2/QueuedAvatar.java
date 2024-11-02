package org.figuramc.figura.backend2;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.UserData;
import org.figuramc.figura.avatar.local.CacheAvatarLoader;
import org.figuramc.figura.ducks.ServerDataAccessor;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.server.avatars.EHashPair;
import org.figuramc.figura.server.packets.AvatarDataPacket;
import org.figuramc.figura.server.packets.CloseIncomingStreamPacket;
import org.figuramc.figura.server.packets.Packet;
import org.figuramc.figura.server.packets.c2s.*;
import org.figuramc.figura.server.packets.s2c.S2CBackendHandshakePacket;
import org.figuramc.figura.server.packets.s2c.S2CPingPacket;
import org.figuramc.figura.server.packets.s2c.S2CUserdataPacket;
import org.figuramc.figura.server.utils.Hash;
import com.mojang.datafixers.util.Pair;
import org.figuramc.figura.server.utils.StatusCode;
import org.figuramc.figura.server.utils.Utils;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.utils.FiguraText;

import java.io.*;
import java.util.*;
public class QueuedAvatar{
	public UserData target;
	public UUID owner;
	public String id;
	public String hash;
	public boolean useFSB;
	public QueuedAvatar(UserData target, UUID owner, String id, String hash){
		this.target = target;
		this.owner = owner;
		this.id = id;
		this.hash = hash;
	}
}
