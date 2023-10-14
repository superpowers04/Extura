package org.figuramc.figura.lua.api.java;

import org.figuramc.figura.lua.FiguraAPIManager;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.figuramc.figura.lua.api.java.JavaAPI.juice;

@LuaWhitelist
@LuaTypeDoc(name = "ClassAPI", value = "java")
public class ClassAPI<T> {
    @NotNull
    final Class<T> klass;

    @LuaWhitelist
    @LuaMethodDoc("java.class.is_lua_safe")
    public boolean isLuaSafe() {
        return FiguraAPIManager.WHITELISTED_CLASSES.contains(klass) && klass.isAnnotationPresent(LuaWhitelist.class);
    }

    public ClassAPI(@NotNull Class<T> klass) {
        this.klass = klass;
    }

//    public LuaFunction getConstructor(String name, ClassAPI<?>... classes) {
//        return getConstructor(name, false, classes);
//    }

//    private final Sudo sudo = new Sudo();

    @LuaWhitelist
    @LuaMethodDoc("java.class.check")
    public T check(InstanceAPI<?> instance) {
        if (has(instance)) {
            return (T) instance.value;
        } else {
            throw new LuaError("Expected an instance of %s, got %s".formatted(klass, instance.toClass()));
        }
    }

    public T check(LuaValue instance) {
        return check((InstanceAPI<?>) instance.checkuserdata(InstanceAPI.class));
    }

//    @LuaWhitelist
//    public class Sudo {
//        public LuaFunction getConstructor(String name, ClassAPI<?>... classes) {
//            return ClassAPI.this.getConstructor(name, true, classes);
//        }
//    }
//    @LuaWhitelist
//    public Sudo sudo() {
//        return sudo;
//    }

    @SafeVarargs
    @LuaMethodDoc("java.class.constructor")
    public final LuaFunction constructor(ClassAPI<Object>... classes) {
        try {var classList = map(classes, api -> api.klass);
            var constructor = klass.getConstructor(classList);
            return JavaAPI.wrapCallable(classes, "constructor of " + this, args -> {
                try {
                    return constructor.newInstance(args);
                } catch (InstantiationException e) {
                    throw new LuaError("Cannot construct abstract class " + this);
                }
            });
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    @LuaWhitelist
    public final LuaFunction constructor(Varargs args) {
        return constructor(checkvarargs(args, ClassAPI.class));
    }

    public static <V> V[] checkvarargs(Varargs args, Class<V> type) {
        return (V[]) map(juice(args), val -> val.checkuserdata(type));
    }

    @LuaWhitelist
    public <P> LuaFunction constructor(ClassAPI<P> klass) {
        return constructor(new ClassAPI[] { klass });
    }

    static <T, R> R[] map(T[] in, Function<T, R> func) {
        final R[] out = (R[]) new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = func.apply(in[i]);
        }
        return out;
    }

    static <T, R> R[] mapn(T[] in, BiFunction<T, Integer, R> func) {
        final R[] out = (R[]) new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = func.apply(in[i], i);
        }
        return out;
    }

    public boolean has(InstanceAPI<?> instance) {
        return klass.isInstance(instance.value);
    }

    @LuaWhitelist
    @LuaMethodDoc("java.class.superclass")
    public ClassAPI<? super T> superclass() {
        return new ClassAPI<>(klass.getSuperclass());
    }

    @LuaWhitelist
    @LuaMethodDoc("java.class.array")
    public ClassAPI<T[]> array() {
        return (ClassAPI<T[]>) new ClassAPI<>(klass.arrayType());
    }

    @LuaWhitelist
    @LuaMethodDoc("java.class.component")
    public ClassAPI<?> component() {
        if (klass.isArray()) {
            return new ClassAPI<>(klass.getComponentType());
        } else {
            return null;
        }
    }

    @SafeVarargs
    @LuaMethodDoc("java.class.method")
    public final <P, R> LuaFunction method(String name, ClassAPI<P>... classApis) {
        final var classes = map(classApis, api -> api.klass);
        try {
            final var method = klass.getMethod(name, classes);
            return JavaAPI.wrapCallable(classApis, "method {%s}::%s".formatted(this, method.getName()), args -> (R) method.invoke(null, args));
        } catch (NoSuchMethodException ignored) {
            final var classapiary = new ArrayList<ClassAPI<?>>(classes.length + 1);
            classapiary.add(this);
            classapiary.addAll(List.of(classApis));
            try {
                final var instmethod = klass.getMethod(name, classes);
                return JavaAPI.wrapCallable((ClassAPI<P>[]) classapiary.toArray(), "method %s::%s".formatted(this, instmethod.getName()), args -> {
                    final var argary = Arrays.asList(args);
                    return instmethod.invoke(argary.remove(0), argary.toArray());
                });
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
    }

    @LuaWhitelist
    public final LuaFunction method(String name, Varargs varargs) {
        return method(name, checkvarargs(varargs, ClassAPI.class));
    }

    @LuaWhitelist
    @LuaMethodDoc("java.class.field")
    public Object field(String name) {
        try {
            return new FieldAPI<>(this, klass.getField(name));
        } catch (NoSuchFieldException ignored2) {
            return null;
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("java.class.fields")
    public FieldAPI<T, ?>[] fields() {
        final var staticFields = klass.getDeclaredFields();
        final var fields = klass.getFields();
        return (FieldAPI<T, ?>[]) Stream.concat(Arrays.stream(fields).parallel(), Arrays.stream(staticFields).parallel()).map(FieldAPI.factory(this)).toArray();
    }

    @LuaWhitelist
    public String toString() {
        return klass.getSimpleName();
    }
}