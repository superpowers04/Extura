package org.figuramc.figura.lua.api;

import org.luaj.vm2.LuaError;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaTypeDoc;


@LuaWhitelist
@LuaTypeDoc(
        name = "ExturaAPI",
        value = "extura"
)
public class ExturaAPI {
    private final Avatar owner;
    private final boolean isHost;
    private final Integer VERSION = 2;

    public ExturaAPI(Avatar owner) {
        this.owner = owner;
        this.isHost = owner.isHost;
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
        switch (key) {
            default -> throw new LuaError("Cannot assign value on key \"" + key + "\"");
        }
    }

    @Override
    public String toString() {
        return "ExturaAPI";
    }
}
