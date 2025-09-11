package org.figuramc.figura.lua.api;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;

@LuaWhitelist
public interface MutablePart<R extends MutablePart<R>> {
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            aliases = "visible",
            value = "model_part.set_visible"
    )
    R setVisible(boolean bool);

    @LuaWhitelist
    default R visible(boolean bool) { return setVisible(bool); }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "renderType"
            ),
            aliases = "primaryRenderType",
            value = "model_part.set_primary_render_type"
    )
    R setPrimaryRenderType(String type);

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "renderType"
            ),
            aliases = "secondaryRenderType",
            value = "model_part.set_secondary_render_type"
    )
    R setSecondaryRenderType(String type);

    @LuaWhitelist
    default R primaryRenderType(String type) { return setPrimaryRenderType(type); }

    @LuaWhitelist
    default R secondaryRenderType(String type) { return setSecondaryRenderType(type); }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "textureType"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"resource", "path"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraTexture.class},
                            argumentNames = {"custom", "texture"}
                    )
            },
            aliases = "primaryTexture",
            value = "model_part.set_primary_texture"
    )
    R setPrimaryTexture(String type, Object x);

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "textureType"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"resource", "path"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraTexture.class},
                            argumentNames = {"custom", "texture"}
                    )
            },
            aliases = "secondaryTexture",
            value = "model_part.set_secondary_texture"
    )
    R setSecondaryTexture(String type, Object x);

    @LuaWhitelist
    default R primaryTexture(String type, Object x) { return setPrimaryTexture(type, x); }

    @LuaWhitelist
    default R secondaryTexture(String type, Object x) { return setSecondaryTexture(type, x); }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            aliases = "color",
            value = "model_part.set_color")
    R setColor(Object r, Double g, Double b);

    @LuaWhitelist
    default R color(Object r, Double g, Double b) { return setColor(r, g, b); }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            aliases = "primaryColor",
            value = "model_part.set_primary_color")
    R setPrimaryColor(Object r, Double g, Double b);

    @LuaWhitelist
    default R primaryColor(Object r, Double g, Double b) { return setPrimaryColor(r, g, b); }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            aliases = "secondaryColor",
            value = "model_part.set_secondary_color")
    R setSecondaryColor(Object r, Double g, Double b);

    @LuaWhitelist
    default R secondaryColor(Object r, Double g, Double b) { return setSecondaryColor(r, g, b); }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "opacity"
            ),
            aliases = "opacity",
            value = "model_part.set_opacity")
    R setOpacity(Float opacity);

    @LuaWhitelist
    default R opacity(Float opacity) { return setOpacity(opacity); } // repeat for the others

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "light"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"blockLight", "skyLight"}
                    )
            },
            aliases = "light",
            value = "model_part.set_light")
    R setLight(Object light, Double skyLight);

    @LuaWhitelist
    default R light(Object light, Double skyLight) { return setLight(light, skyLight); }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "overlay"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"whiteOverlay", "hurtOverlay"}
                    )
            },
            aliases = "overlay",
            value = "model_part.set_overlay")
    R setOverlay(Object whiteOverlay, Double hurtOverlay);

    @LuaWhitelist
    default R overlay(Object whiteOverlay, Double hurtOverlay) { return setOverlay(whiteOverlay, hurtOverlay); }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "parentType"
            ),
            aliases = "parentType",
            value = "model_part.set_parent_type")
    R setParentType(String parent);

    @LuaWhitelist
    default R parentType(String parent) { return setParentType(parent); }
}
