package org.figuramc.figura.parsers;

import com.google.gson.*;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import org.figuramc.figura.model.ParentType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockbenchCommonTypes {

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(BlockbenchV5Model.class, BlockbenchV5Model.MODEL_DESERIALIZER)
                .registerTypeAdapter(BlockbenchV4Model.class, BlockbenchV4Model.MODEL_DESERIALIZER)
                .registerTypeAdapter(Vector4f.class, VEC4F_DESERIALIZER)
                .registerTypeAdapter(Vector3f.class, VEC3F_DESERIALIZER)
                .registerTypeAdapter(Vector2f.class, VEC2F_DESERIALIZER)
                .registerTypeAdapter(BlockbenchV5Model.OutlinerItem.class, BlockbenchV5Model.OutlinerItem.DESERIALIZER)
                .registerTypeAdapter(
                        BlockbenchV4Model.OutlinerItem.Element.class,
                        BlockbenchV4Model.OutlinerItem.Element.DESERIALIZER
                )
                .registerTypeAdapterFactory(ModelFormat.ADAPTER_FACTORY)
                .registerTypeAdapterFactory(Element.ADAPTER_FACTORY)
                .registerTypeAdapterFactory(Keyframe.ADAPTER_FACTORY)
                .registerTypeAdapterFactory(BlockbenchV4Model.OutlinerItem.ADAPTER_FACTORY)
                .create();
    }

    public static void parseParent(String name, CompoundTag nbt) {
        ParentType parentType = ParentType.get(name);
        if (parentType != ParentType.None)
            nbt.putString("pt", parentType.name());
    }

    public static Vector2i getPNGDimensions(byte[] png) {
        // this grabs the two values from the header ._.
        // duplicate of BlockbenchModelParser.getTextureSize
        int w = (int) png[16] & 0xFF;
        w = (w << 8) + ((int) png[17] & 0xFF);
        w = (w << 8) + ((int) png[18] & 0xFF);
        w = (w << 8) + ((int) png[19] & 0xFF);

        int h = (int) png[20] & 0xFF;
        h = (h << 8) + ((int) png[21] & 0xFF);
        h = (h << 8) + ((int) png[22] & 0xFF);
        h = (h << 8) + ((int) png[23] & 0xFF);

        return new Vector2i(w, h);
    }

    public static abstract class ModelFormat {
        String formatVersion;

        public static final Pattern FORMAT_VERSION_MATCHER = Pattern.compile("^(\\d+?)\\.");

        public static final TypeAdapterFactory ADAPTER_FACTORY = new GsonTypeByField<>(
                ModelFormat.class, obj -> {
            String version = obj.getAsJsonObject().getAsJsonObject("meta").get("format_version").getAsString();
            Matcher matcher = FORMAT_VERSION_MATCHER.matcher(version);
            if (!matcher.find()) throw new IllegalStateException("No match on version number");
            return matcher.group(1);
        }
        ).bind("5", BlockbenchV5Model.class).bind("4", BlockbenchV4Model.class);
        String modelFormat;

        public abstract Map<String, UUIDReferable> getAllReferences();

        public abstract CompoundTag convert(BlockbenchParser2.Intermediary target);
    }

    public static final JsonDeserializer<Vector4f> VEC4F_DESERIALIZER = (json, typeOfT, context) -> {
        JsonArray four = json.getAsJsonArray();
        if (four.size() != 4)
            throw new JsonParseException("Trying to parse a Vector4f, but the list has " + four.size() + " items (expected 4)");
        return new Vector4f(
                four.get(0).getAsFloat(),
                four.get(1).getAsFloat(),
                four.get(2).getAsFloat(),
                four.get(3).getAsFloat()
        );
    };
    public static final JsonDeserializer<Vector3f> VEC3F_DESERIALIZER = (json, typeOfT, context) -> {
        JsonArray three = json.getAsJsonArray();
        if (three.size() != 3)
            throw new JsonParseException("Trying to parse a Vector3f, but the list has " + three.size() + " items (expected 3)");
        return new Vector3f(
                three.get(0).getAsFloat(),
                three.get(1).getAsFloat(),
                three.get(2).getAsFloat()
        );
    };
    public static final JsonDeserializer<Vector2f> VEC2F_DESERIALIZER = (json, typeOfT, context) -> {
        JsonArray two = json.getAsJsonArray();
        if (two.size() != 2)
            throw new JsonParseException("Trying to parse a Vector2f, but the list has " + two.size() + " items (expected 2)");
        return new Vector2f(
                two.get(0).getAsFloat(),
                two.get(1).getAsFloat()
        );
    };

    public static ListTag floatArrToList(float[] fa) {
        int type = 0;
        for (float c : fa) {
            if (c % 1 == 0) {
                // short?
                if (c < -127 || c >= 128) type = 1;
                // full float?
                if (c < -16383 || c >= 16384) {
                    type = 2;
                    break;
                }
            } else {
                type = 2;
                break;
            }
        }

        ListTag tag = new ListTag();

        for (float c : fa) {
            switch (type) {
                case 0 -> tag.add(ByteTag.valueOf((byte) c));
                case 1 -> tag.add(ShortTag.valueOf((short) c));
                case 2 -> tag.add(FloatTag.valueOf(c));
            }
        }
        return tag;
    }

    public static ListTag vecToList(Vector3f vector) {
        return floatArrToList(new float[]{vector.x, vector.y, vector.z});
    }

    public static ListTag vecToList(Vector4f vector) {
        return floatArrToList(new float[]{vector.x, vector.y, vector.z, vector.w});
    }

    public static float parseFloatOr(@Nullable String floatLike, float fallback) {
        // fail-fast the most common not-numbers
        if (floatLike == null || floatLike.isBlank()) return fallback;
        try {
            return Float.parseFloat(floatLike);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public interface UUIDReferable {
        String getUUID();
    }

    public interface NBTRepresentation<T extends Tag> {
        T toNBT(BlockbenchParser2.Intermediary context);
    }

    @SuppressWarnings("unused")
    public static abstract class Element implements UUIDReferable, NBTRepresentation<CompoundTag> {
        public static final TypeAdapterFactory ADAPTER_FACTORY =
                new GsonTypeByField<>(Element.class, "type")
                        .bind("cube", CubeElement.class)
                        .bind("mesh", MeshElement.class)
                        .bind("locator", PointElement.class)
                        .bind("null_object", PointElement.class)
                        .withFallback(UnknownElement.class);

        String name;
        String type;
        String uuid;

        @Nullable Vector3f rotation;
        @Nullable Vector3f origin;

        @Nullable Boolean visibility;
        @Nullable Boolean export;

        @Override
        public String getUUID() {
            return uuid;
        }

        @Override
        public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
            CompoundTag tag = new CompoundTag();

            if (Boolean.FALSE.equals(export)) return null;

            tag.putString("name", name);
            try {
                tag.putIntArray("nr", UUIDUtil.uuidToIntArray(UUID.fromString(uuid)));
            } catch (IllegalArgumentException ignored) {
                tag.putString("nr", uuid);
            }
            if (rotation != null && !rotation.equals(0, 0, 0)) {
                tag.put("rot", vecToList(rotation));
            }
            if (origin != null && !origin.equals(0, 0, 0)) {
                tag.put("piv", vecToList(origin));
            }

            // realistically this is only applicable if it's false.
            if (Boolean.FALSE.equals(visibility))
                tag.putBoolean("vsb", false);

            // TODO: collections?!
            return tag;
        }
    }

    public static class UnknownElement extends Element {
        /* idk */
    }

    public static class CubeElement extends Element {
        // type = "cube"
        Vector3f from;
        Vector3f to;

        float inflate;

        Map<String, CubeFace> faces;

        @Override
        public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
            CompoundTag tag = super.toNBT(context);
            if (tag == null) return null;

            if (!from.equals(0, 0, 0))
                tag.put("f", vecToList(from));
            if (!to.equals(0, 0, 0))
                tag.put("t", vecToList(to));
            if (inflate != 0f)
                tag.putFloat("inf", inflate);

            CompoundTag cubeData = new CompoundTag();
            for (String face : CubeFace.FACES) {
                CubeFace faceData;
                if ((faceData = faces.get(face)) != null) {
                    CompoundTag faceNBT = faceData.toNBT(context);
                    if (faceNBT != null)
                        cubeData.put(String.valueOf(face.charAt(0)), faceNBT);
                }
            }

            tag.put("cube_data", cubeData);

            return tag;
        }
    }

    public static class CubeFace implements NBTRepresentation<CompoundTag> {
        static final String[] FACES = new String[]{"north", "south", "west", "east", "up", "down"};

        Vector4f uv;
        float rotation;
        Integer texture;

        @Override
        public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
            if (texture == null)
                return null;
            CompoundTag tag = new CompoundTag();
            tag.putInt("tex", context.getTextureGlobalID(texture));
            if (rotation != 0f)
                tag.putFloat("rot", rotation);
            if (!uv.equals(0, 0, 0, 0)) {
                Vector2f size = context.getTextureFixedSize(texture);
                Vector4f sizeTwice = new Vector4f(size.x, size.y, size.x, size.y);
                // this is the order it is because otherwise we'd be mutating uv
                Vector4f corrected = sizeTwice.mul(uv);
                tag.put("uv", vecToList(corrected));
            }
            return tag;
        }
    }

    public static class MeshElement extends Element {
        /// map of face ID(?) to face
        Map<String, MeshFace> faces;
        /// map of vertex ID to offsets
        Map<String, Vector3f> vertices;

        // default "flat", alternative "smooth"
        // smooth: set 'smo' to true (note: since meshes aren't groups, only setting it if true is fine)
        String shading;

        @Override
        public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
            CompoundTag tag = super.toNBT(context);
            if (tag == null) return null;

            if (Objects.equals(shading, "smooth"))
                tag.putBoolean("smo", true);

            CompoundTag meshData = new CompoundTag();

            HashMap<String, Integer> vert2idx = new HashMap<>();
            HashMap<String, Vector3f> vert2pos = new HashMap<>();
            ListTag vtx = new ListTag();

            int i = 0;
            for (Map.Entry<String, Vector3f> entry : vertices.entrySet()) {
                Vector3f combined = new Vector3f(entry.getValue()).add(origin);
                vert2idx.put(entry.getKey(), i++);
                vert2pos.put(entry.getKey(), combined);

                vtx.add(FloatTag.valueOf(combined.x));
                vtx.add(FloatTag.valueOf(combined.y));
                vtx.add(FloatTag.valueOf(combined.z));
            }

            // textures _and_ vertex counts, despite the name
            ListTag tex = new ListTag();
            ListTag uvs = new ListTag();
            ListTag fac = new ListTag();

            int intType = 0; // byte
            if (i > 0xff) intType = 1; // short
            if (i > 0xffff) intType = 2; // int

            for (Map.Entry<String, MeshFace> entry : faces.entrySet()) {
                MeshFace face = entry.getValue();
                if (face.texture == null || face.vertices == null || face.uv == null || face.vertices.length < 3 || face.vertices.length > 4)
                    continue;

                // so, want a face with... 16 sides?
                // ...or 4096 textures? (which will cause other problems)
                // format 0xTTTn
                short texBase = (short) ((context.getTextureGlobalID(face.texture) << 4) + face.vertices.length);
                tex.add(ShortTag.valueOf(texBase));

                if (face.vertices.length == 4)
                    face.reorder(vert2pos);

                for (String vertID : face.vertices) {
                    Tag value = switch (intType) {
                        case 0 -> ByteTag.valueOf(vert2idx.get(vertID).byteValue());
                        case 1 -> ShortTag.valueOf(vert2idx.get(vertID).shortValue());
                        case 2 -> IntTag.valueOf(vert2idx.get(vertID));
                        default -> throw new IllegalStateException("bad int size key " + intType);
                    };
                    fac.add(value);

                    Vector2f uv = face.uv.get(vertID);
                    Vector2f fixedSize = context.getTextureFixedSize(face.texture);
                    uvs.add(FloatTag.valueOf(uv.x * fixedSize.x));
                    uvs.add(FloatTag.valueOf(uv.y * fixedSize.y));
                }
            }

            meshData.put("vtx", vtx);
            meshData.put("tex", tex);
            meshData.put("fac", fac);
            meshData.put("uvs", uvs);

            tag.put("mesh_data", meshData);
            return tag;
        }
    }

    public static class MeshFace {
        /// map of vertex ID to UV position
        Map<String, Vector2f> uv;
        /// list of vertex IDs
        String[] vertices;
        /// optional texture override
        Integer texture;

        public void reorder(Map<String, Vector3f> vertexIDtoPos) {
            if (vertices.length != 4) return;

            Vector3f v1 = vertexIDtoPos.get(vertices[0]);
            Vector3f v2 = vertexIDtoPos.get(vertices[1]);
            Vector3f v3 = vertexIDtoPos.get(vertices[2]);
            Vector3f v4 = vertexIDtoPos.get(vertices[3]);

            if (testOppositeSides(v2, v3, v1, v4)) {
                vertices = new String[]{
                        vertices[2],
                        vertices[0],
                        vertices[1],
                        vertices[3]
                };
            } else if (testOppositeSides(v1, v2, v3, v4)) {
                vertices = new String[]{
                        vertices[0],
                        vertices[2],
                        vertices[1],
                        vertices[3]
                };
            }
        }

        private static final Vector3f
                temp1 = new Vector3f(),
                temp2 = new Vector3f(),
                temp3 = new Vector3f(),
                temp4 = new Vector3f();

        /**
         * Checks if the two points are on opposite sides of the line formed by the two other points
         */
        private static boolean testOppositeSides(Vector3f line1, Vector3f line2, Vector3f point1, Vector3f point2) {
            // why does this work? I don't know
            temp1.set(line1);
            temp2.set(line2);
            temp3.set(point1);
            temp4.set(point2);

            temp2.sub(temp1);
            temp3.sub(temp1);
            temp4.sub(temp1);

            temp1.set(temp2);
            temp1.cross(temp3);
            temp2.cross(temp4);

            return temp1.dot(temp2) < 0;
        }
    }

    public static class PointElement extends Element {
        Vector3f position;
    }

    public static class Animation {
        String name;

        // once, loop, hold
        @Nullable String loop;
        @Nullable Boolean override;
        float length;

        @Nullable String anim_time_update;
        @Nullable String blend_weight;
        @Nullable String start_delay;
        @Nullable String loop_delay;

        // key is a UUID, or the string "effects"
        @Nullable Map<String, Animator> animators;
    }

    public static class Animator {
        // why does it do this? No clue
        /**
         * <a href="https://github.com/JannisX11/blockbench/blob/603201853499087aa3aa6a14406a061f27d4898f/js/io/formats/bbmodel.js#L72-L98">
         * v5 flipped the X and Y axes on rotations.
         * </a>
         */
        public static final Vector3f v5_ROT_TRANS = new Vector3f(-1, -1, 1);
        /**
         * <a href="https://github.com/JannisX11/blockbench/blob/603201853499087aa3aa6a14406a061f27d4898f/js/io/formats/bbmodel.js#L72-L98">
         * v5 flipped the X axis on positions.
         * </a>
         */
        public static final Vector3f v5_POS_TRANS = new Vector3f(-1, 1, 1);

        private static final Vector3f DO_NOTHING = new Vector3f(1, 1, 1);

        String name;
        String type;
        @Nullable Boolean rotation_global;
        // This goes unused in Figura, but I'm leaving it here in the hopes
        // that someone else picks it up, maybe
        @Nullable Boolean quaternion_interpolation;
        @Nullable List<Keyframe> keyframes;

        // okay so that qualified class name is horrible, but it's what we have

        /**
         * Get a NBT tag for this animator.
         *
         * @param animContext what {@link BlockbenchParser2.Intermediary.AnimationRepresentation} this is
         * @param isV5        if this is a v5 model; flips X and Y components of some things
         * @return the NBT
         */
        public @Nullable CompoundTag getNBT(BlockbenchParser2.Intermediary.AnimationRepresentation animContext,
                                            boolean isV5) {
            if (type.equals("effect")) throw new RuntimeException("Shouldn't be attaching FX animators to parts!");

            Vector3f rotTrans = isV5 ? v5_ROT_TRANS : DO_NOTHING;
            Vector3f posTrans = isV5 ? v5_POS_TRANS : DO_NOTHING;

            ListTag rot = new ListTag();
            ListTag pos = new ListTag();
            ListTag scale = new ListTag();

            if (keyframes != null)
                for (Keyframe kf : keyframes) {
                    if (!(kf instanceof Keyframe.Keyframe3 kf3)) continue;

                    CompoundTag kfTag = new CompoundTag();
                    kfTag.putFloat("time", kf3.time);
                    kfTag.putString("int", kf3.interpolation != null ? kf3.interpolation : "linear");
                    Keyframe.Keyframe3.Data pre = kf3.data_points[0];

                    Vector3f trans;
                    boolean transX = isV5 && (kf3.channel.equals("rotation") || kf3.channel.equals("position"));
                    boolean transY = isV5 && (kf3.channel.equals("rotation"));
                    switch(kf3.channel) {
                        case "rotation" -> trans = rotTrans;
                        case "position" -> trans = posTrans;
                        default -> trans = DO_NOTHING;
                    }

                    kfTag.put("pre", pre.toNBT(kf3.channel, transX, transY));
                    if (kf3.data_points.length > 1) {
                        Keyframe.Keyframe3.Data post = kf3.data_points[1];
                        kfTag.put("end", post.toNBT(kf3.channel, transX, transY));
                    }

                    // Bezier handles
                    if (kf3.bezier_left_value != null && !kf3.bezier_left_value.equals(0, 0, 0)) {
                        kfTag.put("bl", vecToList(new Vector3f(kf3.bezier_left_value).mul(trans)));
                    }
                    if (kf3.bezier_right_value != null && !kf3.bezier_right_value.equals(0, 0, 0)) {
                        kfTag.put("br", vecToList(new Vector3f(kf3.bezier_right_value).mul(trans)));
                    }
                    if (kf3.bezier_left_time != null && !kf3.bezier_left_time.equals(-0.1f, -0.1f, -0.1f)) {
                        kfTag.put("blt", vecToList(kf3.bezier_left_time));
                    }
                    if (kf3.bezier_right_time != null && !kf3.bezier_right_time.equals(0.1f, 0.1f, 0.1f)) {
                        kfTag.put("brt", vecToList(kf3.bezier_right_time));
                    }

                    switch (kf3.channel) {
                        case "position" -> pos.add(kfTag);
                        case "rotation" -> rot.add(kfTag);
                        case "scale" -> scale.add(kfTag);
                    }
                }

            CompoundTag animatorTag = new CompoundTag();
            CompoundTag channels = new CompoundTag();
            if (!rot.isEmpty()) {
                if (Boolean.TRUE.equals(rotation_global)) {
                    // ... does this work at all on the other end?
                    channels.put("grot", rot);
                } else {
                    channels.put("rot", rot);
                }
            }
            if (!pos.isEmpty()) channels.put("pos", pos);
            if (!scale.isEmpty()) channels.put("scl", scale);

            if (!channels.isEmpty()) {
                animatorTag.putInt("id", animContext.globalID);
                animatorTag.put("data", channels);
            } else {
                return null;
            }

            return animatorTag;
        }
    }

    public static class Keyframe {
        public static final TypeAdapterFactory ADAPTER_FACTORY =
                new GsonTypeByField<>(Keyframe.class, "channel")
                        .bind("position", Keyframe3.class)
                        .bind("rotation", Keyframe3.class)
                        .bind("scale", Keyframe3.class)
                        .bind("timeline", InstructionKeyframe.class)
                        .withFallback(Keyframe.class);

        String channel;
        @Nullable String interpolation;
        float time;

        // bezier interpolation
        @Nullable Vector3f bezier_left_time;
        @Nullable Vector3f bezier_left_value;
        @Nullable Vector3f bezier_right_time;
        @Nullable Vector3f bezier_right_value;

        public static class Keyframe3 extends Keyframe {
            public static class Data {
                String x;
                String y;
                String z;

                public ListTag toNBT(String channel, boolean invX, boolean invY) {
                    float fallback = channel.equals("scale") ? 1f : 0f;
                    Object x = kfData(this.x, fallback), y = kfData(this.y, fallback), z = kfData(this.z, fallback);

                    ListTag tag = new ListTag();
                    if (x instanceof Float xf && y instanceof Float yf && z instanceof Float zf) {
                        float xs = invX ? -1f : 1f;
                        float ys = invY ? -1f : 1f;
                        tag.add(FloatTag.valueOf(xf * xs));
                        tag.add(FloatTag.valueOf(yf * ys));
                        tag.add(FloatTag.valueOf(zf));
                    } else {
                        tag.add(StringTag.valueOf(String.valueOf(x)));
                        tag.add(StringTag.valueOf(String.valueOf(y)));
                        tag.add(StringTag.valueOf(String.valueOf(z)));
                    }

                    return tag;
                }
            }

            /// 1 or 2
            Data[] data_points;
        }

        public static class InstructionKeyframe extends Keyframe {
            public static class Data {
                String script;
            }

            /// should only be 1 item
            Data[] data_points;
        }

        public static Object kfData(String in, float fallback) {
            if (in == null || in.isBlank()) return fallback;
            try {
                return Float.parseFloat(in);
            } catch (NumberFormatException e) {
                return in;
            }
        }
    }

    public static class Texture {
        String name;
        String source;
        String relative_path;
        Float width;
        Float height;
        Float uv_width;
        Float uv_height;
    }
}
