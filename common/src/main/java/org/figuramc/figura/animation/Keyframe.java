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

    private final Avatar owner;
    private final Animation animation;
    private float time;
    private Interpolation interpolation;
    private FiguraVec3 targetA, targetB;
    private String[] aCode, bCode;
    private final String chunkName;
    private FiguraVec3 bezierLeft, bezierRight;
    private FiguraVec3 bezierLeftTime, bezierRightTime;

    public Keyframe(Avatar owner, Animation animation, float time, Interpolation interpolation, Pair<FiguraVec3, String[]> a, Pair<FiguraVec3, String[]> b, FiguraVec3 bezierLeft, FiguraVec3 bezierRight, FiguraVec3 bezierLeftTime, FiguraVec3 bezierRightTime) {
        this.owner = owner;
        this.animation = animation;
        this.time = time;
        this.interpolation = interpolation;
        this.targetA = a.getFirst();
        this.targetB = b.getFirst();
        this.aCode = a.getSecond();
        this.bCode = b.getSecond();
        this.chunkName = animation.getName() + " keyframe (" + time + "s)";
        this.bezierLeft = bezierLeft;
        this.bezierRight = bezierRight;
        this.bezierLeftTime = bezierLeftTime;
        this.bezierRightTime = bezierRightTime;
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_target_a")
    public FiguraVec3 getTargetA(float delta) {
        return targetA != null ? targetA.copy() : FiguraVec3.of(parseStringData(aCode[0], delta), parseStringData(aCode[1], delta), parseStringData(aCode[2], delta));
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_target_b")
    public FiguraVec3 getTargetB(float delta) {
        return targetB != null ? targetB.copy() : FiguraVec3.of(parseStringData(bCode[0], delta), parseStringData(bCode[1], delta), parseStringData(bCode[2], delta));
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.set_target_a")
    public Keyframe setTargetA(float delta,Object x, Double y, Double z) {
        targetA = LuaUtils.parseVec3("keyframe.setTargetA",x,y,z);
        return this;
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.set_target_b")
    public Keyframe setTargetB(float delta,Object x, Double y, Double z) {
        targetB = LuaUtils.parseVec3("keyframe.setTargetB",x,y,z);
        return this;
    }
    private float parseStringData(String data, float delta) {
        FiguraMod.pushProfiler(data);
        try {
            return FiguraMod.popReturnProfiler(Float.parseFloat(data));
        } catch (Exception ignored) {
            if (data == null)
                return FiguraMod.popReturnProfiler(0f);

            try {
                LuaValue val = owner.loadScript(chunkName, "return " + data);
                if (val == null)
                    return FiguraMod.popReturnProfiler(0f);

                Varargs args = owner.run(val, owner.animation, delta, animation);
                if (args.isnumber(1))
                    return FiguraMod.popReturnProfiler(args.tofloat(1));
                else
                    throw new Exception(); // dummy exception
            } catch (Exception ignored2) {
                try {
                    LuaValue val = owner.loadScript(chunkName, data);
                    if (val == null)
                        return FiguraMod.popReturnProfiler(0f);

                    Varargs args = owner.run(val, owner.animation, delta, animation);
                    if (args.isnumber(1))
                        return FiguraMod.popReturnProfiler(args.tofloat(1));
                    else
                        throw new LuaError("Failed to parse data from [" + this.chunkName + "], expected number, but got " + args.arg(1).typename());
                } catch (Exception e) {
                    if (owner.luaRuntime != null)
                        owner.luaRuntime.error(e);
                }
            }
        }

        return FiguraMod.popReturnProfiler(0f);
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_time")
    public float getTime() {
        return time;
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.set_time")
    public Keyframe setTime(Float newTime) {
        time = newTime;
        return this;
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.get_interpolation")
    public Interpolation getInterpolation() {
        return interpolation;
    }
    @LuaWhitelist
    @LuaMethodDoc("keyframe.set_interpolation")
    public Keyframe setInterpolation(String interp) {
        interpolation = Interpolation.valueOf(interp);
        return this;
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
    public int compareTo(Keyframe other) {
        return Float.compare(this.getTime(), other.getTime());
    }
}
