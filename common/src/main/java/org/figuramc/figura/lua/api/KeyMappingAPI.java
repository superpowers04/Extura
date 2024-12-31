package org.figuramc.figura.lua.api;

import net.minecraft.client.KeyMapping;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.jetbrains.annotations.NotNull;

@LuaWhitelist
@LuaTypeDoc(
        name = "KeyMappingAPI",
        value = "keymapping"
)
public class KeyMappingAPI<T extends KeyMapping> {

    @NotNull
    private final T mapping;

    @LuaWhitelist
    @LuaFieldDoc("keymapping.id")
    public final String id;

    public KeyMappingAPI(T keymapping) {
        this.mapping = keymapping;
        this.id = keymapping.getName();
    }

    public static KeyMappingAPI<?> wrap(KeyMapping m) {
        return new KeyMappingAPI<>(m);
    }

    @LuaWhitelist
    @LuaMethodDoc("keymapping.get_id")
    public String getID() {
        return id;
    }

    @LuaWhitelist
    @LuaMethodDoc("keymapping.get_category")
    public String getCategory() {
        return mapping.getCategory();
    }

    @LuaWhitelist
    @LuaMethodDoc("keymapping.click")
    public KeyMappingAPI<T> click() {
        ++mapping.clickCount;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("keymapping.set_down")
    public KeyMappingAPI<T> setDown(boolean state) {
        mapping.setDown(state);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("keymapping.is_down")
    public boolean isDown() {
        return mapping.isDown();
    }

    @LuaWhitelist
    @LuaMethodDoc("keymapping.is_unbound")
    public boolean isUnbound() {
        return mapping.isUnbound();
    }

    @LuaWhitelist
    @LuaMethodDoc("keymapping.get_key")
    public String getKey() {
        return mapping.saveString();
    }

    @LuaWhitelist
    @LuaMethodDoc("keymapping.get_key_name")
    public String getKeyName() {
        return mapping.getTranslatedKeyMessage().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc("keymapping.is_default")
    public boolean isDefault() {
        return mapping.isDefault();
    }

    @LuaWhitelist
    @LuaMethodDoc("keymapping.get_default_key")
    public String getDefaultKey() {
        return mapping.getDefaultKey().getName();
    }

    @Override
    public String toString() {
        return (id + " (KeyMapping)");
    }
}