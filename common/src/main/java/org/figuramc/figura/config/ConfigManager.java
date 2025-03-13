package org.figuramc.figura.config;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.PlatformUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public final class ConfigManager {

	private static File PER_INSTANCE_FILE = new File(PlatformUtils.getConfigDir().resolve(FiguraMod.FORK_ID + "-PER-INSTANCE").toString());
	private static File FILE = new File(PlatformUtils.getConfigDir().resolve(FiguraMod.FORK_ID + ".json").toString());
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	public static final List<ConfigType<?>> REGISTRY = new ArrayList<>();
	public static final Map<String, ConfigType.Category> CATEGORIES_REGISTRY = new LinkedHashMap<>();

	private static boolean initializing = false;
	private static boolean perInstance = false;
	private static JsonObject configJson = new JsonObject();

	public static void init() {
		Configs.init();
		loadConfig();
		saveConfig();
	}
	public static void findConfig(){
		Configs.PER_INSTANCE_CONFIG.value = perInstance = PER_INSTANCE_FILE.exists();
		FILE = new File((perInstance ? PlatformUtils.getConfigDir() : FiguraMod.getFiguraDirectory()).resolve(FiguraMod.FORK_ID + ".json").toString());
	}
	public static void changePerInstance(boolean setting){
		try{

			if(perInstance) PER_INSTANCE_FILE.createNewFile();
			else PER_INSTANCE_FILE.delete();
		}catch(IOException ignored){

		}
		loadConfig();
	}
	public static void loadConfig() {
		findConfig();
		try {
			if (FILE.exists()) {
				initializing = true;
				BufferedReader br = new BufferedReader(new FileReader(FILE));
				configJson = JsonParser.parseReader(br).getAsJsonObject();


				JsonElement version = configJson.get("CONFIG_VERSION");
				if (version != null && version.getAsInt() != Configs.CONFIG_VERSION) {
					update(configJson, version.getAsInt());
				} else {
					for (ConfigType<?> config : REGISTRY) {
						JsonElement object = configJson.get(config.id.toLowerCase(Locale.US));
						if (object == null)
							continue;
						if (config instanceof ConfigType.SerializableConfig s) {
							s.deserialize(object);
							continue;
						}
						String obj = object.getAsString();
						if (config instanceof ConfigType.KeybindConfig keybind) {
							keybind.keyBind.setKey(InputConstants.getKey(obj));
						} else if (config instanceof ConfigType.InputConfig<?> input) {
							if (input.inputType.validator.test(obj))
								config.setValue(obj);
						} else {
							config.setValue(obj);
						}
					}
				}

				br.close();
				FiguraMod.debug("Successfully loaded config file");
			}
		} catch (Exception e) {
			FiguraMod.LOGGER.warn("Failed to load config file! Resetting all settings...", e);
			setDefaults();
		}

		initializing = false;
	}

	public static void saveConfig() {
		if (initializing)
			return;

		try {
			configJson.addProperty("CONFIG_VERSION", Configs.CONFIG_VERSION);

			for (ConfigType<?> config : REGISTRY) {
				if (config.isDefault())
					continue;

				String id = config.id;
				if (config instanceof ConfigType.SerializableConfig s)
					configJson.add(id, s.serialize());
				else if (config.value instanceof Number n)
					configJson.addProperty(id, n);
				else if (config.value instanceof Character c)
					configJson.addProperty(id, c);
				else if (config.value instanceof Boolean b)
					configJson.addProperty(id, b);
				else if (config.value != null)
					configJson.addProperty(id, String.valueOf(config.value));
			}

			String jsonString = GSON.toJson(configJson);
			FileWriter fileWriter = new FileWriter(FILE);
			fileWriter.write(jsonString);
			fileWriter.close();

			FiguraMod.debug("Successfully saved config file");
		} catch (Exception e) {
			FiguraMod.LOGGER.error("Failed to save config file!", e);
		}
	}

	public static void applyConfig() {
		for (ConfigType<?> config : REGISTRY)
			config.setValue(String.valueOf(config.tempValue));
	}

	public static void discardConfig() {
		for (ConfigType<?> config : REGISTRY)
			config.discardConfig();
	}

	public static void setDefaults() {
		for (ConfigType<?> config : REGISTRY)
			config.setDefault();
	}

	public static void update(JsonObject json, int version) {
		Map<ConfigType<?>, String> versionMap = Configs.CONFIG_UPDATES.get(version);
		if (versionMap == null)
			return;

		for (Map.Entry<ConfigType<?>, String> config : versionMap.entrySet()) {
			JsonElement object = json.get(config.getValue());

			if (object == null)
				continue;

			String jsonValue = object.getAsString();
			config.getKey().setValue(jsonValue);
		}

		FiguraMod.debug("Config updated from version " + version);
	}
}