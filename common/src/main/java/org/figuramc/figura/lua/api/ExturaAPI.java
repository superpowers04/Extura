package org.figuramc.figura.lua.api;

import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;

import net.minecraft.network.chat.Component;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.config.Configs;
import org.luaj.vm2.*;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaTypeManager;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.FiguraDocsManager;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.permissions.Permissions;
// import org.figuramc.figura.lua.api.java.ExturaClassAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.util.HashMap;
import java.lang.Class;
import java.util.concurrent.CompletableFuture;


import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/* TODO ADD SET_MINECRAFT_SETTING*/

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
		Field field;
		try {
			field = Configs.class.getDeclaredField(arg);
		}catch(java.lang.NoSuchFieldException ignored){
			return null;
		}
		try {
			return ((ConfigType<?>) field.get(null)).value;
		}catch(java.lang.IllegalAccessException ignored){
			return null;
		}
	}
	@LuaWhitelist
	@LuaMethodDoc("extura.get_figura_settings")
	public HashMap<String, Object> getFiguraSettings() {
		if (!this.isHost) return null;
		Field[] fieldList= Configs.class.getDeclaredFields();
		HashMap<String, Object> map = new HashMap<>();
		for (Field field : fieldList) {
			try {
				if(field.get(null) instanceof ConfigType<?> cfg)
					map.put(field.getName(), cfg.value);
			}catch(Exception ignored){}
		}
			// return ((ConfigType<?>) obj.get(null)).value;
		return map;
	}

	@LuaWhitelist
	@LuaMethodDoc("extura.model_part_path")
	public List<String> modelPartPath(FiguraModelPart part) {
		List<String> path = new ArrayList<>();
		FiguraModelPart p = part;
		while (p != null) {
			path.add(0,p.getName());
			p = p.parent;
		}
		return path;
	}

	@LuaWhitelist
	@LuaMethodDoc("extura.get_minecraft_setting")
	public Object getMinecraftSetting(String arg) {
		if (arg == null || !this.isHost) return null;
		Field obj;
		try {
			obj = Options.class.getDeclaredField(arg);
		}catch(java.lang.NoSuchFieldException ignored){
			return null;
		}
		try {
			return obj.get(Minecraft.getInstance().options);
		}catch(java.lang.IllegalAccessException ignored){
			return null;
		}
	}
	@LuaWhitelist
	@LuaMethodDoc("extura.get_minecraft_settings")
	public HashMap<String, Object> getMinecraftSettings() {
		if (!this.isHost) return null;
		Field[] fieldList= Options.class.getDeclaredFields();
		HashMap<String, Object> map = new HashMap<>();

		Options options = Minecraft.getInstance().options;
		for (Field field : fieldList) {
			try {
				map.put(field.getName(),owner.luaRuntime.typeManager.javaToLua(field.get(options)));
			}catch(java.lang.IllegalAccessException ignored){}catch(java.lang.RuntimeException ignored){}
		}
		return map;
	}
	// @LuaWhitelist
	// @LuaMethodDoc("extura.get_class")
	// public ExturaClassAPI getClass(String arg) {
	// 	if (arg == null || !this.isHost) return null;
	// 	return ExturaClassAPI.fromString(arg);
	// }
	@LuaWhitelist
	@LuaMethodDoc("extura.http_get")
	public String httpGet(String arg,String method) {
		if (!Configs.EXPOSE_SENSITIVE_LIBRARIES.value || arg == null || (!this.isHost && !Configs.EXPOSE_HTTP.value))  return null;
		if (owner.permissions.get(Permissions.NETWORKING) < 1) throw new LuaError("This avatar's permissions does not allow networking!");
		if (method == null || method.isEmpty()) method = "GET";
		try{
			// https://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html my beloved
			URLConnection connec = new URI(arg).toURL().openConnection();
			if(connec instanceof HttpURLConnection){
				((HttpURLConnection)connec).setRequestMethod(method);

			}
			connec.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(connec.getInputStream()));
			StringBuilder ret = new StringBuilder();
			String inLine;
			while ((inLine = in.readLine()) != null) ret.append(inLine);
			in.close();
			return ret.toString();
		}catch (ProtocolException err) {
			throw new LuaError("Request method '"+method +"' not valid for HTTP: " + err);
		}catch (URISyntaxException | MalformedURLException err) {
			throw new LuaError("Unable to parse URL: " + err);
		}catch(IOException err){
			throw new LuaError("Unable to send request: " + err);
		}
		// return null;
	}
	@LuaWhitelist
	@LuaMethodDoc("extura.async_lua_function")
	public void asyncLuaFunction(LuaFunction func) {
		if (!this.isHost) return;
		CompletableFuture.runAsync(() -> {
			func.call();
		});
	}
	@LuaWhitelist
	@LuaMethodDoc("extura.async_http_get")
	public void asyncHttpGet(String arg, LuaFunction func,String method) {
		if (!Configs.EXPOSE_SENSITIVE_LIBRARIES.value || arg == null || (!this.isHost && !Configs.EXPOSE_HTTP.value)) return;
		// if (owner.permissions.get(Permissions.NETWORKING) < 1) throw new LuaError("This avatar's permissions does not allow networking!");
		if (owner.permissions.get(Permissions.NETWORKING) < 1) throw new LuaError("This avatar's permissions does not allow networking!");
		CompletableFuture.runAsync(() -> {
			func.call(httpGet(arg,method));
		});
		return;
	}


	public Object getJava(LuaValue name){
		if(!name.isstring()) return null;
		return getJava(name.tojstring());
	}
	public Object getJava(String name){
		return (name.startsWith("$") ? javaVariables.get(name.substring(1)) : javaVariables.get(name));
	}
	public Object getJavaOnlyDollar(String name){
		return (name.startsWith("$") ? javaVariables.get(name.substring(1)) : name);
	}
	public Object fromLua(LuaValue value) {
		switch(value.type()){
			case LuaValue.TBOOLEAN: return value.toboolean();
			case LuaValue.TNUMBER:{
				if(value.isint()) return value.toint();
				if(value.islong()) return value.tolong();
				return value.tofloat();
			}
			case LuaValue.TSTRING: return getJavaOnlyDollar(value.tojstring());
			// case TTABLE: 
			// case TFUNCTION: 
			case LuaValue.TUSERDATA: return value.checkuserdata();
			// case TTHREAD: 
		}
		return value;
	}

	@LuaWhitelist
	public Object __index(String arg) {
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
