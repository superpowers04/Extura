package org.figuramc.figura.lua.api;

import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.config.Configs;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.LuaTable;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.api.net.NetworkingAPI;
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
	@LuaMethodDoc("extura.async_http_get")
	public void asyncHttpGet(String arg, LuaFunction func,String method) {
		if (arg == null || (!this.isHost)) return;
		// if (owner.permissions.get(Permissions.NETWORKING) < 1) throw new LuaError("This avatar's permissions does not allow networking!");
		NetworkingAPI.securityCheckLink(owner,arg);
		// if (owner.permissions.get(Permissions.NETWORKING) < 1) throw new LuaError("This avatar's permissions does not allow networking!");
		CompletableFuture.runAsync(() -> {
			func.call(httpGet(arg,method));
		});
		return;
	}
	@LuaWhitelist
	@LuaMethodDoc("extura.http_get")
	public String httpGet(String arg,String method) {
		if (arg == null || (!this.isHost))  return null;
		NetworkingAPI.securityCheckLink(owner,arg);
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
