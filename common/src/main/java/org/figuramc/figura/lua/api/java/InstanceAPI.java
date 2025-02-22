package org.figuramc.figura.lua.api.java;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Varargs;

@LuaWhitelist
@LuaTypeDoc(name = "InstanceAPI", value = "java")
public class InstanceAPI<T> {
    protected @NotNull T value;

    public InstanceAPI(@NotNull T value) {
        this.value = value;
    }

    @LuaWhitelist
    @LuaMethodDoc("java.instance.to_class")
    public ClassAPI<?> toClass() {
        return new ClassAPI<>(this.value.getClass());
    }

    @LuaWhitelist
    @LuaMethodDoc("java.instance.is_lua_safe")
    public boolean isLuaSafe() {
        return toClass().isLuaSafe();
    }

    public <C extends T> C check(Class<C> klass) {
        if (klass.isInstance(value)) {
            return (C) value;
        } else {
            throw new LuaError("Expected an instance of %s, got %s".formatted(klass.getSimpleName(), value.getClass().getSimpleName()));
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("java.instance.check")
    public <C extends T> C check(ClassAPI<C> klass) {
        return klass.check(this);
    }

    @LuaWhitelist
    @LuaMethodDoc("java.instance.cast")
    public <C> InstanceAPI<C> cast(ClassAPI<C> klass) {
        if (klass.has(this)) {
            // Safety: resulting class is guaranteed to
            return (InstanceAPI<C>) this;
        } else {
            return null;
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("java.instance.unwrap")
    public T unwrap() {
        if (isLuaSafe()) {
            return value;
        } else {
            return null;
        }
    }

    @LuaMethodDoc("java.instance.method")
    public <P> LuaFunction method(String name, ClassAPI<P>... classes) {
        return toClass().method(name, classes);
    }

    @LuaWhitelist
    public LuaFunction method(String name, Varargs varargs) {
        return method(name, ClassAPI.checkvarargs(varargs, ClassAPI.class));
    }

    public String toString() {
        return "%s {%s}".formatted(value.getClass().getSimpleName(), value);
    }
}
