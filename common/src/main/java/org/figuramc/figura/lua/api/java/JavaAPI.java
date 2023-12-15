package org.figuramc.figura.lua.api.java;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;
import org.figuramc.figura.avatar.Avatar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import static org.figuramc.figura.lua.api.java.ClassAPI.mapn;

@LuaWhitelist
@LuaTypeDoc(
	name = "JavaAPI",
	value = "java"
)
public class JavaAPI {
	private final Avatar owner;
	private final boolean isHost;

	public JavaAPI(Avatar owner) {
		this.isHost = (this.owner = owner).isHost;
	}
	@LuaWhitelist
	@LuaMethodDoc("java.get_class")
	public ClassAPI<?> getClass(String name) {
		if(!this.isHost) throw new LuaError("You can't use JavaAPI outside of host environment");
		try {
			final var klass = getClass().getClassLoader().loadClass(name);
			if (klass != null) {
				return new ClassAPI<>(klass);
			} else {
				return null;
			}
		} catch (ClassNotFoundException ignored) {
			return null;
		}
	}

	@LuaWhitelist
	@LuaMethodDoc("java.wrap_primitive")
	public InstanceAPI<Float> wrapFloat(LuaValue value) {
		if(!this.isHost) throw new LuaError("You can't use JavaAPI outside of host environment");
		value.checknumber();
		return wrap(value.tofloat());
	}
	@LuaWhitelist
	@LuaMethodDoc("java.wrap_primitive")
	public InstanceAPI<Double> wrapDouble(LuaValue value) {
		if(!this.isHost) throw new LuaError("You can't use JavaAPI outside of host environment");
		value.checknumber();
		return wrap(value.todouble());
	}
	@LuaWhitelist
	@LuaMethodDoc("java.wrap_primitive")
	public InstanceAPI<Integer> wrapInt(LuaValue value) {
		if(!this.isHost) throw new LuaError("You can't use JavaAPI outside of host environment");
		value.checknumber();
		return wrap(value.checkint());
	}
	@LuaWhitelist
	@LuaMethodDoc("java.wrap_primitive")
	public InstanceAPI<Byte> wrapByte(LuaValue value) {
		if(!this.isHost) throw new LuaError("You can't use JavaAPI outside of host environment");
		value.checknumber();
		return wrap(value.tobyte());
	}
	@LuaWhitelist
	@LuaMethodDoc("java.wrap_primitive")
	public InstanceAPI<Short> wrapShort(LuaValue value) {
		if(!this.isHost) throw new LuaError("You can't use JavaAPI outside of host environment");
		value.checknumber();
		return wrap(value.toshort());
	}
	@LuaWhitelist
	@LuaMethodDoc("java.wrap_primitive")
	public InstanceAPI<Long> wrapLong(LuaValue value) {
		if(!this.isHost) throw new LuaError("You can't use JavaAPI outside of host environment");
		value.checknumber();
		return wrap(value.tolong());
	}
	@LuaWhitelist
	@LuaMethodDoc("java.wrap_primitive")
	public InstanceAPI<Character> wrapChar(LuaValue value) {
		if(!this.isHost) throw new LuaError("You can't use JavaAPI outside of host environment");
		value.checknumber();
		return wrap(value.tochar());
	}
	@LuaWhitelist
	@LuaMethodDoc("java.wrap_primitive")
	public <T> InstanceAPI<T> wrap(T value) {
		if(!this.isHost) throw new LuaError("You can't use JavaAPI outside of host environment");
		return new InstanceAPI<>(value);
	}

	public static <T> LuaFunction wrapCallable(ClassAPI<T>[] argTypes, Function<T[], ?> callable) {
		return new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				final var argvals = juice(args);
				final var argary = mapn(argTypes, (type, i) -> type.check(argvals[i]));
				return new LuaUserdata(callable.apply(argary));
			}
		};
	}

	public static <T> LuaFunction wrapCallable(ClassAPI<T>[] argTypes, String name, ReflectFunction<T, ?> callable) {
		return new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				final var argVals = juice(args);
				final var argAry = mapn(argTypes, (type, i) -> type.check(argVals[i]));
				try {
					return new LuaUserdata(callable.invoke(argAry));
				} catch (IllegalAccessException e) {
					throw new LuaError("Cannot access " + name);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("Invocation of %s failed".formatted(name), e);
				}
			}
		};
	}

	static LuaValue[] juice(Varargs args) {
		final var values = new LuaValue[args.narg()];
		for (int i = 1; i <= args.narg(); i++) {
			values[i] = args.arg(i+1);
		}
		return values;
	}

	@FunctionalInterface
	interface ReflectFunction<T, R> {
		@SuppressWarnings("unchecked")
		R invoke(T... args) throws IllegalAccessException, InvocationTargetException;
	}

	public String toString() {
		return "JavaAPI";
	}
}
