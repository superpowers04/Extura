package org.figuramc.figura.lua.api;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import dev.tr7zw.firstperson.api.FirstPersonAPI;
import net.irisshaders.iris.Iris;
import net.minecraft.client.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.mixin.input.KeyMappingAccessor;
import org.figuramc.figura.permissions.Permissions;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.luaj.vm2.LuaError;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.GuiMessageAccessor;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.mixin.LivingEntityAccessor;
import org.figuramc.figura.mixin.gui.ChatComponentAccessor;
import org.figuramc.figura.mixin.gui.ChatScreenAccessor;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.overrides.NoInput;
import org.figuramc.figura.overrides.ExturaInput;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.luaj.vm2.LuaError;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.backend2.Destination;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.backend2.FSB;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.nio.file.Path;


@LuaWhitelist
@LuaTypeDoc(
		name = "HostAPI",
		value = "host"
)
public class HostAPI {

	private final Avatar owner;
	private final boolean isHost;
	private final Minecraft minecraft;
	private static Input defaultInput;

	@LuaWhitelist
	@LuaFieldDoc("host.unlock_cursor")
	public boolean unlockCursor = false;
	public Integer chatColor;

	public HostAPI(Avatar owner) {
		defaultInput = null;
		this.minecraft = Minecraft.getInstance();
		this.isHost = (this.owner = owner).isHost;
	}
    @LuaMethodDoc("host.get_cursor_item")
    @LuaWhitelist
    public ItemStackAPI getCursorItem() {
        if (!isHost() || this.minecraft.player == null)
            return ItemStackAPI.verify(ItemStack.EMPTY);
        return ItemStackAPI.verify(this.minecraft.player.containerMenu.getCarried());
    }

	@LuaWhitelist
	@LuaMethodDoc("host.is_host")
	public boolean isHost() {
		return this.isHost;
	}

