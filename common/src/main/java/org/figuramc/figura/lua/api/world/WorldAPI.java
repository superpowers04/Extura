package org.figuramc.figura.lua.api.world;

import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.levelgen.Heightmap;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.entity.PlayerAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.NoiseGenerator;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;

import java.util.*;

@LuaWhitelist
@LuaTypeDoc(
        name = "WorldAPI",
        value = "world"
)
public class WorldAPI {

    public static final WorldAPI INSTANCE = new WorldAPI();

    public static Level getCurrentWorld() {
        return Minecraft.getInstance().level;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_biome"
    )
    public static BiomeAPI getBiome(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getBiome", x, y, z);
        return new BiomeAPI(getCurrentWorld().getBiome(pos.asBlockPos()).value(), pos.asBlockPos());
    }

    @SuppressWarnings("deprecation")
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_block_state"
    )
    public static BlockStateAPI getBlockState(Object x, Double y, Double z) {
        BlockPos blockPos = LuaUtils.parseVec3("getBlockState", x, y, z).asBlockPos();
        Level world = getCurrentWorld();
        return new BlockStateAPI((world.hasChunkAt(blockPos) ? world.getBlockState(blockPos) : Blocks.VOID_AIR.defaultBlockState()), blockPos);
    }

    @SuppressWarnings("deprecation")
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"min", "max"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, FiguraVec3.class},
                            argumentNames = {"minX", "minY", "minZ", "max"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, Double.class, Double.class, Double.class},
                            argumentNames = {"min", "maxX", "maxY", "maxZ"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"minX", "minY", "minZ", "maxX", "maxY", "maxZ"}
                    )
            },
            value = "world.get_blocks"
    )
    public static List<BlockStateAPI> getBlocks(Object x, Object y, Double z, Double w, Double t, Double h) {
        Pair<FiguraVec3, FiguraVec3> pair = LuaUtils.parse2Vec3("getBlocks", x, y, z, w, t, h, 1);
        List<BlockStateAPI> list = new ArrayList<>();

        BlockPos min = pair.getFirst().asBlockPos();
        BlockPos max = pair.getSecond().asBlockPos();
        if(!Configs.GETBLOCKS_LIMIT.value){
            max = new BlockPos(
                    Math.min(min.getX() + 8, max.getX()),
                    Math.min(min.getY() + 8, max.getY()),
                    Math.min(min.getZ() + 8, max.getZ())
            );
        }

        Level world = getCurrentWorld();
        if (!world.hasChunksAt(min, max))
            return list;

        BlockPos.betweenClosedStream(min, max).forEach(blockPos -> {
            BlockPos pos = new BlockPos(blockPos);
            list.add(new BlockStateAPI(world.getBlockState(pos), pos));
        });
        return list;
    }
    @SuppressWarnings("deprecation")
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.isChunkLoaded"
    )
    public static Boolean isChunkLoaded(Object x, Double y, Double z) {
        return getCurrentWorld().hasChunkAt(LuaUtils.parseVec3("isChunkLoaded", x, y, z).asBlockPos());
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_redstone_power"
    )
    public static int getRedstonePower(Object x, Double y, Double z) {
        BlockPos blockPos = LuaUtils.parseVec3("getRedstonePower", x, y, z).asBlockPos();
        Level World = getCurrentWorld();
        return (World.getChunkAt(blockPos) == null) ? 0 : World.getBestNeighborSignal(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_strong_redstone_power"
    )
    public static int getStrongRedstonePower(Object x, Double y, Double z) {
        BlockPos blockPos = LuaUtils.parseVec3("getStrongRedstonePower", x, y, z).asBlockPos();
        Level World = getCurrentWorld();
        return (World.getChunkAt(blockPos) == null) ? 0 : World.getDirectSignalTo(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_time"
    )
    public static double getTime(double delta) {
        return getCurrentWorld().getGameTime() + delta;
    }

    @LuaWhitelist
    @LuaMethodDoc(
        overloads = {
            @LuaMethodOverload,
            @LuaMethodOverload(
                argumentTypes = Double.class,
                argumentNames = "delta"
            )
        },
        value = "world.get_time_of_day"
    )
    public static double getTimeOfDay(double delta) {
        return getCurrentWorld().getDayTime() + delta;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_day_time"
    )
    public static double getDayTime(double delta) {
        return (getCurrentWorld().getDayTime() + delta) % 24000;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_day"
    )
    public static double getDay(double delta) {
        return Math.floor((getCurrentWorld().getDayTime() + delta) / 24000);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload,
            value = "world.get_moon_phase"
    )
    public static int getMoonPhase() {
        return getCurrentWorld().getMoonPhase();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_rain_gradient"
    )
    public static double getRainGradient(Float delta) {
        return getCurrentWorld().getRainLevel((delta == null) ? 1f : delta );
    }

    @LuaWhitelist
    @LuaMethodDoc("world.is_thundering")
    public static boolean isThundering() {
        return getCurrentWorld().isThundering();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_light_level"
    )
    public static Integer getLightLevel(Object x, Double y, Double z) {
        BlockPos blockPos = LuaUtils.parseVec3("getLightLevel", x, y, z).asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null) return null;
        world.updateSkyBrightness();
        return world.getLightEngine().getRawBrightness(blockPos, world.getSkyDarken());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_sky_light_level"
    )
    public static Integer getSkyLightLevel(Object x, Double y, Double z) {
        BlockPos blockPos = LuaUtils.parseVec3("getSkyLightLevel", x, y, z).asBlockPos();
        Level world = getCurrentWorld();
        return ((world.getChunkAt(blockPos) == null) ? null : world.getBrightness(LightLayer.SKY, blockPos));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.get_block_light_level"
    )
    public static Integer getBlockLightLevel(Object x, Double y, Double z) {
        BlockPos blockPos = LuaUtils.parseVec3("getBlockLightLevel", x, y, z).asBlockPos();
        Level world = getCurrentWorld();
        return (world.getChunkAt(blockPos) == null ? null : world.getBrightness(LightLayer.BLOCK, blockPos));
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec2.class, String.class},
                            argumentNames = {"pos", "heightmap"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, String.class},
                            argumentNames = {"x", "z", "heightmap"}
                    )
            },
            value = "world.get_height"
    )
    public static Integer getHeight(Object x, Double z, String heightmap) {
        FiguraVec2 pos = LuaUtils.parseVec2("getHeight", x, z);
        Level world = getCurrentWorld();

        BlockPos blockPos = new BlockPos((int) pos.x(), 0, (int) pos.y());
        if (world.getChunkAt(blockPos) == null)
            return null;

        Heightmap.Types heightmapType;
        try {
            heightmapType = heightmap != null ? Heightmap.Types.valueOf(heightmap.toUpperCase(Locale.US)) : Heightmap.Types.MOTION_BLOCKING;
        } catch (IllegalArgumentException e) {
            throw new LuaError("Invalid heightmap type provided");
        }

        return world.getHeight(heightmapType, (int) pos.x(), (int) pos.y());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "world.is_open_sky"
    )
    public static Boolean isOpenSky(Object x, Double y, Double z) {
        BlockPos blockPos = LuaUtils.parseVec3("isOpenSky", x, y, z).asBlockPos();
        Level world = getCurrentWorld();

        return (world.getChunkAt(blockPos) == null ? null : world.canSeeSky(blockPos));
    }

    @LuaWhitelist
    @LuaMethodDoc("world.get_dimension")
    public static String getDimension() {
        return getCurrentWorld().dimension().location().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc("world.get_players")
    public static Map<String, EntityAPI<?>> getPlayers() {
        HashMap<String, EntityAPI<?>> playerList = new HashMap<>();
        for (Player player : getCurrentWorld().players())
            playerList.put(player.getName().getString(), PlayerAPI.wrap(player));
        return playerList;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, FiguraVec3.class},
                            argumentNames = {"half_range", "pos"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Double.class, Double.class, Double.class},
                            argumentNames = {"half_range", "x", "y", "z"}
                    )
            },
            value = "world.get_nearby_entities"
    )
    public static Map<String, EntityAPI<?>> getNearbyEntities(Integer range, Object x, Double y, Double z) {
        var pos = LuaUtils.parseVec3("getNearbyEntities", x, y, z).asVec3();
        HashMap<String, EntityAPI<?>> entityList = new HashMap<>();

        AABB area = new AABB(pos.subtract(range, range, range), pos.add(range, range, range));
        for (Entity entity : getCurrentWorld().getEntitiesOfClass(Entity.class, area)) {
            entityList.put(entity.getUUID().toString(), EntityAPI.wrap(entity));
        }
        return entityList;
    }


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "UUID"
            ),
            value = "world.get_entity"
    )
    public static EntityAPI<?> getEntity(@LuaNotNil String uuid) {
        try {
            return EntityAPI.wrap(EntityUtils.getEntityByUUID(UUID.fromString(uuid)));
        } catch (Exception ignored) {
            throw new LuaError("Invalid UUID");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                    argumentTypes = {Boolean.class, FiguraVec3.class, FiguraVec3.class},
                    argumentNames = {"fluid", "start", "end"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Boolean.class, Double.class, Double.class, Double.class, FiguraVec3.class},
                            argumentNames = {"fluid", "startX", "startY", "startZ", "end"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Boolean.class, FiguraVec3.class, Double.class, Double.class, Double.class},
                            argumentNames = {"fluid", "start", "endX", "endY", "endZ"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Boolean.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"fluid", "startX", "startY", "startZ", "endX", "endY", "endZ"}
                    )
                }
            ,
            value = "world.raycast_block"
    )
    public HashMap<String, Object> raycastBlock(boolean fluid, Object x, Object y, Double z, Object w, Double t, Double h) {
        FiguraVec3 start, end;

        Pair<FiguraVec3, FiguraVec3> pair = LuaUtils.parse2Vec3("raycastBlock", x, y, z, w, t, h,1);
        start = pair.getFirst();
        end = pair.getSecond();

        BlockHitResult result = getCurrentWorld().clip(new ClipContext(start.asVec3(), end.asVec3(), ClipContext.Block.OUTLINE, fluid ? ClipContext.Fluid.NONE : ClipContext.Fluid.ANY, new Marker(EntityType.MARKER, getCurrentWorld())));
        if (result == null || result.getType() == HitResult.Type.MISS)
            return null;

        HashMap<String, Object> map = new HashMap<>();
        BlockPos pos = result.getBlockPos();
        map.put("block", getBlockState(pos.getX(), (double) pos.getY(), (double) pos.getZ()));
        map.put("direction", result.getDirection().getName());
        map.put("pos", FiguraVec3.fromVec3(result.getLocation()));

        return map;
    }
    @LuaWhitelist
    @LuaMethodDoc(
            value = "world.linetrace_block"
    )
    public HashMap<String, Object> linetraceBlock(boolean fluid, Object x, Object y, Double z, Object w, Double t, Double h) {
        return raycastBlock(fluid, x, y, z, w, t, h);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"start", "end"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, FiguraVec3.class},
                            argumentNames = {"startX", "startY", "startZ", "end"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, Double.class, Double.class, Double.class},
                            argumentNames = {"start", "endX", "endY", "endZ"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"startX", "startY", "startZ", "endX", "endY", "endZ"}
                    )
            }
            ,
            value = "world.raycast_entity"
    )
    public HashMap<String, Object> raycastEntity(Object x, Object y, Double z, Object w, Double t, Double h) {
        FiguraVec3 start, end;

        Pair<FiguraVec3, FiguraVec3> pair = LuaUtils.parse2Vec3("raycastEntity", x, y, z, w, t, h, 1);
        start = pair.getFirst();
        end = pair.getSecond();

        EntityHitResult result = ProjectileUtil.getEntityHitResult(new Marker(EntityType.MARKER, getCurrentWorld()), start.asVec3(), end.asVec3(), new AABB(start.asVec3(), end.asVec3()), entity -> true, Double.MAX_VALUE);

        if (result == null)
            return null;

        HashMap<String, Object> map = new HashMap<>();
        map.put("entity", EntityAPI.wrap(result.getEntity()));
        map.put("pos", FiguraVec3.fromVec3(result.getLocation()));

        return map;
    }
    @LuaWhitelist
    @LuaMethodDoc(
            value = "world.linetrace_entity"
    )
    public HashMap<String, Object> linetraceEntity(Object x, Object y, Double z, Object w, Double t, Double h) {
        return raycastEntity(x, y, z, w, t, h);
    }
    @LuaWhitelist
    @LuaMethodDoc("world.avatar_vars")
    public static Map<String, LuaTable> avatarVars() {
        HashMap<String, LuaTable> varList = new HashMap<>();
        for (Avatar avatar : AvatarManager.getLoadedAvatars()) {
            LuaTable tbl = avatar.luaRuntime == null ? new LuaTable() : avatar.luaRuntime.avatar_meta.storedStuff;
            varList.put(avatar.owner.toString(), new ReadOnlyLuaTable(tbl));
        }
        return varList;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "block"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraVec3.class},
                            argumentNames = {"block", "pos"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class},
                            argumentNames = {"block", "x", "y", "z"}
                    )
            },
            value = "world.new_block"
    )
    public static BlockStateAPI newBlock(@LuaNotNil String string, Object x, Double y, Double z) {
        BlockPos pos = LuaUtils.parseVec3("newBlock", x, y, z).asBlockPos();
        try {
            Level level = getCurrentWorld();
            BlockState block = BlockStateArgument.block(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).getState();
            return new BlockStateAPI(block, pos);
        } catch (Exception e) {
            throw new LuaError("Could not parse block state from string: " + string);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {BlockStateAPI.class, FiguraVec3.class},
                            argumentNames = {"block", "pos"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {BlockStateAPI.class, Double.class, Double.class, Double.class},
                            argumentNames = {"block", "x", "y", "z"}
                    )
            },
            value = "world.set_block"
    )
    public static Boolean setBlock(@LuaNotNil String string, Object x, Double y, Double z) {
        BlockPos pos = LuaUtils.parseVec3("setBlock", x, y, z).asBlockPos();
        try {
            Level level = getCurrentWorld();
            BlockState block = BlockStateArgument.block(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).getState();

            level.setBlockAndUpdate(pos,block);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // @LuaWhitelist
    // @LuaMethodDoc(
    //         overloads = {
    //                 @LuaMethodOverload(
    //                         argumentTypes = {BlockStateAPI.class, FiguraVec3.class},
    //                         argumentNames = {"block", "pos"}
    //                 ),
    //                 @LuaMethodOverload(
    //                         argumentTypes = {BlockStateAPI.class, Double.class, Double.class, Double.class},
    //                         argumentNames = {"block", "x", "y", "z"}
    //                 )
    //         },
    //         value = "world.send_block"
    // )
    // public static Boolean sendBlock(@LuaNotNil String string, Object x, Double y, Double z) {
    //     BlockPos pos = LuaUtils.parseVec3("setBlock", x, y, z).asBlockPos();
    //     try {
    //         Level level = getCurrentWorld();
    //         if(!level.hasChunkAt(pos)) return false;
    //         BlockState block = BlockStateArgument.block(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).getState();
	// 		BlockState oldBlock = level.getBlockState(pos);
    //         level.setBlockAndUpdate(pos,block);
    //         level.syncBlockState(pos,block,pos);
    //         return true;
    //     } catch (Exception e) {
    //         return false;
    //     }
    // }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {Long.class, FiguraVec3.class, Boolean.class},
                            argumentNames = {"seed", "pos", "isSmooth"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Long.class, Double.class, Double.class, Double.class, Boolean.class},
                            argumentNames = {"seed", "xPos", "yPos", "zPos", "isSmooth"}
                    ),
            },
            value = "world.get_noise"
    )
    public static Double getNoise(Long seed, Object x, Double y, Double z, Boolean isSmooth) {
        FiguraVec3 pos = LuaUtils.parseVec3("getNoise", x, y, z);
        NoiseGenerator noise = new NoiseGenerator();
        noise.setSeed(seed);
        if (isSmooth) return noise.smoothNoise(pos.x, pos.y, pos.z);
        else { return noise.noise(pos.x,pos.y,pos.z); }
    }


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "item"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Integer.class},
                            argumentNames = {"item", "count"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Integer.class, Integer.class},
                            argumentNames = {"item", "count", "damage"}
                    )
            },
            value = "world.new_item"
    )
    public static ItemStackAPI newItem(@LuaNotNil String string, Integer count, Integer damage) {
        try {
            Level level = getCurrentWorld();
            ItemStack item = ItemArgument.item(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).createItemStack(1, false);
            if (count != null)
                item.setCount(count);
            if (damage != null)
                item.setDamageValue(damage);
            return new ItemStackAPI(item);
        } catch (Exception e) {
            throw new LuaError("Could not parse item stack from string: " + string);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("world.exists")
    public static boolean exists() {
        return getCurrentWorld() != null;
    }

    @LuaWhitelist
    @LuaMethodDoc("world.get_build_height")
    public static int[] getBuildHeight() {
        Level world = getCurrentWorld();
        return new int[]{world.getMinBuildHeight(), world.getMaxBuildHeight()};
    }

    @LuaWhitelist
    @LuaMethodDoc("world.get_spawn_point")
    public static FiguraVec3 getSpawnPoint() {
        Level world = getCurrentWorld();
        return FiguraVec3.fromBlockPos(world.getSharedSpawnPos());
    }

    @Override
    public String toString() {
        return "WorldAPI";
    }
}
