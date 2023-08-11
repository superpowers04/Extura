package org.figuramc.figura.lua.api;

import com.google.gson.*;
import org.luaj.vm2.LuaError;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.luaj.vm2.LuaTable;

import java.util.HashMap;

@LuaWhitelist
@LuaTypeDoc(
        name = "JsonAPI",
        value = "json"
)
public class JsonAPI {
    static Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public JsonAPI() {}
//    @LuaWhitelist
//    @LuaMethodDoc("json.encode")
//    public String encode(HashMap<Object,Object> object) {
//        return GSON.toJson(object);
//    }

    @LuaWhitelist
    @LuaMethodDoc("json.decode")
    public Object decode(String jsonString) {
        return GSON.fromJson(jsonString, HashMap.class);
    }
    @LuaWhitelist
    public Object __index(String arg) {return null;}

    @LuaWhitelist
    public void __newindex(@LuaNotNil String key) {throw new LuaError("Cannot assign value on key \"" + key + "\"");}

    @Override
    public String toString() {
        return "JsonAPI";
    }
}
