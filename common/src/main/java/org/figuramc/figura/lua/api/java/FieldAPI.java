package org.figuramc.figura.lua.api.java;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Function;

@LuaWhitelist
@LuaTypeDoc(name = "FieldWrapper", value = "java")
public class FieldAPI<T, F> {
    private final ClassAPI<T> klass;
    final Field field;
    @LuaWhitelist
    @LuaFieldDoc("java.field.is_static")
    final boolean isStatic;

    FieldAPI(ClassAPI<T> klass, Field field) {
        this.klass = klass;
        this.field = field;
        isStatic = Modifier.isStatic(field.getModifiers());
    }

    @LuaWhitelist
    @LuaMethodDoc("java.field.to_class")
    public ClassAPI<T> toClass() {
        return klass;
    }

    public static <T> Function<Field, FieldAPI<T, ?>> factory(ClassAPI<T> klass) {
        return field -> new FieldAPI<>(klass, field);
    }

    @LuaWhitelist
    @LuaMethodDoc(
        overloads = {
            @LuaMethodOverload(
                argumentNames = "receiver",
                argumentTypes = InstanceAPI.class
            ),
            @LuaMethodOverload()
        },
        value = "java.field.get"
    )
    F get(InstanceAPI<T> receiver) {
        receiver.check((Class<T>) field.getDeclaringClass());
        try {
            return (F) field.get(receiver.value);
        } catch (IllegalAccessException e) {
            throw new LuaError("Cannot read field %s".formatted(this));
        }
    }

    @LuaWhitelist
    F get() {
        if (isStatic) {
            return get(null);
        } else {
            throw new LuaError("Field %s is not static and requires a receiver to read".formatted(this));
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
        overloads = {
            @LuaMethodOverload(
                argumentNames = "value",
                argumentTypes = InstanceAPI.class
            ),
            @LuaMethodOverload(
                argumentNames = {"receiver", "value"},
                argumentTypes = {Object.class, Object.class}
            )
        },
        value = "java.field.get"
    )
    void set(T receiver, F value) {
        try {
            field.set(receiver, value);
        } catch (IllegalAccessException e) {
            throw new LuaError("Cannot write field %s::%s".formatted(this, field.getName()));
        } catch (ClassCastException e) {
            if (value != null) {
                throw new LuaError("Field set mismatch: field %s is of type %s, got %s".formatted(this, field.getType().getSimpleName(), value.getClass().getSimpleName()));
            } else {
                throw new AssertionError("nulls cannot throw ClassCastExceptions");
            }
        }
    }

    @LuaWhitelist
    void set(F value) {
        if (isStatic) {
            set(null, value);
        } else {
            throw new LuaError("Field %s is not static and requires a receiver to write".formatted(this));
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("java.field.get_type")
    public ClassAPI<F> getType() {
        return (ClassAPI<F>) new ClassAPI<>(field.getType());
    }

    public String toString() {
        return "%s::%s".formatted(field.getDeclaringClass().getSimpleName(), field.getName());
    }
}
