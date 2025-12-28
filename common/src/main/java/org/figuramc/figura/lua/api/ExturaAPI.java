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
	@LuaMethodDoc("extura.new_class_instance")
	public Object newClassInstance(@LuaNotNil String path,LuaValue[] arr) {
		if (!Configs.EXPOSE_SENSITIVE_LIBRARIES.value || !this.isHost) return null;
		int len = arr.length;
		Object[] objects = new Object[len];
		for (int i = 0;i < len;i++) {
			objects[i] = typeManager.luaToJava(arr[i]);
		}
		Class<?>[] classes = new Class<?>[len];
		for (int i = 0;i < len;i++) {
			classes[i] = arr[i].getClass();
		}
		try{
			Class<?> clazz = Class.forName(path);
			Constructor[] constors = clazz.getDeclaredConstructors();
			len = constors.length;
			for(int i = 0;i < len;i++){
				Constructor constor = constors[i];
				if(constor.getParameterTypes() != classes) continue;
				return typeManager.javaToLua(constor.newInstance(arr));
			}
			throw new LuaError("No such constructor found of types passed "+classes+"!");
		}catch(ClassNotFoundException x){
			x.printStackTrace();
			throw new LuaError("Class not found '"+path+"'!");
		}catch (IllegalAccessException x) {
			x.printStackTrace();
			throw new LuaError("Unable to access '"+path+"'!");
		}catch (InstantiationException x) {
			x.printStackTrace();
			throw new LuaError("Unable to instantiate '"+path+"'! ");
		}catch (InvocationTargetException x) {
			x.printStackTrace();
			throw new LuaError("Unable to invoke '"+path+"'! ");
		}
	}
	@LuaWhitelist
	@LuaMethodDoc("extura.wrap_class")
	public Object wrapClass(@LuaNotNil String path) {
		if (!Configs.EXPOSE_SENSITIVE_LIBRARIES.value || !this.isHost) return null;
		try{
			return typeManager.javaToLua(Class.forName(path));
		}catch(ClassNotFoundException e){
			throw new LuaError("Class not fount '"+path+"'!");
		}
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


	private final ExturaLuaTypeManager typeManager = new ExturaLuaTypeManager();
	/**
	 * One LuaTypeManager per LuaRuntime, so that people can be allowed to edit the metatables within.
	 */
	private class ExturaLuaTypeManager {
		private final Map<Class<?>, LuaTable> metatables = new HashMap<>();
		

		public void generateMetatableFor(Class<?> clazz) {
			if (clazz == null || clazz == Object.class)
				return;
			if (metatables.containsKey(clazz))
				return;
			try {
				generateMetatableFor(clazz.getSuperclass());
				for (Class<?> iface : clazz.getInterfaces())
					generateMetatableFor(iface);
			} catch (IllegalArgumentException ignored) {}

			LuaTable metatable = new LuaTable();

			LuaTable indexTable = new LuaTable();
			extracted(clazz, metatable, indexTable);

			if (metatable.rawget("__index") == LuaValue.NIL)
				metatable.set("__index", indexTable);

			// if we don't have a special toString, then have our toString give the type name from the annotation
			if (metatable.rawget("__tostring") == LuaValue.NIL) {
				metatable.set("__tostring", new OneArgFunction() {
					private final LuaString val = LuaString.valueOf(clazz.getName());
					@Override
					public LuaValue call(LuaValue arg) {
						return val;
					}
				});
			}

			// if we don't have a special __index, then have our indexer look in the next metatable up in the java inheritance.
			if (indexTable.rawget("__index") == LuaValue.NIL) {
				LuaTable superclassMetatable = metatables.get(clazz.getSuperclass());
				if (superclassMetatable != null) {
					LuaTable newMetatable = new LuaTable();
					newMetatable.set("__index", superclassMetatable.get("__index"));
					indexTable.setmetatable(newMetatable);
				}
			}
			metatable.set("__index", new TwoArgFunction() {
				@Override
				public LuaValue call(LuaValue arg1, LuaValue arg2) {
					LuaValue result = indexTable.get(arg2);
					if(result != LuaValue.NIL) return result;
					if(!arg2.isstring()) return LuaValue.NIL;
					try{
						Object fieldValue = clazz.getField(arg2.checkjstring()).get(luaToJava(arg1));
						return javaToLua(fieldValue).arg1();
					}catch(NoSuchFieldException ignored){
					}catch(NullPointerException ignored){
					}catch(IllegalAccessException ignored){
					}
					return LuaValue.NIL;
				}
			});

			metatables.put(clazz, metatable);
		}

		private void extracted(Class<?> clazz, LuaTable metatable, LuaTable indexTable) {
			Class<?> currentClass = clazz;
			while (currentClass != null) {
				for (Method method : currentClass.getDeclaredMethods()) {
					indexTable.set(method.getName(), getWrapper(method));
				}
				for (Class<?> iface: currentClass.getInterfaces()) extracted(iface, metatable, indexTable);
				currentClass = currentClass.getSuperclass();
			}
		}

		public void dumpMetatables(LuaTable table) {
			for (Map.Entry<Class<?>, LuaTable> entry : metatables.entrySet()) {
				String name = getTypeName(entry.getKey());
				// if (table.get(name) != LuaValue.NIL)
				// 	throw new IllegalStateException("Two classes have the same type name: " + name);
				table.set(name, entry.getValue());
			}
		}

		private final Map<Class<?>, String> namesCache = new HashMap<>();
		public String getTypeName(Class<?> clazz) {
			return namesCache.computeIfAbsent(clazz, someClass -> {
				if (someClass.isAnnotationPresent(LuaTypeDoc.class))
					return someClass.getAnnotation(LuaTypeDoc.class).name();
				return someClass.getSimpleName();
			});
		}

		private static boolean[] getRequiredNotNil(Method method) {
			Parameter[] params = method.getParameters();
			boolean[] result = new boolean[params.length];
			for (int i = 0; i < params.length; i++)
				if (params[i].isAnnotationPresent(LuaNotNil.class))
					result[i] = true;
			return result;
		}
		private final Object[] emptyArgs = new Object[0];
		public VarArgFunction getWrapper(Method method) {
			if(method.getParameterTypes().length == 0) {
				if(Modifier.isStatic(method.getModifiers())){
					return new StaticFunctionWithoutArgs(method);
				}
				return new InstanceFunctionWithoutArgs(method);
			}
			return new FunctionWithArgs(method);
		}

		private class StaticFunctionWithoutArgs extends VarArgFunction{
			public final Method method;
			public StaticFunctionWithoutArgs(Method method){
				super();
				this.method = method;
			}
			@Override
			public Varargs invoke(Varargs args) {
				return invokeMethod();
			}

			public Varargs invokeMethod(Object obj, Object[] args) {
				// Invoke the wrapped method
				Object result;
				try {
					result = method.invoke(obj, args);
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw e.getCause() instanceof LuaError l ? l : new LuaError(e.getCause());
				}
				// Convert the return value
				return result instanceof Varargs v ? v : javaToLua(result);
			}
			public Varargs invokeMethod(Object obj) {return invokeMethod(obj, null);}
			public Varargs invokeMethod() {return invokeMethod(null, null);}


			@Override
			public String tojstring() {return "function: " + method.getName();}
		}
		private class InstanceFunctionWithoutArgs extends StaticFunctionWithoutArgs{

			public Object caller;
			public final Class<?> clazz;

			public InstanceFunctionWithoutArgs(Method method){
				super(method);
				clazz = method.getDeclaringClass();
			}
			public void getCaller(Varargs args){
				try {
					caller = args.checkuserdata(1, clazz);
				} catch (LuaError e) {
					String methodName = method.getName();
					String targetType = getTypeName(clazz);
					throw new LuaError(String.format(
							"Use a colon (:) to call %s on a %s, instead of a dot.\nFor example, change .%s( to :%s(",
							methodName, targetType, methodName, methodName
					));
				}
			}
			@Override
			public Varargs invoke(Varargs args) {
				getCaller(args);
				return invokeMethod(caller);
			}
		}
		private class FunctionWithArgs extends InstanceFunctionWithoutArgs{
			public final Class<?>[] argumentTypes;
			public final boolean isStatic, canOffset;
			public final Object[] actualArgs, defaultArgs;
			public final boolean[] requiredNotNil;

			public FunctionWithArgs(Method method){
				super(method);
				isStatic = Modifier.isStatic(method.getModifiers());
				argumentTypes = method.getParameterTypes();

				canOffset = argumentTypes.length > 0 && !argumentTypes[0].isAssignableFrom(clazz);
				actualArgs = new Object[argumentTypes.length];
				defaultArgs = new Object[argumentTypes.length]; 
				requiredNotNil = getRequiredNotNil(method);
				for(int i = 0; i < argumentTypes.length; i++){
					defaultArgs[i] = switch (argumentTypes[i].getName()) {
						case "double" -> 0D;
						case "int" -> 0;
						case "long" -> 0L;
						case "float" -> 0f;
						case "boolean" -> false;
						default -> null;
					};
				}
			}

			@Override
			public Varargs invoke(Varargs args) {
				int offset;
				if (isStatic) {
					// dirty hack for QOL of ignoring the first argument if the method is static and the arg matches the class type
					offset = canOffset && args.isuserdata(1) && clazz.isAssignableFrom(args.checkuserdata(1).getClass()) ? 2 : 1;
				}else{
					offset = 2;
					getCaller(args);
				}


				// Fill in actualArgs from args
				for (int i = 0; i < argumentTypes.length; i++) {
					int argIndex = i + offset;
					if (args.isnil(argIndex)){
						if(requiredNotNil[i]) 
							throw new LuaError("bad argument: " + method.getName() + " " + argIndex + " does not allow nil values, expected " + FiguraDocsManager.getNameFor(argumentTypes[i]));
					}else if (argIndex <=  args.narg()) {
						try {
							actualArgs[i] = switch (argumentTypes[i].getName()) {
								case "java.lang.Number", "java.lang.Double", "double" -> args.checkdouble(argIndex);
								case "java.lang.String" -> args.checkjstring(argIndex);
								case "java.lang.Boolean", "boolean" -> args.toboolean(argIndex);
								case "java.lang.Float", "float" -> (float) args.checkdouble(argIndex);
								case "java.lang.Integer", "int" -> args.checkint(argIndex);
								case "java.lang.Long", "long" -> args.checklong(argIndex);
								case "org.luaj.vm2.LuaTable" -> args.checktable(argIndex);
								case "org.luaj.vm2.LuaFunction" -> args.checkfunction(argIndex);
								case "org.luaj.vm2.LuaValue" -> args.arg(argIndex);
								case "java.lang.Object" -> luaToJava(args.arg(argIndex));
								default -> argumentTypes[i].getName().startsWith("[") ? luaVarargToJava(args, argIndex, argumentTypes[i]) : args.checkuserdata(argIndex, argumentTypes[i]);
							};
						} catch (LuaError err) {
							String expectedType = FiguraDocsManager.getNameFor(argumentTypes[i]);
							String actualType;
							if (args.arg(argIndex).type() == LuaValue.TUSERDATA)
								actualType = FiguraDocsManager.getNameFor(args.arg(argIndex).checkuserdata().getClass());
							else
								actualType = args.arg(argIndex).typename();
							throw new LuaError("bad argument #" + argIndex + " to '" + method.getName() + "' (" + expectedType + " expected, but got " + actualType+")");
						}
						continue;
					}
					actualArgs[i] = defaultArgs[i];
					
				}

				return invokeMethod(caller, actualArgs);
			}
		}


		private LuaValue wrapClass(Class<?> clazz) {
			LuaTable metatable = metatables.get(clazz);
			if(metatable == null){
				generateMetatableFor(clazz);
				metatable = metatables.get(clazz);
			}
			// while (metatable == null) {
			//     clazz = clazz.getSuperclass();
			//     if (clazz == Object.class)
			//         throw new RuntimeException("Attempt to wrap illegal type " + clazz.getName() + " (not registered in LuaTypeManager's \"metatables\" map)!");
			//     metatable = metatables.get(clazz);
			// }

			LuaUserdata result = new LuaUserdata(clazz);
			result.setmetatable(metatable);
			return result;
		}
		private LuaValue wrap(Object instance) {
			Class<?> clazz = instance.getClass();
			LuaTable metatable = metatables.get(clazz);
			if(metatable == null){
				generateMetatableFor(clazz);
				metatable = metatables.get(clazz);
			}

			LuaUserdata result = new LuaUserdata(instance);
			result.setmetatable(metatable);
			return result;
		}

		private LuaValue wrapMap(Map<?, ?> map) {
			LuaTable table = new LuaTable();

			for (Map.Entry<?, ?> entry : map.entrySet()) {
				LuaValue key = javaToLua(entry.getKey()).arg1();
				LuaValue val = javaToLua(entry.getValue()).arg1();
				table.set(key, val);
			}

			return table;
		}

		private LuaValue wrapCollection(Collection<?> collection) {
			LuaTable table = new LuaTable();

			int i = 1;
			for (Object o : collection) {
				table.set(i++, javaToLua(o).arg1());
			}

			return table;
		}

		private Varargs wrapArray(Object array) {
			int len = Array.getLength(array);
			LuaValue[] args = new LuaValue[len];

			for (int i = 0; i < len; i++)
				args[i] = javaToLua(Array.get(array, i)).arg1();

			return LuaValue.varargsOf(args);
		}

		public Object luaVarargToJava(Varargs args, int argIndex, Class<?> argumentType) {
			if (args.arg(argIndex).istable()) {
				return luaVarargToJava(args.checktable(argIndex).unpack(), 1, argumentType);
			}
			Object[] obj = new Object[args.narg() - argIndex + 1];
			for (int start = argIndex; argIndex <= args.narg(); argIndex++) {
				obj[argIndex - start] = switch (argumentType.getName()) {
					case "[Ljava.lang.Number;", "[Ljava.lang.Double;", "[D" -> args.checkdouble(argIndex);
					case "[Ljava.lang.String;" -> args.checkjstring(argIndex);
					case "[Ljava.lang.Boolean;", "[B" -> args.toboolean(argIndex);
					case "[Ljava.lang.Float;", "[F" -> (float) args.checkdouble(argIndex);
					case "[Ljava.lang.Integer;", "[I" -> args.checkint(argIndex);
					case "[Ljava.lang.Long;", "[J" -> args.checklong(argIndex);
					case "[Lorg.luaj.vm2.LuaTable;" -> args.checktable(argIndex);
					case "[Lorg.luaj.vm2.LuaFunction;" -> args.checkfunction(argIndex);
					case "[Lorg.luaj.vm2.LuaValue;" -> args.arg(argIndex);
					case "[Ljava.lang.Object;" -> luaToJava(args.arg(argIndex));
					default -> args.checkuserdata(argIndex, argumentType);
				};
			}
			return Arrays.copyOf(obj, obj.length, (Class<? extends Object[]>) argumentType);
			
		}

		// we need to allow string being numbers here
		// however in places like pings and print we should keep strings as strings
		public Object luaToJava(LuaValue val) {
			if (val.istable())
				return val.checktable();
			else if (val.isnumber())
				if (val instanceof LuaInteger i) // dumb
					return i.checkint();
				else if (val.isint() && val instanceof LuaString s) // very dumb
					return s.checkint();
				else
					return val.checkdouble();
			else if (val.isstring())
				return val.checkjstring();
			else if (val.isboolean())
				return val.checkboolean();
			else if (val.isfunction())
				return val.checkfunction();
			else if (val.isuserdata())
				return val.checkuserdata(Object.class);
			else
				return null;
		}

		public Varargs javaToLua(Object val) {
			if (val == null)
				return LuaValue.NIL;
			else if (val instanceof LuaValue l)
				return l;
			else if (val instanceof Double d)
				return LuaValue.valueOf(d);
			else if (val instanceof String s)
				return LuaValue.valueOf(s);
			else if (val instanceof Boolean b)
				return LuaValue.valueOf(b);
			else if (val instanceof Integer i)
				return LuaValue.valueOf(i);
			else if (val instanceof Float f)
				return LuaValue.valueOf(f);
			else if (val instanceof Byte b)
				return LuaValue.valueOf(b);
			else if (val instanceof Long l)
				return LuaValue.valueOf(l);
			else if (val instanceof Character c)
				return LuaValue.valueOf(c);
			else if (val instanceof Short s)
				return LuaValue.valueOf(s);
			else if (val instanceof Map<?,?> map)
				return wrapMap(map);
			else if (val instanceof Collection<?> collection)
				return wrapCollection(collection);
			else if (val.getClass().isArray())
				return wrapArray(val);
			else if (val instanceof Class<?> c)
				return wrapClass(c);
			else if (val instanceof Component c)
				return LuaValue.valueOf(Component.Serializer.toJson(c));
			else
				return wrap(val);
		}
	}


}
