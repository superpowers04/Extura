package org.figuramc.figura.parsers;

import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.figuramc.figura.FiguraMod;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Stream;

/**
 * Entrypoint for fuzzing the model parser.
 */
@SuppressWarnings("deprecation") // we're using the old parser ofc
public class ModelFuzzerMain {

    public static final int MAX_IN_QUEUE = 5000;

    public record State(JsonElement base, int depth, String label, List<String> mutations) {
    }

    private static final Gson GSON_RAW = new GsonBuilder().create();
    private final SecureRandom rand;

    public static final int MAX_EXPLORE_PER_LAYER = 7;
    public static final int MAX_DEPTH = 10;
    public static final Path OUT_TO = Path.of("build/reports/fuzz");

    public static final Set<String> NOT_DELETABLE = Set.of(
            // known issues WONTFIX (i.e. definitely wrong)
            "width", "height", "rotation",
            "uv_width", "uv_height", "resolution"
    );
    public static final Set<String> NOT_MODIFIABLE = Set.of(
            // known issues WONTFIX (i.e. definitely wrong)
            "resolution", "origin",
            "bezier_left_time", "bezier_right_time", "bezier_left_value", "bezier_right_value"
    );

    static {
        setupOutput();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void setupOutput() {
        if (OUT_TO.toFile().exists()) {
            try (Stream<Path> paths = Files.walk(OUT_TO)) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                LOGGER.error("Failed to clean output", e);
            }
        }
        OUT_TO.toFile().mkdirs();
    }

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("Figura Model Fuzzer");

