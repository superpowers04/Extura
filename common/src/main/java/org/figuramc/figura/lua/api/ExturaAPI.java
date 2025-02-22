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
	@LuaMethodDoc("extura.reflect_java")
	public Object reflectJava(@LuaNotNil LuaTable stuff) {
		if (!Configs.EXPOSE_SENSITIVE_LIBRARIES.value || !this.isHost) return null;
			int max = stuff.length();
			if(max == 0) return null;
			Object last = null;

			for (int index = 1;index <= max;index++) {
				LuaTable tbl = stuff.get(index).checktable();

				String type = ((tbl.get("type").optjstring(tbl.get(1).checkjstring())) ).toLowerCase();
				String path = ((tbl.get("path").optjstring(tbl.get(2).checkjstring())) );
				LuaValue store = tbl.get("store");
				switch(type){
					case "get":{
						last = javaVariables.get(path);
						break;
					}
					case "set":{
						last = fromLua(tbl.get("store"));
						javaVariables.put(path,last);
						break;
					}
					case "class":{
						try{
							last = Class.forName(path);
						}catch(ClassNotFoundException e){
							throw new LuaError("No such class '"+path+"'");
						}
						if(store.isstring()) javaVariables.put(store.tojstring(),last);
						break;
					}
					case "method":{
						LuaTable the = tbl.get("args").opttable(tbl.get(4).checktable());
						int len = the.length();
						Class<?>[] classes = new Class<?>[len];
						for (int ci = 0;ci < len;ci++) {
							String className = the.get(ci).checkjstring();
							try{

								Object VARI = getJava(className);
								classes[ci] = (VARI==null ? Class.forName(path) : ((Class<?>)VARI));
							}catch(ClassNotFoundException e){
								throw new LuaError("No such class '"+path+"'");
							}
						}
						try{
							last = ((Class<?>) last).getDeclaredMethod(path,classes);
						}catch(NoSuchMethodException e){
							throw new LuaError("No such method '"+path+"' on "+last);
						}
						if(store.isstring()) javaVariables.put(store.tojstring(),last);
						break;
					}
					case "field":{
						try{
							last = ((Class<?>) last).getDeclaredField(path).get(last);
						}catch(NoSuchFieldException e){
							throw new LuaError("No such field '"+path+"' on "+last);
						}catch(IllegalAccessException e){
							throw new LuaError("Unable to access field '"+path+"' on "+last);
						}
						if(store.isstring()) javaVariables.put(store.tojstring(),last);
						break;
					}
					case "setfield":{
						try{
							((Class<?>) last).getDeclaredField(path).set(last,fromLua(tbl.get("store")));
						}catch(NoSuchFieldException e){
							throw new LuaError("No such field '"+path+"' on "+last);
						}catch(IllegalAccessException e){
							throw new LuaError("Unable to access field '"+path+"' on "+last);
						}
						break;
					}
					case "callmethod":{
						LuaTable the = tbl.get("args").opttable(tbl.get(4).checktable());
						int len = the.length();
						Object[] args = new Object[len];
						for (int ci = 0;ci < len;ci++) {
							LuaValue className = the.get(ci);
							Object VARI = getJava(className);
							args[ci] = (VARI == null ? className : VARI);
						}

						try{
							last = ((Method) last).invoke(last,args);
						}catch(IllegalAccessException e){
							throw new LuaError("Unable to invoke "+last);
						}catch(InvocationTargetException e){
							try{
								last = ((Method) last).invoke(args);
							}catch(IllegalAccessException er){
								throw new LuaError("Unable to invoke "+last);
							}catch(InvocationTargetException er){
								throw new LuaError("Unable to invoke "+last);
							}
						}
						if(store.isstring()) javaVariables.put(store.tojstring(),last);
						break;
					}
					default:{
						throw new LuaError("Invalid reflect type " + type);
					}
				}
			}

		return last;

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
