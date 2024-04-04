package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Function;

@LuaWhitelist
@LuaTypeDoc(value = "future", name = "Future")
public class FiguraFuture<T> {
    private final Avatar avatar;
    private boolean isDone;
    private boolean hasError;
    private LuaError errorObject;
    private final Deque<Consumer<T>> onFinish = new ArrayDeque<>();
    private final Deque<Consumer<LuaError>> onFinishError = new ArrayDeque<>();
    private T value;

    public FiguraFuture(Avatar avatar) {
        this.avatar = avatar;
    }

    public void handle(T value, Throwable error) {
        if (error != null) error(error);
        else complete(value);
    }

    public void complete(T value) {
        if (!isDone) {
            this.value = value;
            isDone = true;
            for (var f: onFinish) {
                f.accept(value);
            }
            onFinish.clear();
            onFinishError.clear();
        }
    }

    public AutoCloseable onFinish(Consumer<T> f) {
            if (isDone && !hasError) {
                f.accept(value);
                return () -> {};
            } else {
                onFinish.add(f);
                return () -> onFinish.remove(f);
            }
    }
    @LuaWhitelist
    public LuaCloseable onFinish(LuaFunction f) {
        if (avatar == null) {
            throw new LuaError("Future.onFinish unavailable for legal reasons");
        } else {
            final var mgr = avatar.luaRuntime.typeManager;
            return new LuaCloseable(onFinish(value -> f.invoke(mgr.javaToLua(f))));
        }
    }

    @LuaWhitelist
    public LuaCloseable onFinishError(LuaFunction f) {
        return new LuaCloseable(onFinishError(err -> f.invoke(err.getMessageObject())));
    }

    public AutoCloseable onFinishError(Consumer<LuaError> f) {
        if (hasError) {
            f.accept(errorObject);
            return () -> {};
        } else {
            onFinishError.add(f);
            return () -> onFinishError.remove(f);
        }
    }

    public void error(Throwable t) {
        if (!isDone) {
            hasError = true;
            isDone = true;
            errorObject = t instanceof LuaError e ? e : new LuaError(t);
            for (var f: onFinishError) {
                f.accept(errorObject);
            }
            onFinish.clear();
            onFinishError.clear();
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "future.is_done",
            overloads = @LuaMethodOverload(
                    returnType = Boolean.class
            )
    )
    public boolean isDone() {
        return isDone;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "future.has_error",
            overloads = @LuaMethodOverload(
                    returnType = Boolean.class
            )
    )
    public boolean hasError() {
        return hasError;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "future.get_value",
            overloads = @LuaMethodOverload(
                    returnType = Object.class
            )
    )
    public T getValue() {
        return value;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "future.get_or_error",
            overloads = @LuaMethodOverload(
                    returnType = Object.class
            )
    )
    public T getOrError() {
        if (errorObject != null) throw errorObject;
        return value;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "future.throw_error"
    )
    public void throwError() {
        if (errorObject != null) throw errorObject;
    }

    public <R> FiguraFuture<R> map(Function<T, R> mapper) {
        final var fut = new FiguraFuture<R>(avatar);
        onFinish(v -> fut.complete(mapper.apply(v)));
        onFinishError(fut::error);
        return fut;
    }

    @LuaWhitelist
    public FiguraFuture<LuaValue> map(LuaFunction mapper) {
        return map(wrapLua(mapper));
    }

    public <R> Function<R, LuaValue> wrapLua(LuaFunction f) {
        return a -> f.invoke(avatar.luaRuntime.typeManager.javaToLua(a)).arg1();
    }

    public FiguraFuture<T> handle(Function<LuaError, T> handler) {
        final var fut = new FiguraFuture<T>(avatar);
        onFinish(fut::complete);
        onFinishError(e -> {
            try {
                fut.complete(handler.apply(e));
            } catch (Throwable t) {
                fut.error(t);
            }
        });
        return fut;
    }

    @LuaWhitelist
    public FiguraFuture<LuaValue> handle(LuaFunction handler) {
        return map(((Function<T, Varargs>) avatar.luaRuntime.typeManager::javaToLua).andThen(Varargs::arg1)).handle(err -> handler.invoke(err.getMessageObject()).arg1());
    }

    @Override
    public String toString() {
        if (isDone) {
            if (hasError) {
                return "Future(error: " + errorObject.toString() + ")";
            } else {
                return "Future(value: " + value.toString() + ")";
            }
        } else {
            return "Future(pending)";
        }
    }
}