	@LuaWhitelist
	@LuaMethodDoc(value = "host.is_cursor_unlocked")
	public boolean isCursorUnlocked() {
		return unlockCursor;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = @LuaMethodOverload(
					argumentTypes = Boolean.class,
					argumentNames = "boolean"
			),
			value = "host.set_unlock_cursor")
	public HostAPI setUnlockCursor(boolean bool) {
		if(!this.isHost) return this;
		unlockCursor = bool;
		return this;
	}
	@LuaWhitelist
	@LuaMethodDoc("host.allow_extura_cheats")
	public Boolean allowExturaCheats() {
		if(!this.isHost) return false;
		LocalPlayer player = this.minecraft.player;
		return player != null && ((player.hasPermissions(2)  || 
				this.minecraft.isLocalServer() ||
				(player.getScoreboard().hasObjective("extura_can_cheat"))
			));
	}
	public Boolean canExturaCheat() {
		if(!this.isHost) return false;
		LocalPlayer player = this.minecraft.player;
		if(player == null) return false;
		if(player.hasPermissions(2)  || 
				this.minecraft.isLocalServer() ||
				(player.getScoreboard().hasObjective("extura_can_cheat"))
			) return true;
		if(!owner.noPermissions.contains(Permissions.EXTURA_CHEATING)){
			owner.noPermissions.add(Permissions.EXTURA_CHEATING);
		}
		return false;
	}
	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = FiguraVec3.class,
							argumentNames = "timesData"
					),
					@LuaMethodOverload(
							argumentTypes = {Integer.class, Integer.class, Integer.class},
							argumentNames = {"fadeInTime", "stayTime", "fadeOutTime"}
					)
			},
			aliases = "titleTimes",
			value = "host.set_title_times"
	)
	public HostAPI setTitleTimes(Object x, Double y, Double z) {
		if (!this.isHost) return this;
		FiguraVec3 times = LuaUtils.parseVec3("setTitleTimes", x, y, z);
		this.minecraft.gui.setTimes((int) times.x, (int) times.y, (int) times.z);
		return this;
	}

	@LuaWhitelist
	public HostAPI titleTimes(Object x, Double y, Double z) {
		return setTitleTimes(x, y, z);
	}


	@LuaWhitelist
	@LuaMethodDoc("host.clear_title")
	public HostAPI clearTitle() {
		if (isHost())
			this.minecraft.gui.clear();
		return this;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = @LuaMethodOverload(
					argumentTypes = String.class,
					argumentNames = "text"
			),
			aliases = "title",
			value = "host.set_title"
	)
	public HostAPI setTitle(@LuaNotNil String text) {
		if (isHost())
			this.minecraft.gui.setTitle(TextUtils.tryParseJson(text));
		return this;
	}

	@LuaWhitelist
	public HostAPI title(@LuaNotNil String text) {
		return setTitle(text);
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = @LuaMethodOverload(
					argumentTypes = String.class,
					argumentNames = "text"
			),
			aliases = "subtitle",
			value = "host.set_subtitle"
	)
	public HostAPI setSubtitle(@LuaNotNil String text) {
		if (isHost())
			this.minecraft.gui.setSubtitle(TextUtils.tryParseJson(text));
		return this;
	}

	@LuaWhitelist
	public HostAPI subtitle(@LuaNotNil String text) {
		return setSubtitle(text);
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = String.class,
							argumentNames = "text"
					),
					@LuaMethodOverload(
							argumentTypes = {String.class, boolean.class},
							argumentNames = {"text", "animated"}
					)
			},
			aliases = "actionbar",
			value = "host.set_actionbar"
	)
	public HostAPI setActionbar(@LuaNotNil String text, boolean animated) {
		if (isHost())
			this.minecraft.gui.setOverlayMessage(TextUtils.tryParseJson(text), animated);
		return this;
	}

	@LuaWhitelist
	public HostAPI actionbar(@LuaNotNil String text, boolean animated) {
		return setActionbar(text, animated);
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = @LuaMethodOverload(
					argumentTypes = String.class,
					argumentNames = "message"
			),
			value = "host.send_chat_message"
	)
	public HostAPI sendChatMessage(@LuaNotNil String message) {
		if (!this.isHost || !Configs.CHAT_MESSAGES.value) return this;
		ClientPacketListener connection = this.minecraft.getConnection();
		if (connection != null) connection.sendChat(message);
		return this;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = @LuaMethodOverload(
					argumentTypes = String.class,
					argumentNames = "command"
			),
			value = "host.send_chat_command"
	)
	public HostAPI sendChatCommand(@LuaNotNil String command) {
		if (!this.isHost || !Configs.CHAT_MESSAGES.value) return this;
		ClientPacketListener connection = this.minecraft.getConnection();
		if (connection != null) connection.sendCommand(command.startsWith("/") ? command.substring(1) : command);
		return this;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = @LuaMethodOverload(
					argumentTypes = String.class,
					argumentNames = "message"
			),
			value = "host.append_chat_history"
	)
	public HostAPI appendChatHistory(@LuaNotNil String message) {
		if (isHost())
			this.minecraft.gui.getChat().addRecentChat(message);
		return this;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = @LuaMethodOverload(
					argumentTypes = Integer.class,
					argumentNames = "index"
			),
			value = "host.get_chat_message"
	)
	public Map<String, Object> getChatMessage(int index) {
		if (!this.isHost)
			return null;

		index--;
		List<GuiMessage> messages = ((ChatComponentAccessor) this.minecraft.gui.getChat()).getAllMessages();
		if (index < 0 || index >= messages.size())
			return null;

		GuiMessage message = messages.get(index);
		Map<String, Object> map = new HashMap<>();

		map.put("addedTime", message.addedTime());
		map.put("message", message.content().getString());
		map.put("json", message.content());
		map.put("backgroundColor", ((GuiMessageAccessor) (Object) message).figura$getColor());

		return map;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = Integer.class,
							argumentNames = "index"
					),
					@LuaMethodOverload(
							argumentTypes = {Integer.class, String.class},
							argumentNames = {"index", "newMessage"}
					),
					@LuaMethodOverload(
							argumentTypes = {Integer.class, String.class, FiguraVec3.class},
							argumentNames = {"index", "newMessage", "backgroundColor"}
					)
			},
			value = "host.set_chat_message")
	public HostAPI setChatMessage(int index, String newMessage, FiguraVec3 backgroundColor) {
		if (!this.isHost) return this;

		index--;
		List<GuiMessage> messages = ((ChatComponentAccessor) this.minecraft.gui.getChat()).getAllMessages();
		if (index < 0 || index >= messages.size())
			return this;

		if (newMessage == null)
			messages.remove(index);
		else {
			GuiMessage old = messages.get(index);
			GuiMessage neww = new GuiMessage(this.minecraft.gui.getGuiTicks(), TextUtils.tryParseJson(newMessage), null, GuiMessageTag.chatModified(old.content().getString()));
			messages.set(index, neww);
			((GuiMessageAccessor) (Object) neww).figura$setColor(backgroundColor != null ? ColorUtils.rgbToInt(backgroundColor) : ((GuiMessageAccessor) (Object) old).figura$getColor());
		}

		this.minecraft.gui.getChat().rescaleChat();
		return this;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload,
					@LuaMethodOverload(
							argumentTypes = Boolean.class,
							argumentNames = "sprinting"
					)
			},
			value = "host.run_method"
	)
	public Object runMethod(String name, Object... args) {
		if (!this.isHost || !this.canExturaCheat()) return this;
		Method med;
		try {
			Class c = this.minecraft.player.getClass();
			if(args == null || args.length == 0){
				med = c.getMethod(name);

			}else{

				Class<?>[] argumentTypes = new Class[args.length];
				var len = args.length;
				for (int i = 0; i < len; i++) {
					argumentTypes[i] = args[i].getClass();
				}
				med = c.getMethod(name, argumentTypes);
			}
		} catch (NoSuchMethodException e) {
			throw new LuaError("No such method method "+name);
		}
		try{
			return med.invoke(name,args);
		}catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new LuaError("Unable to access method "+name);
		}
	}
	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload,
					@LuaMethodOverload(
							argumentTypes = Boolean.class,
							argumentNames = "sprinting"
					)
			},
			value = "host.setSprinting"
	)
	public HostAPI setSprinting(boolean bool) {
		if (!this.isHost) return this;
		this.minecraft.player.setSprinting(bool == true);
		return this;
	}
	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload,
					@LuaMethodOverload(
							argumentTypes = Boolean.class,
							argumentNames = "offhand"
					)
			},
			value = "host.swing_arm"
	)
	public HostAPI swingArm(boolean offhand) {
		if (this.isHost && this.minecraft.player != null)
			this.minecraft.player.swing(offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
		return this;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = String.class,
							argumentNames = "slot"
					),
					@LuaMethodOverload(
							argumentTypes = Integer.class,
							argumentNames = "slot"
					)
			},
			value = "host.get_slot"
	)
	public ItemStackAPI getSlot(@LuaNotNil Object slot) {
		if (!this.isHost) return null;
		Entity e = this.owner.luaRuntime.getUser();
		if (e == null || !e.isAlive())
			return ItemStackAPI.verify(ItemStack.EMPTY);
		SlotAccess slotAccess = e.getSlot(LuaUtils.parseSlot(slot, null));
		return ItemStackAPI.verify(slotAccess.get());
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(argumentTypes = String.class, argumentNames = "slot"),
					@LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "slot"),
					@LuaMethodOverload(argumentTypes = {String.class, String.class}, argumentNames = {"slot", "item"}),
					@LuaMethodOverload(argumentTypes = {Integer.class, ItemStackAPI.class}, argumentNames = {"slot", "item"})
			},
			value = "host.set_slot"
	)
	public HostAPI setSlot(@LuaNotNil Object slot, Object item) {
		if (!this.isHost || (slot == null && item == null) || this.minecraft.gameMode == null || this.minecraft.player == null || !this.minecraft.gameMode.getPlayerMode().isCreative())
			return this;

		Inventory inventory = this.minecraft.player.getInventory();

		int index = LuaUtils.parseSlot(slot, inventory);
		ItemStack stack = LuaUtils.parseItemStack("setSlot", item);

		inventory.setItem(index, stack);
		this.minecraft.gameMode.handleCreativeModeItemAdd(stack, index + 36);

		return this;
	}

	@LuaWhitelist
	public HostAPI setBadge(int index, boolean value, boolean pride) {


		Pair<BitSet, BitSet> badges = AvatarManager.getBadges(owner.owner);
		if (badges == null)
			return this;

		BitSet set = pride ? badges.getFirst() : badges.getSecond();
		set.set(index, value);
		return this;
	}

	@LuaWhitelist
	public HostAPI badge(int index, boolean value, boolean pride) {
		return setBadge(index, value, pride);
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_chat_color")
	public Integer getChatColor() {
		return this.isHost ? this.chatColor : null;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = FiguraVec3.class,
							argumentNames = "color"
					),
					@LuaMethodOverload(
							argumentTypes = {Double.class, Double.class, Double.class},
							argumentNames = {"r", "g", "b"}
					)
			},
			aliases = "chatColor",
			value = "host.set_chat_color"
	)
	public HostAPI setChatColor(Object x, Double y, Double z) {
		if (this.isHost) this.chatColor = x == null ? null : ColorUtils.rgbToInt(LuaUtils.parseVec3("setChatColor", x, y, z));
		return this;
	}

	@LuaWhitelist
	public HostAPI chatColor(Object x, Double y, Double z) {
		return setChatColor(x, y, z);
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_chat_text")
	public String getChatText() {
		if (this.isHost && this.minecraft.screen instanceof ChatScreen chat)
			return ((ChatScreenAccessor) chat).getInput().getValue();

		return null;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = @LuaMethodOverload(
					argumentTypes = String.class,
					argumentNames = "text"
			),
			aliases = "chatText",
			value = "host.set_chat_text"
	)
	public HostAPI setChatText(@LuaNotNil String text) {
		if (this.isHost && Configs.CHAT_MESSAGES.value && this.minecraft.screen instanceof ChatScreen chat)
			((ChatScreenAccessor) chat).getInput().setValue(text);
		return this;
	}

	@LuaWhitelist
	public HostAPI chatText(@LuaNotNil String text) {
		return setChatText(text);
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_screen")
	public String getScreen() {
		if (!this.isHost || this.minecraft.screen == null) return null;
		return this.minecraft.screen.getClass().getName();
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_screen_slot_count")
	public Integer getScreenSlotCount() {
		if (this.isHost && this.minecraft.screen instanceof AbstractContainerScreen<?> screen)
			return screen.getMenu().slots.size();
		return null;
	}

	@LuaWhitelist
	@LuaMethodDoc(overloads = {
			@LuaMethodOverload(argumentTypes = String.class, argumentNames = "slot"),
			@LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "slot")
	}, value = "host.get_screen_slot")
	public ItemStackAPI getScreenSlot(@LuaNotNil Object slot) {
		if (!this.isHost || !(this.minecraft.screen instanceof AbstractContainerScreen<?> screen))
			return null;

		NonNullList<Slot> slots = screen.getMenu().slots;
		int index = LuaUtils.parseSlot(slot, null);
		if (index < 0 || index >= slots.size())
			return null;
		return ItemStackAPI.verify(slots.get(index).getItem());
	}

	@LuaWhitelist
	@LuaMethodDoc("host.is_chat_open")
	public boolean isChatOpen() {
		return this.isHost && this.minecraft.screen instanceof ChatScreen;
	}

	@LuaWhitelist
	@LuaMethodDoc("host.is_container_open")
	public boolean isContainerOpen() {
		return this.isHost && this.minecraft.screen instanceof AbstractContainerScreen;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = @LuaMethodOverload(
					argumentTypes = String.class,
					argumentNames = "name"
			),
			value = "host.screenshot")
	public FiguraTexture screenshot(@LuaNotNil String name) {
		if (!this.isHost) return null;

		return owner.luaRuntime.texture.register(name, Screenshot.takeScreenshot(this.minecraft.getMainRenderTarget()), true);
	}

	@LuaWhitelist
	@LuaMethodDoc("host.is_avatar_uploaded")
	public boolean isAvatarUploaded() {
		return this.isHost && AvatarManager.localUploaded;
	}
	@LuaWhitelist
	@LuaMethodDoc("host.upload_avatar")
	public boolean uploadAvatar() {
		if(!this.isHost) return false;
		Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
		if(avatar == null) throw new LuaError("Cannot upload a null avatar!");
		try {
			LocalAvatarLoader.loadAvatar(null, null);
		} catch (Exception ignored) {}
		NetworkStuff.uploadAvatar(avatar);
		AvatarList.selectedEntry = null;
		return true;
	}
	@LuaWhitelist
	@LuaMethodDoc("host.upload_avatar_to")
	public boolean uploadAvatarTo(boolean backend,boolean fsb) {
		if(!this.isHost) return false;
		Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
		if(avatar == null) throw new LuaError("Cannot upload a null avatar!");
		try {
			LocalAvatarLoader.loadAvatar(null, null);
		} catch (Exception ignored) {}
		NetworkStuff.uploadAvatar(avatar,(!backend && !fsb) ? Destination.FSB_OR_BACKEND : Destination.fromBool(backend,fsb));
		AvatarList.selectedEntry = null;
		return true;
	}
	@LuaWhitelist
	@LuaMethodDoc(
		overloads = {
			@LuaMethodOverload(argumentTypes = String.class, argumentNames = "Avatar Owner"),
			@LuaMethodOverload(argumentTypes = EntityAPI.class, argumentNames = "Avatar Owner")
		},
		value = "host.reload_avatar")
	public void reloadAvatar(Object playerUUID) {
		if(!this.isHost) return;
		final UUID uuid;
		if(playerUUID instanceof EntityAPI){
			uuid = ((EntityAPI) playerUUID).getEntity().getUUID();
		}else if(playerUUID instanceof String){
			uuid = UUID.fromString((String) playerUUID);
		}else if(playerUUID != null){
			throw new LuaError("Expected String, EntityAPI or Nil");
		}else{
			uuid = FiguraMod.getLocalPlayerUUID();
		}
		// (UUID != null && !UUID.isEmpty() ? UUID.fromString(UUID) : FiguraMod.getLocalPlayerUUID() )
		AvatarManager.reloadAvatar(uuid);
	}
   @LuaWhitelist
    @LuaMethodDoc(
            value = "host.update_statistics",
            aliases = "updateStats"
    )
    public void updateStatistics() {
        this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS)); // in theory, this cant nullptr; if it does, ill fix later
    }
    @LuaWhitelist
    public void updateStats() {updateStatistics();}

    @LuaWhitelist
    @LuaMethodDoc(
            value = "host.get_general_statistics",
            aliases = "getGeneralStats"
    )
    public Map<String, Double> getGeneralStatistics() {
        Map<String, Double> map = new HashMap<>();
        LocalPlayer player = this.minecraft.player;
        if (player == null || !isHost()) return map;
        for (Stat<?> stat : Stats.CUSTOM) {
            String name = stat.getName().replace("minecraft.custom:minecraft.",""); // turn registry to translation key
            String formatted = stat.format(player.getStats().getValue(stat)).replaceAll("[^\\d.]", ""); // remove things that aren't digits or periods
            double finalValue = Double.parseDouble(formatted);
            map.put(name, finalValue);
        }
        return map;
    }
    @LuaWhitelist
    public Map<String, Double> getGeneralStats() { return getGeneralStatistics(); }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "host.get_item_statistic",
            aliases = "getItemStat",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = ItemStackAPI.class,
                            argumentNames = "item"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class},
                            argumentNames = "id"
                    )
            }
    )
    public Map<String, Integer> getItemStatistic(Object id) {
        Map<String, Integer> map = new HashMap<>();
        LocalPlayer player = this.minecraft.player;
        if (player == null || !isHost()) return map;
        String itemKey;
        ItemStackAPI stack;
        if (id instanceof String) {
            stack = WorldAPI.newItem(id.toString(), 1, null); // this handles errors for me and im lazy
            itemKey = id.toString();
        } else {
            stack = (ItemStackAPI) id;
            itemKey = BuiltInRegistries.ITEM.getKey(stack.itemStack.getItem()).toString();
        }

        int timesMined = 0; // init to 0 for if it's not a block
        int timesBroken;
        int timesCrafted;
        int timesUsed;
        int timesPickedUp;
        int timesDropped;
        if (stack.itemStack.getItem() instanceof BlockItem) {
            Block block = stack.getBlockstate().blockState.getBlock();
            Stat<Block> stat = Stats.BLOCK_MINED.get(block);
            timesMined = player.getStats().getValue(stat);
        }

        Stat<Item> brokenStat = Stats.ITEM_BROKEN.get(stack.itemStack.getItem());
        timesBroken = player.getStats().getValue(brokenStat);

        Stat<Item> craftedStat = Stats.ITEM_CRAFTED.get(stack.itemStack.getItem());
        timesCrafted = player.getStats().getValue(craftedStat);

        Stat<Item> usedStat = Stats.ITEM_USED.get(stack.itemStack.getItem());
        timesUsed = player.getStats().getValue(usedStat);

        Stat<Item> pickedUpStat = Stats.ITEM_PICKED_UP.get(stack.itemStack.getItem());
        timesPickedUp = player.getStats().getValue(pickedUpStat);

        Stat<Item> droppedStat = Stats.ITEM_DROPPED.get(stack.itemStack.getItem());
        timesDropped = player.getStats().getValue(droppedStat);


        map.put("mined", timesMined);
        map.put("crafted", timesCrafted);
        map.put("used", timesUsed);
        map.put("picked_up", timesPickedUp);
        map.put("broken", timesBroken);
        map.put("dropped", timesDropped);
        return map;
    }
    @LuaWhitelist
    public Map<String, Integer> getItemStat(Object id) { return getItemStatistic(id); }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "host.get_entity_statistic",
            aliases = "get_entity_stat",
            overloads = @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "type"
                    )
            )
    public Map<String, Integer> getEntityStatistic(String type) {
        Map<String, Integer> map = new HashMap<>();
        LocalPlayer player = this.minecraft.player;
        if (player == null || !isHost()) return map;
        Optional<EntityType<?>> entity = EntityType.byString(type);
        if (entity.isPresent()) {
            Stat<EntityType<?>> killedStat = Stats.ENTITY_KILLED.get(entity.get());
            Stat<EntityType<?>> killedByStat = Stats.ENTITY_KILLED_BY.get(entity.get());

            int timesKilled = player.getStats().getValue(killedStat);
            int timesKilledBy = player.getStats().getValue(killedByStat);

            map.put("killed", timesKilled);
            map.put("killed_by", timesKilledBy);
        }
        else {
            throw new LuaError("Invalid entity type " + type);
        }
        return map;
    }
    @LuaWhitelist
    public Map<String, Integer> getEntityStat(String type) { return getEntityStatistic(type); }

	
	@LuaWhitelist
	@LuaMethodDoc("host.load_local_avatar") // Did not steal this from GoofyPlugin, no proof
	public void loadLocalAvatar(String path) {
		if(!this.isHost) return;
		if(path == null || path.isEmpty()){
			AvatarManager.clearAvatars(FiguraMod.getLocalPlayerUUID());
			AvatarList.selectedEntry = null;
			return;
		}
		Path _path = LocalAvatarFetcher.getLocalAvatarDirectory().resolve(path);
		AvatarManager.loadLocalAvatar(_path);
		AvatarList.selectedEntry = _path;
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_status_effects")
	public List<Map<String, Object>> getStatusEffects() {


		List<Map<String, Object>> list = new ArrayList<>();
		LocalPlayer player = this.minecraft.player;
		if (!this.isHost || player == null)
			return list;

		for (MobEffectInstance effect : player.getActiveEffects()) {
			Map<String, Object> map = new HashMap<>();
			map.put("name", effect.getEffect().getDescriptionId());
			map.put("amplifier", effect.getAmplifier());
			map.put("duration", effect.getDuration());
			map.put("visible", effect.isVisible());

			list.add(map);
		}

		return list;
	}
	@LuaWhitelist
	@LuaMethodDoc("host.set_shader_pack_name")
	public void setShaderPackName(@LuaNotNil String name) {
		if (!isHost || !ClientAPI.HAS_IRIS) return;
		try{
			Iris.getIrisConfig().setShaderPackName(name);
			// Class.forName("net.irisshaders.iris.Iris").getMethod("getIrisConfig")().getMethod("setShaderPackName")(name);
		}catch(Exception ignored){}
	}
	@LuaWhitelist
	@LuaMethodDoc("host.iris_save_config")
	public void irisSaveConfig() {
		if (!isHost || !ClientAPI.HAS_IRIS) return;
		try {
			Iris.getIrisConfig().save();
			// .getMethod("save").invoke(conf);
		}catch(Exception ignored){}
	}
	@LuaWhitelist
	@LuaMethodDoc("host.get_clipboard")
	public String getClipboard() {
		return this.isHost ? this.minecraft.keyboardHandler.getClipboard() : null;
	}
	@LuaWhitelist
	@LuaMethodDoc("host.iris_reload")
	public void irisReload() {
		if (!this.isHost || !ClientAPI.HAS_IRIS) return;
		try {
			Class Iris = Class.forName("net.irisshaders.iris.Iris");
			Iris.getMethod("reload").invoke(Iris);
		}catch (Exception ignored) {
		}
	}

	@LuaWhitelist
	@LuaMethodDoc("client.first_person_model_set_enabled")
	public void fpmSetEnabled(Boolean enabled) {
		if (!this.isHost || !ClientAPI.HAS_FIRSTPERSONMOD) return;
		FirstPersonAPI.setEnabled(enabled);
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = @LuaMethodOverload(
					argumentTypes = String.class,
					argumentNames = "text"
			),
			aliases = "clipboard",
			value = "host.set_clipboard")
	public HostAPI setClipboard(@LuaNotNil String text) {
		if (this.isHost) this.minecraft.keyboardHandler.setClipboard(text);
		return this;
	}

	@LuaWhitelist
	public HostAPI clipboard(@LuaNotNil String text) {
		return setClipboard(text);
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_attack_charge")
	public float getAttackCharge() {
		if(!this.isHost) return 0;
		LocalPlayer player = this.minecraft.player;
		if (player != null)
			return player.getAttackStrengthScale(0f);
		return 0f;
	}

	@LuaWhitelist
	@LuaMethodDoc("host.is_jumping")
	public boolean isJumping() {
		if(!this.isHost) return false;
		LocalPlayer player = this.minecraft.player;
		return player != null && ((LivingEntityAccessor) player).isJumping();
	}

	@LuaWhitelist
	@LuaMethodDoc("host.is_flying")
	public boolean isFlying() {
		if(!this.isHost) return false;
		LocalPlayer player = this.minecraft.player;
		return (player != null && player.getAbilities().flying);
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_reach_distance")
	public double getReachDistance() {
		return this.minecraft.gameMode == null ? 0 : this.minecraft.gameMode.getPickRange();
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_air")
	public int getAir() {
		if(!this.isHost) return 0;
		LocalPlayer player = this.minecraft.player;
		return (player == null) ? 0 : player.getAirSupply();
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_pick_block")
	public Object[] getPickBlock() {
		return this.isHost ? LuaUtils.parseBlockHitResult(minecraft.hitResult) : null;
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_pick_entity")
	public EntityAPI<?> getPickEntity() {
		return this.isHost ? EntityAPI.wrap(minecraft.crosshairPickEntity) : null;
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = Boolean.class,
							argumentNames = "vec"
					),
			},
			value = "host.set_velocity"
	)
	public void setVelocity(Object x, Double y, Double z) {
		if(!canExturaCheat()) return;
		this.minecraft.player.setDeltaMovement(LuaUtils.parseVec3("player_setVelocity", x, y, z).asVec3());

	}
	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = Boolean.class,
							argumentNames = "vec"
					),
			},
			value = "host.travel"
	)
	public void travel(Object x, Double y, Double z) {
		if(!canExturaCheat()) return;
		this.minecraft.player.travel(LuaUtils.parseVec3("player_travel", x, y, z).asVec3());

	}
	@LuaWhitelist
	@LuaMethodDoc("host.set_pose")
	public void setPose(String pose) {
		if(!canExturaCheat()) return;
		try{
			Pose _pose = Pose.valueOf(pose);
			this.minecraft.player.setPose(_pose);
		}catch(IllegalArgumentException ignored){
			throw new LuaError("Invalid pose " + pose);
		}
	}
	@LuaWhitelist
	@LuaMethodDoc("host.set_physics")
	public void setPhysics(Boolean physics) {
		if(!canExturaCheat()) return;
		this.minecraft.player.noPhysics = !physics;
	}
	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = Boolean.class,
							argumentNames = "pos"
					),
			},
			value = "host.set_pos"
	)
	public void setPos(Object x, Double y, Double z) {
		if (!canExturaCheat()) return;
		if(x == null) return;
		LocalPlayer player = this.minecraft.player;
		player.setPos(LuaUtils.parseVec3("player_setPos", x, y, z).asVec3());
	}
	@LuaWhitelist
	@LuaMethodDoc("host.start_riding")
	public void startRiding(EntityAPI entity,boolean bool) {
		if (!canExturaCheat()) return;
		LocalPlayer player = this.minecraft.player;
		if(entity == null) player.removeVehicle();
		Entity t = entity.getEntity();
		if(t == player) throw new LuaError("You cannot ride yourself!");
		player.startRiding(t,bool);
	}

	@LuaWhitelist
	@LuaMethodDoc("host.drop_item")
	public void dropItem(boolean dropAll) {
		if(!this.isHost) return;
		LocalPlayer player = this.minecraft.player;
		player.drop(dropAll == true);
	}
	@LuaWhitelist
	@LuaMethodDoc("host.close_container")
	public void closeContainer() {
		if (!canExturaCheat()) return;
		LocalPlayer player = this.minecraft.player;
		player.closeContainer();
	}
	@LuaWhitelist
	@LuaMethodDoc("host.start_using_item")
	public void startUsingItem(boolean offHand) {
		if (!canExturaCheat()) return;
		LocalPlayer player = this.minecraft.player;
		player.startUsingItem(offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
	}
	@LuaWhitelist
	@LuaMethodDoc("host.stop_using_item")
	public void stopUsingItem() {
		if (!canExturaCheat()) return;
		LocalPlayer player = this.minecraft.player;
		player.stopUsingItem();
	}
	@LuaWhitelist
	@LuaMethodDoc("host.send_open_inventory")
	public void sendOpenInventory() {
		if (!canExturaCheat()) return;
		LocalPlayer player = this.minecraft.player;
		player.sendOpenInventory();
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = {Boolean.class},
							argumentNames = {"playerMovement"}
					)
			},
			value = "host.set_player_movement"
	)
	public void setPlayerMovement(Boolean playerMovement) {
		LocalPlayer player;
		if (!this.isHost || (player = this.minecraft.player) == null) return;
		player.input = (playerMovement ? new ExturaInput(this.minecraft.options) : new NoInput());

	}
	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = {String.class,Boolean.class},
							argumentNames = {"input","state"}
					),
					@LuaMethodOverload(
							argumentTypes = {String.class},
							argumentNames = {"input","state"}
					),
			},
			value = "host.override_player_movement"
	)
	public void overridePlayerMovement(@LuaNotNil String input,Boolean sta) {
		if(!canExturaCheat()) return;
		LocalPlayer player;
		if (!this.isHost || (player = this.minecraft.player) == null) return;
		if(!(player.input instanceof ExturaInput)){
			player.input = new ExturaInput(this.minecraft.options);
		}
		int state = sta == null ? 0 : sta ? 2 : 1;
		ExturaInput inputObj =(ExturaInput) player.input;
		switch(input.toLowerCase()){
			case "up": inputObj.upOverride = state; break;
			case "down": inputObj.downOverride = state; break;
			case "left": inputObj.leftOverride = state; break;
			case "right": inputObj.rightOverride = state; break;
			case "jump": inputObj.jumpOverride = state; break;
			case "shift": inputObj.shiftOverride = state; break;
			default: throw new LuaError("Invalid input");
		}
	}
	@LuaWhitelist
	@LuaMethodDoc("host.get_player_movement")
	public Boolean getPlayerMovement() {
		LocalPlayer player;
		if (!this.isHost || (player = this.minecraft.player) == null) return true;
		return (player.input instanceof NoInput);
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_last_death_pos")
	public FiguraVec3 getLastDeathPos() {
		if(!isHost) return null;
		LocalPlayer player = this.minecraft.player;
		if (player != null) {
			Optional<GlobalPos> deathLocation = player.getLastDeathLocation();
			if(deathLocation.isPresent()) return FiguraVec3.fromBlockPos(deathLocation.get().pos());
		}
		return null;
	}


	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = FiguraVec2.class,
							argumentNames = "vec"
					),
					@LuaMethodOverload(
							argumentTypes = {Double.class, Double.class},
							argumentNames = {"x", "y"}
					)
			},
			value = "host.set_rot"
	)
	public void setRot(Object x, Double y) {
		if(!canExturaCheat()) return;
		FiguraVec2 vec = LuaUtils.parseVec2("player_setRot", x, y);
		LocalPlayer player = this.minecraft.player;
		player.setXRot((float) vec.x);
		player.setYRot((float) vec.y);

	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = {Double.class},
							argumentNames = {"angle"}
					)
			},
			value = "host.set_body_rot"
	)
	public void setBodyRot(Double angle) {
		if(!canExturaCheat()) return;
		LocalPlayer player = this.minecraft.player;
		player.setYBodyRot(angle.floatValue());

	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = {Double.class},
							argumentNames = {"angle"}
					)
			},
			value = "host.set_body_offset_rot"
	)
	public void setBodyOffsetRot(Double angle) {
		if(!canExturaCheat()) return;
		LocalPlayer player = this.minecraft.player;
		player.setYBodyRot( angle.floatValue() + player.getYRot() );
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = {Boolean.class},
							argumentNames = {"hasForce"}
					)
			},
			value = "host.set_gravity"
	)
	public void setGravity(Boolean hasForce) {
		if(!canExturaCheat()) return;
		LocalPlayer player = this.minecraft.player;
		if (player == null) return;
		player.setNoGravity(!hasForce);

	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = {Boolean.class},
							argumentNames = {"hasForce"}
					)
			},
			value = "host.set_drag"
	)
	public void setDrag(Boolean hasForce) {
		if(!canExturaCheat()) return;
		LocalPlayer player = this.minecraft.player;
		if (player == null) return;
		player.setDiscardFriction(hasForce != true);
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(
							argumentTypes = {BlockStateAPI.class, FiguraVec3.class},
							argumentNames = {"block", "pos"}
					),
					@LuaMethodOverload(
							argumentTypes = {BlockStateAPI.class, Double.class, Double.class, Double.class},
							argumentNames = {"block", "x", "y", "z"}
					)
			},
			value = "host.set_block"
	)
	public Boolean setBlock(@LuaNotNil String string, Object x, Double y, Double z) {
		if(!this.isHost) return false;
		BlockPos pos = LuaUtils.parseVec3("setBlock", x, y, z).asBlockPos();
		try {
			Level level = this.minecraft.level;
			BlockState block = BlockStateArgument.block(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).getState();

			level.setBlockAndUpdate(pos,block);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@LuaWhitelist
	@LuaMethodDoc("host.get_key_mappings")
	public Map<String, KeyMappingAPI<?>> getKeyMappings() {
		if (!isHost()) return new HashMap<>();
		HashMap<String, KeyMappingAPI<?>> mappingslist = new HashMap<>();

		Map<String, KeyMapping> mappings = KeyMappingAccessor.getAll();

		mappings.forEach((k,v) -> {
			mappingslist.put(k,KeyMappingAPI.wrap(v));
		});
		return mappingslist;
	}

	// borrowed this from vivecraft - jess

	@LuaWhitelist
	@LuaMethodDoc("host.set_bind_pressed")
	public void setBindPressed(@LuaNotNil String id, boolean state) {
		if (!isHost()) return;
		KeyMapping key = KeyMappingAccessor.getAll().get(id);
		if (key == null)
			throw new LuaError("Failed to find key: \"" + id + "\"");
		key.setDown(state);
		key.clickCount += 1;
	}



	@LuaWhitelist
	@LuaMethodDoc("host.is_chat_verified")
	public boolean isChatVerified() {
		if (!this.isHost) return false;
		ClientPacketListener connection = this.minecraft.getConnection();
		PlayerInfo playerInfo = connection != null ? connection.getPlayerInfo(owner.owner) : null;
		return playerInfo != null && playerInfo.hasVerifiableChat();
	}
	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(argumentTypes = String.class, argumentNames = "string"),
			},
			value = "host.write_to_log"
	)
	public void writeToLog(@LuaNotNil String string) {
		if (!isHost()) return;
		FiguraMod.LOGGER.info("[FIGURA/LUA] -- " + string);
	}

	@LuaWhitelist
	@LuaMethodDoc(
			overloads = {
					@LuaMethodOverload(argumentTypes = String.class, argumentNames = "string"),
			},
			value = "host.warn_to_log"
	)
	public void warnToLog(@LuaNotNil String string) {
		if (!isHost()) return;
		FiguraMod.LOGGER.warn("[FIGURA/LUA] -- " + string);
	}
	
	public Object __index(String arg) {
		if ("unlockCursor".equals(arg))
			return unlockCursor;
		return null;
	}

	@LuaWhitelist
	public void __newindex(@LuaNotNil String key, Object value) {
		if ("unlockCursor".equals(key))
			unlockCursor = (Boolean) value;
		else throw new LuaError("Cannot assign value on key \"" + key + "\"");
	}

	@Override
	public String toString() {
		return "HostAPI";
	}
}

