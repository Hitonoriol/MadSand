package hitonoriol.madsand.resources;

import static hitonoriol.madsand.resources.Resources.readInternal;
import static hitonoriol.madsand.world.GameSaver.readFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;

import hitonoriol.madsand.Enumerable;
import hitonoriol.madsand.commons.reflection.Reflection;
import hitonoriol.madsand.containers.Pair;

public class Serializer extends ObjectMapper {

	public Serializer(DefaultTyping typing) {
		enable(SerializationFeature.INDENT_OUTPUT);
		configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
		configure(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature(), true);
		configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		configure(Feature.ALLOW_COMMENTS, true);
		configure(Feature.ALLOW_YAML_COMMENTS, true);
		activateDefaultTyping(getPolymorphicTypeValidator(), typing);
		regsiterModules();
	}

	public Serializer() {
		this(DefaultTyping.OBJECT_AND_NON_CONCRETE);

	}

	private void regsiterModules() {
		var module = new SimpleModule();
		module.addKeyDeserializer(Pair.class, new Pair.PairKeyDeserializer());
		module.addAbstractTypeMapping(Map.class, HashMap.class);
		registerModule(module);
	}

	public <T> T update(T obj, JsonNode json) {
		try {
			return readerForUpdating(obj).readValue(json);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public <T> T loadString(String jsonStr, Class<T> type) {
		try {
			return readerFor(type).readValue(jsonStr);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public <T> T load(TreeNode node, Class<T> type) {
		try {
			return treeToValue(node, type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public <T> T load(String internalFile, Class<T> type) {
		return loadString(readInternal(internalFile), type);
	}

	public void save(String file, Object object) {
		try {
			writeValue(new File(file), object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public <T> ArrayList<T> loadList(String internalFile, Class<T> type) {
		try {
			return readValue(
				readInternal(internalFile),
				getTypeFactory().constructCollectionType(ArrayList.class, type)
			);
		} catch (Exception e) {
			return null;
		}
	}

	public MapType getMapType(Class<? extends Map<?, ?>> mapClass, Class<?> key, Class<?> value) {
		return getTypeFactory().constructMapType(mapClass, key, value);
	}

	public ObjectWriter getMapWriter(Class<? extends Map<?, ?>> mapClass, Class<?> key, Class<?> value) {
		return writerFor(getMapType(mapClass, key, value));
	}

	public ObjectWriter getMapWriter(Class<?> key, Class<?> value) {
		return getMapWriter(Reflection.mapClass(HashMap.class), key, value);
	}

	public ObjectReader getMapReader(Class<? extends Map<?, ?>> mapClass, Class<?> key, Class<?> value) {
		return readerFor(getMapType(mapClass, key, value));
	}

	public ObjectReader getMapReader(Class<?> key, Class<?> value) {
		return getMapReader(Reflection.mapClass(HashMap.class), key, value);
	}

	public ObjectReader getMapReader(Class<?> value) {
		return getMapReader(Pair.class, value);
	}

	public ObjectWriter getMapWriter(Class<?> value) {
		return getMapWriter(Pair.class, value);
	}

	private <T extends Map<K, V>, K, V> T loadMap(
		String file, Class<T> mapClass, Class<K> keyType, Class<V> valueType,
		boolean internal
	) {
		try {
			return readValue(internal ? readInternal(file) : readFile(file), getMapType(mapClass, keyType, valueType));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public <T extends Map<K, V>, K, V> T loadMap(
		String internalFile, Class<T> mapClass, Class<K> keyType,
		Class<V> valueType
	) {
		return loadMap(internalFile, mapClass, keyType, valueType, true);
	}

	public <T extends Map<K, V>, K, V> T loadMap(String internalFile, Class<K> keyType, Class<V> valueType) {
		return loadMap(internalFile, Reflection.mapClass(HashMap.class), keyType, valueType);
	}

	public <K, V> HashMap<K, V> readMap(String file, Class<K> keyType, Class<V> valueType) {
		return loadMap(file, Reflection.mapClass(HashMap.class), keyType, valueType, false);
	}

	public <V> HashMap<Pair, V> readMap(String file, Class<V> valueType) {
		return readMap(file, Pair.class, valueType);
	}

	public String saveMap(Map<?, ?> map, Class<?> keyType, Class<?> valType) {
		try {
			return getMapWriter(keyType, valType).writeValueAsString(map);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String saveMap(Map<?, ?> map, Class<?> valType) {
		return saveMap(map, Pair.class, valType);
	}

	public <K, V> void saveMap(String file, Map<K, V> map, Class<K> keyType, Class<V> valType) {
		try {
			getMapWriter(keyType, valType).writeValue(new File(file), map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public <T extends Enumerable> Map<Integer, T> loadEnumerableMap(
		String internalFile,
		Class<T> type,
		Consumer<T> initAction
	) {
		Map<Integer, T> map = loadMap(internalFile, Enumerable.idType, type);
		map.forEach((id, value) -> {
			value.setId(id);
			if (initAction != null)
				initAction.accept(value);
		});
		return map;
	}

	public <T extends Enumerable> Map<Integer, T> loadEnumerableMap(String internalFile, Class<T> type) {
		return loadEnumerableMap(internalFile, type, null);
	}
}