    private ModelFuzzerMain(byte[] seed) {
        rand = new SecureRandom(seed);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> T bindUnderlyingLogger(org.slf4j.Logger slf, Class<T> to) {
        try {
            final Class<? extends org.slf4j.Logger> cls = slf.getClass();
            final Field field = cls.getDeclaredField("logger");
            field.setAccessible(true);
            return to.cast(field.get(slf));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("failed to bind logging backend", e);
        }
    }

    public static void main(String[] args) {
        // Enable mocking for normally @ExpectPlatform methods
        System.setProperty("figura.standalone_testing", "true");

        String pathToFile = args[0];
        org.apache.logging.log4j.Logger figuraLogger = bindUnderlyingLogger(
                FiguraMod.LOGGER,
                org.apache.logging.log4j.Logger.class
        );

        byte[] seed;
        seed = new byte[]{0, 0, 0, 0, 0, 0, 0, 4};

        // make Figura shut up while we're running the show
        Configurator.setLevel(figuraLogger, Level.FATAL);

        importFile(Path.of(pathToFile));


        ModelFuzzerMain INSTANCE = new ModelFuzzerMain(seed);
        INSTANCE.exploreUntilExhaustion(new State(baseStructure, 0, "a", List.of()));
    }

    private static void importFile(Path file) {
        try {
            LOGGER.info("importing model file {}", file);
            String s = Files.readString(file);
            baseStructure = GSON_RAW.fromJson(s, JsonElement.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reportFailure(String kind, JsonElement problem, String label, List<String> mutations) {
        try {
            Path modelFile = OUT_TO.resolve(label + "_" + kind + ".bbmodel");
            Path reportingFile = OUT_TO.resolve(label + "_" + kind + ".txt");

            String jsonified = GSON_RAW.toJson(problem);
            Files.writeString(modelFile, jsonified, StandardCharsets.UTF_8);
            Files.write(reportingFile, mutations, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Report failure failed:", e);
        }
    }

    public final Deque<State> queue = new ArrayDeque<>();

    public void exploreUntilExhaustion(State initial) {
        queue.add(initial);
        int n = 0;
        while (!queue.isEmpty()) {
            if (queue.size() > MAX_IN_QUEUE) {
                int toYeet = rand.nextInt(MAX_EXPLORE_PER_LAYER * 4);
                for (int i = 0; i < toYeet; i++) queue.pop();
            }
            n++;
            explore(queue.pop());
            if (n % 100 == 0) {
                LOGGER.info("---- PROGRESS REPORT ----");
                LOGGER.info("  {} completed", n);
                LOGGER.info("  {} in queue", queue.size());
            }
        }
    }

    public void explore(State s) {
        JsonElement base = s.base;
        String label = s.label;
        int depth = s.depth;
        List<String> mutations = s.mutations;
        // Try this one.
        boolean passed = tryIt(base, label);

        // If we failed, then stop here; we've found a defect.
        if (!passed) {
            reportFailure("fail", base, label, mutations);
            return;
        }

        if (depth >= MAX_DEPTH) return;

        // Try to derive some stuff from this node
        Deque<String> path = new ArrayDeque<>();
        for (int i = 0; i < MAX_EXPLORE_PER_LAYER; i++) {
            List<String> mutCopy = new ArrayList<>(mutations);
            path.clear();
            JsonElement derived = warp(base, path, mutCopy);
            queue.add(new State(derived, depth + 1, label + i, mutCopy));
        }
    }


    BlockbenchModelParser oldParser;
    BlockbenchParser2 newParser;

    static JsonElement baseStructure = null;
    static final Path avatarFolder = Path.of(".").toAbsolutePath();
    static final Path modelPath = Path.of("./example.bbmodel").toAbsolutePath();


    public void before() {
        oldParser = new BlockbenchModelParser();
        newParser = new BlockbenchParser2();
    }

    private static final double STR_MODIFY = 0.33;
    private static final double STR_ADD = 0.33;
    // rest is STR_REMOVE

    public String mutateString(String input, Deque<String> path, List<String> mutations) {
        // TODO
        double f = rand.nextDouble();
        throw new RuntimeException();
    }

    private static final double DELETE_CHANCE = 0.05;

    public JsonElement warp(JsonElement element, Deque<String> path, List<String> mutations) {
        element = element.deepCopy();
        // do... something
        double f = rand.nextDouble();
        boolean deleteSomething = f <= DELETE_CHANCE;
        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            if (arr.isEmpty()) return arr;
            int el = rand.nextInt(arr.size());
            path.add("[" + el + "]");
            if (deleteSomething) {
                mutations.add(String.format(
                        "Deleted %s",
                        String.join("", path)
                ));
                arr.remove(el);
            } else arr.set(el, warp(arr.get(el), path, mutations));
            path.removeLast();
            return arr;
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.size() == 0) return obj;
            String key = obj.keySet().stream()
                    .skip(rand.nextInt(obj.size()))
                    .findFirst()
                    .orElse(null);
            if (key == null) return obj;
            if (NOT_DELETABLE.contains(key) && deleteSomething) return obj;
            if (NOT_MODIFIABLE.contains(key) && !deleteSomething) return obj;
            path.add("." + key);
            if (deleteSomething) {
                mutations.add(String.format(
                        "Deleted %s",
                        String.join("", path)
                ));
                obj.remove(key);
            } else obj.add(key, warp(obj.get(key), path, mutations));
            path.removeLast();
            return obj;
        }
        if (element.isJsonNull()) return element;
        if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isNumber()) {
                int by = rand.nextInt(-2, 2);
                mutations.add(String.format(
                        "Changed %s (%.2f -> %.2f)",
                        String.join("", path),
                        prim.getAsNumber().doubleValue(),
                        prim.getAsNumber().doubleValue() + by
                ));
                return new JsonPrimitive(
                        prim.getAsNumber().doubleValue() + by
                );
            }
            if (prim.isBoolean()) {
                mutations.add(String.format(
                        "Changed %s (%s -> %s)",
                        String.join("", path),
                        prim.getAsBoolean(),
                        !prim.getAsBoolean()
                ));
                return new JsonPrimitive(!prim.getAsBoolean());
            }
        }
        return element;
    }

    public boolean tryIt(JsonElement structure, String label) {
        LOGGER.info("-- {} --", label);
        boolean pass = true;
        before();
        boolean failedOld, failedNew;
        ModelParseResult oldRes, newRes;
        String serialized = GSON_RAW.toJson(structure);
        try {
            oldRes = oldParser.parseModel(avatarFolder, modelPath, serialized, "example", "");
            failedOld = false;
        } catch (Exception e) {
            LOGGER.info("Old parser failed with exception", e);
            failedOld = true;
            oldRes = null;
        }
        try {
            newRes = newParser.parseModel(avatarFolder, modelPath, serialized, "example", "");
            failedNew = false;
        } catch (Exception e) {
            LOGGER.info("New parser failed with exception", e);
            failedNew = true;
            newRes = null;
        }

        if (failedNew && !failedOld) {
            LOGGER.error("-- {} FAILED (exception in new parser, old parser passed) --", label);
            pass = false;
        }
        if (failedOld && !failedNew) {
            LOGGER.warn("{} mismatch (exception in old parser, new parser passed)", label);
        }

        if (oldRes != null && newRes != null) {
            CompoundTag texturesOld = oldRes.textures().getCompound("src");
            CompoundTag texturesNew = newRes.textures().getCompound("src");
            List<CompoundTag> animationsOld = oldRes.animationList();
            List<CompoundTag> animationsNew = newRes.animationList();

            // Check the same texture paths are imported
            Set<String> texturePathsOld = texturesOld.getAllKeys();
            Set<String> texturePathsNew = texturesNew.getAllKeys();
            Set<String> extraOld = new HashSet<>(texturePathsOld);
            Set<String> extraNew = new HashSet<>(texturePathsNew);
            extraOld.removeAll(texturePathsNew);
            extraNew.removeAll(texturePathsOld);
            if (!extraOld.isEmpty() || !extraNew.isEmpty()) {
                LOGGER.error("-- {} FAILED (mismatched texture paths) --", label);
                pass = false;
            } else {
                LOGGER.info("{} textures match", texturePathsOld.size());
            }
            if (!extraOld.isEmpty()) {
                LOGGER.error("old &! new: {}", String.join(",", extraOld));
            }
            if (!extraNew.isEmpty()) {
                LOGGER.error("new &! old: {}", String.join(",", extraNew));
            }
        }

        LOGGER.info("-- {} COMPLETED {} --", label, pass ? "PASSED" : "FAILED");

        return pass;
    }
}
