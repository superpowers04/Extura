package org.figuramc.figura.lua.api;

import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.config.Configs;
import org.luaj.vm2.LuaError;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.lang.reflect.Field;
import org.figuramc.figura.lua.docs.LuaMethodDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "ExturaAPI",
        value = "extura"
)
public class ExturaAPI {
    private final Avatar owner;
    private final boolean isHost;
    private final static Integer VERSION = 3;

    public ExturaAPI(Avatar owner) {
        this.owner = owner;
        this.isHost = owner.isHost;
    }
    @LuaWhitelist
    @LuaMethodDoc("extura.get_figura_setting")
    public Object getFiguraSetting(String arg) {
        if (arg == null || !this.isHost) return null;
        Field obj;
        try {
             obj = Configs.class.getDeclaredField(arg);
        }catch(java.lang.NoSuchFieldException ignored){
            return null;
        }
        try {
            return ((ConfigType<?>) obj.get(null)).value;
        }catch(java.lang.IllegalAccessException ignored){
            return null;
        }
    }
    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
        	case "isHost" -> isHost;
        	case "version" -> VERSION;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String key) {
//        switch (key) {
//            default ->
        throw new LuaError("Cannot assign value on key \"" + key + "\"");
//        }
    }

    @Override
    public String toString() {
        return "ExturaAPI";
    }
}
