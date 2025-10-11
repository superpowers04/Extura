package org.figuramc.figura.animation;

import com.mojang.datafixers.util.Pair;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@LuaWhitelist
@LuaTypeDoc(name = "Keyframe",value = "keyframe")
public class Keyframe implements Comparable<Keyframe> {
    /**
     * <p>
     * wannabe <code>Either&lt;Double, LuaValue&gt;</code>
     * (because we have our own {@link org.figuramc.figura.server.utils.Either}, but it's in the server code so we can't use it)
     * </p>
     *
     * <p>(the presence or absence of {@link KeyframeValue#function} implies which value is the "real" one)</p>
     * <p>{@link KeyframeValue#chunkName} is also stored in here so that we don't have to dig it out of the function</p>
     */
    public record KeyframeValue(double literal, LuaValue function, String chunkName) {
        public static KeyframeValue literal(double literal) {
            return new KeyframeValue(literal, null, null);
        }

        public static KeyframeValue function(LuaValue function, String chunkName) {
            return new KeyframeValue(0.0, function, chunkName);
        }
    }

    private final Avatar owner;
    private final Animation animation;
    private final float time;
    private final Interpolation interpolation;
    private final FiguraVec3 targetA, targetB;
    private final String[] aCode, bCode;
    private final FiguraVec3 bezierLeft, bezierRight;
    private final FiguraVec3 bezierLeftTime, bezierRightTime;

    private final KeyframeValue[] aCache = {null, null, null};
    private final KeyframeValue[] bCache = {null, null, null};

    public Keyframe(Avatar owner,
                    Animation animation,
                    float time,
                    Interpolation interpolation,
                    Pair<FiguraVec3, String[]> a,
                    Pair<FiguraVec3, String[]> b,
                    FiguraVec3 bezierLeft,
                    FiguraVec3 bezierRight,
                    FiguraVec3 bezierLeftTime,
                    FiguraVec3 bezierRightTime) {
        this.owner = owner;
        this.animation = animation;
        this.time = time;
        this.interpolation = interpolation;
        this.targetA = a.getFirst();
        this.targetB = b.getFirst();
        this.aCode = a.getSecond();
        this.bCode = b.getSecond();
        this.bezierLeft = bezierLeft;
        this.bezierRight = bezierRight;
        this.bezierLeftTime = bezierLeftTime;
        this.bezierRightTime = bezierRightTime;
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_target_a")
    public FiguraVec3 getTargetA(float delta) {
        return targetA != null ? targetA.copy() : FiguraVec3.of(getA(0, delta), getA(1, delta), getA(2, delta));
    }

    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_target_a")
    public FiguraVec3 getTargetB(float delta) {
        return targetB != null ? targetB.copy() : FiguraVec3.of(getB(0, delta), getB(1, delta), getB(2, delta));
    }

    /**
     * Get a {@link KeyframeValue} for the provided source and Lua chunk name.
     *
     * @param source    source code or double literal
     * @param chunkName chunk name for debugging in case of errors
     * @return KeyframeValue if successful, or null if not (usually because the avatar isn't ready yet)
     */
    private KeyframeValue compile(String source, String chunkName) {
        try {
            try {
                return KeyframeValue.literal(Double.parseDouble(source));
            } catch (NumberFormatException fail) {
                try {
                    // Okay, so it's Lua. Try as an expression first...
                    LuaValue chunk = owner.loadScript(chunkName, "return " + source);
                    if (chunk == null) return null;
                    return KeyframeValue.function(chunk, chunkName);
                } catch (LuaError e) { /* chunk compile failed (probably syntax) */
                    // Try as a statement.
                    LuaValue chunk = owner.loadScript(chunkName, source);
                    if (chunk == null) return null;
                    return KeyframeValue.function(chunk, chunkName);
                }
            }
        } catch (Exception e3) {
            // generic failure
            if (owner.luaRuntime != null) {
                owner.luaRuntime.error(e3);
            }
            return null;
        }
    }

    private double evaluate(KeyframeValue k, float delta) {
        if (k.function == null) return k.literal;
        Varargs v = owner.run(k.function, owner.animation, delta, animation);
        if (v == null)
            throw new LuaError(String.format(
                    "Tried to run animation with code [%s] without a functioning avatar runtime",
                    k.chunkName
            ));
        if (!v.isnumber(1)) throw new LuaError(String.format(
                "Failed to parse data from [%s]: expected number, but got %s",
                k.chunkName, v.arg1().typename()
        ));
        return v.todouble(1);
    }

    private double evalCompile(KeyframeValue[] cache, String[] code, int idx, float delta) {
        try {
            if (cache[idx] != null) return evaluate(cache[idx], delta);
            cache[idx] = compile(code[idx], generateChunkName(idx));
            if (cache[idx] != null) return evaluate(cache[idx], delta);
            throw new LuaError("Can't compile keyframe [" + generateChunkName(idx) + "] (code: " + code[idx] + ")");
        } catch (Exception e) {
            if (owner.luaRuntime != null) {
                owner.luaRuntime.error(e);
            }
            return 0; // fallback
        }
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_a")
    public double getA(int idx, float delta) {
        return evalCompile(aCache, aCode, idx, delta);
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_b")
    public double getB(int idx, float delta) {
        return evalCompile(bCache, bCode, idx, delta);
    }

    private String generateChunkName(int idx) {
        StringBuilder b = new StringBuilder();
        // note: scripts rely on the animation name followed by "keyframe" being at the start
        b.append(animation.getName())
                .append(" keyframe (")
                .append(time)
                .append("s, ");

        switch (idx) {
            case 0:
                b.append("X");
                break;
            case 1:
                b.append("Y");
                break;
            case 2:
                b.append("Z");
                break;
            default:
                b.append("?");
                break;
        }
        b.append(")");
        return b.toString();
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_time")
    public float getTime() {
        return time;
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_interpolation")
    public Interpolation getInterpolation() {
        return interpolation;
    }

    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_bezier_left")
    public FiguraVec3 getBezierLeft() {
        return bezierLeft.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_bezier_right")
    public FiguraVec3 getBezierRight() {
        return bezierRight.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_bezier_left_time")
    public FiguraVec3 getBezierLeftTime() {
        return bezierLeftTime.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_bezier_right_time")
    public FiguraVec3 getBezierRightTime() {
        return bezierRightTime.copy();
    }

    @Override
    @LuaWhitelist
    public int compareTo(Keyframe other) {
        return Float.compare(this.getTime(), other.getTime());
    }
}
