package org.figuramc.figura.lua.api;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaTypeDoc;



@LuaWhitelist
@LuaTypeDoc(
        name = "ExturaAPI",
        value = "extura"
)
public class FiguraMathAPI {
    @LuaWhitelist
    public static Float clamp(Float val,Float min,Float max) {  // Faster one from Auria, Still completely figura compatible
        return (min > max ? max : (val > max ? max : (val < min ? min : val)));
    }
	@LuaWhitelist
    public static Float lerp(Float a,Float b,Float t) {
		return a + (b - a) * t;
	}
	@LuaWhitelist
    public static Float shortAngle(Float a,Float b) {
		float x = (a - b) % 360;
		return x - ((2 * x) % 360);
	}
	@LuaWhitelist
    public static Float lerpAngle(Float a,Float b,Float t){
		float x = (a - b) % 360;
		return (a + (x - ((2 * x) % 360) * t) % 360);
	}
	@LuaWhitelist
    public static Integer sign(Float x) {
		return (x == 0 ? 0 : (x > 0 ? 1 : -1));
	}
	@LuaWhitelist
    public static Integer round(Float x) {
		return Math.round(x);
	}
	@LuaWhitelist
    public static Float map(Float value, Float min1, Float max1, Float min2, Float max2) {
        return (value - min1) / (max1 - min1) * (max2 - min2) + min2;
    }
    public FiguraMathAPI() {}
}
