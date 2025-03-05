package org.figuramc.figura.lua.api;

import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.config.Configs;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.LuaTable;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.permissions.Permissions;
// import org.figuramc.figura.lua.api.java.ExturaClassAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Objects;
import java.net.*;
import java.util.HashMap;
import java.lang.Class;
import java.util.concurrent.CompletableFuture;

import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.luaj.vm2.LuaFunction;

@LuaWhitelist
@LuaTypeDoc(
		name = "ExturaAPI(Extura)",
		value = "extura"
)
public class ExturaAPI {
	private final Avatar owner;
	private final boolean isHost;
	private final static Integer VERSION = 7;
	@LuaWhitelist
	@LuaFieldDoc("extura.java_variables")
	private final HashMap<String,Object> javaVariables = new HashMap<String,Object>();

	public ExturaAPI(Avatar owner) {
		this.isHost = (this.owner = owner).isHost;
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
	// @LuaWhitelist
	// @LuaMethodDoc("extura.get_class")
	// public ExturaClassAPI getClass(String arg) {
	// 	if (arg == null || !this.isHost) return null;
	// 	return ExturaClassAPI.fromString(arg);
	// }
	@LuaWhitelist
	@LuaMethodDoc("extura.async_lua_function")
	public void asyncLuaFunction(LuaFunction func) {
		if (!this.isHost) return;
		CompletableFuture.runAsync(() -> {
			func.call();
		});
	}


	@LuaWhitelist
	public Object __index(String arg) {
		if(arg.startsWith("java_")){
			return javaVariables.get(arg.substring(5));
		}
		return switch (arg.toLowerCase()) {
			case "ishost" -> isHost;
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
