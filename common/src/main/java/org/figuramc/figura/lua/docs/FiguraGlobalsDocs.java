package org.figuramc.figura.lua.docs;

import org.figuramc.figura.lua.api.*;
import org.figuramc.figura.lua.api.action_wheel.ActionWheelAPI;
import org.figuramc.figura.lua.api.data.DataAPI;
import org.figuramc.figura.lua.api.data.ResourcesAPI;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.entity.PlayerAPI;
import org.figuramc.figura.lua.api.event.EventsAPI;
import org.figuramc.figura.lua.api.java.JavaAPI;
import org.figuramc.figura.lua.api.json.JsonAPI;
import org.figuramc.figura.lua.api.keybind.KeybindAPI;
import org.figuramc.figura.lua.api.math.MatricesAPI;
import org.figuramc.figura.lua.api.math.VectorsAPI;
import org.figuramc.figura.lua.api.nameplate.NameplateAPI;
import org.figuramc.figura.lua.api.net.NetworkingAPI;
import org.figuramc.figura.lua.api.particle.ParticleAPI;
import org.figuramc.figura.lua.api.ping.PingAPI;
import org.figuramc.figura.lua.api.sound.SoundAPI;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.lua.api.ExturaAPI;
import org.figuramc.figura.model.FiguraModelPart;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;

/**
 * Class only exists to have docs for the global figura
 * functions/fields! This class should never end up in
 * anyone's code. It's not even whitelisted!
 */
@LuaTypeDoc(
        name = "globals",
        value = "globals"
)
public abstract class FiguraGlobalsDocs {

    @LuaFieldDoc("globals.vec")
    public LuaFunction vec;
    @LuaFieldDoc("globals.action_wheel")
    public ActionWheelAPI action_wheel;
    @LuaFieldDoc("globals.animations")
    public AnimationAPI animations;
    @LuaFieldDoc("globals.figura_metatables")
    public LuaTable figuraMetatables;
    @LuaFieldDoc("globals.nameplate")
    public NameplateAPI nameplate;
    @LuaFieldDoc("globals.world")
    public WorldAPI world;
    @LuaFieldDoc("globals.vanilla_model")
    public VanillaModelAPI vanilla_model;
    @LuaFieldDoc("globals.models")
    public FiguraModelPart models;
    @LuaFieldDoc("globals.player")
    public PlayerAPI player;
    @LuaFieldDoc("globals.events")
    public EventsAPI events;
    @LuaFieldDoc("globals.keybinds")
    public KeybindAPI keybinds;
    @LuaFieldDoc("globals.vectors")
    public VectorsAPI vectors;
    @LuaFieldDoc("globals.matrices")
    public MatricesAPI matrices;
    @LuaFieldDoc("globals.client")
    public ClientAPI client;
    @LuaFieldDoc("globals.host")
    public HostAPI host;
    @LuaFieldDoc("globals.java")
    public JavaAPI java;
    @LuaFieldDoc("globals.avatar")
    public AvatarAPI avatar;
    @LuaFieldDoc("globals.particles")
    public ParticleAPI particles;
    @LuaFieldDoc("globals.sounds")
    public SoundAPI sounds;
    @LuaFieldDoc("globals.renderer")
    public RendererAPI renderer;
    @LuaFieldDoc("globals.user")
    public EntityAPI<?> user;
    @LuaFieldDoc("globals.pings")
    public PingAPI pings;
    @LuaFieldDoc("globals.textures")
    public TextureAPI textures;
    @LuaFieldDoc("globals.config")
    public ConfigAPI config;
    @LuaFieldDoc("globals.data")
    public DataAPI data;
    @LuaFieldDoc("globals.file")
    public FileAPI file;
    @LuaFieldDoc("globals.json")
    public JsonAPI json;
    @LuaFieldDoc("globals.resources")
    public ResourcesAPI resources;
    @LuaFieldDoc("globals.net")
    public NetworkingAPI net;
    @LuaFieldDoc("globals.raycast")
    public RaycastAPI raycast;
    @LuaFieldDoc("globals.server_packets")
    public ServerPacketsAPI server_packets;

    @LuaFieldDoc("globals.type")
    public LuaFunction type;

    @LuaFieldDoc("globals.extura")
    public ExturaAPI extura;

    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "scriptName"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, LuaFunction.class},
                            argumentNames = {"scriptName", "fallbackFunction"}
                    )
            },
            value = "globals.require"
    )
    public static Object require() {return null;}
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "scriptName"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"scriptName", "scriptContents"}
                    )
            },
            value = "globals.add_script"
    )
    public static Object addScript() {return null;}
    @LuaMethodDoc(
            value = "globals.get_scripts"
    )
    public static Object getScripts() {return null;}
    @LuaMethodDoc(
            value = "globals.get_script",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "scriptName"
                    )}
    )
    public static Object getScript() {return null;}

    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "folder"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Boolean.class},
                            argumentNames = {"folder", "subFolders"}
                    )
            },
            value = "globals.list_files"
    )
    public static LuaTable listFiles() {return null;}

    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Object.class,
                    argumentNames = "arg"
            ),
            aliases = "log",
            value = "globals.print"
    )
    public static String print() {
        return null;
    }

    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = LuaTable.class,
                            argumentNames = "table"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = LuaUserdata.class,
                            argumentNames = "javaObject"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {LuaTable.class, Integer.class},
                            argumentNames = {"table", "maxDepth"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {LuaUserdata.class, Integer.class},
                            argumentNames = {"javaObject", "maxDepth"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {LuaTable.class, Integer.class, Boolean.class},
                            argumentNames = {"table", "maxDepth", "silent"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {LuaUserdata.class, Integer.class, Boolean.class},
                            argumentNames = {"javaObject", "maxDepth", "silent"}
                    )
            },
            aliases = "logTable",
            value = "globals.print_table"
    )
    public static String printTable() {
        return null;
    }

    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "json"
            ),
            aliases = "logJson",
            value = "globals.print_json"
    )
    public static String printJson() {
        return null;
    }

    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = LuaValue.class,
                            argumentNames = "value"
                    )
            },
            aliases = "toJson",
            value = "globals.to_json"
    )
    public static String toJson() {return null;}

    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "jsonString"
                    )
            },
            aliases = "parseJson",
            value = "globals.parse_json"
    )
    public static LuaValue parseJson() {return null;}
}
