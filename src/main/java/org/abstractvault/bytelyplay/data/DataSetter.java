package org.abstractvault.bytelyplay.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.abstractvault.bytelyplay.Getter;
import org.abstractvault.bytelyplay.Setter;
import org.abstractvault.bytelyplay.enums.DataFormat;
import org.abstractvault.bytelyplay.utils.GetterSetter;
import org.abstractvault.bytelyplay.utils.MapperProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DataSetter {
    private final ConcurrentHashMap<GetterSetter<?>, String> gettersSettersWithIDs;
    private final MapperProvider mapperProvider = new MapperProvider();

    public static class Builder {
        private ConcurrentHashMap<GetterSetter<?>, String> gettersSettersWithIDs = new ConcurrentHashMap<>();
        private int defaultCounter = 0;
        public DataSetter build() {
            return new DataSetter(this);
        }
        public <T> Builder getterSetter(Getter<T> getter, Setter<T> setter) {
            int id = defaultCounter++;
            while (gettersSettersWithIDs.containsValue(String.valueOf(id))) id++;
            getterSetter(getter, setter, String.valueOf(id));
            return this;
        }
        public <T> Builder getterSetter(Getter<T> getter, Setter<T> setter, String ID) {
            if (gettersSettersWithIDs.containsValue(String.valueOf(ID))) {
                throw new IllegalArgumentException("Tried to add a getterSetter with an ID that already exists.");
            }
            gettersSettersWithIDs.put(new GetterSetter<>(getter, setter),
                    ID);
            return this;
        }
    }
    private DataSetter(Builder builder) {
        this.gettersSettersWithIDs = builder.gettersSettersWithIDs;
    }
    public void save(Path jsonFile, @NotNull DataFormat format) {
        try (FileOutputStream stream = new FileOutputStream(jsonFile.toString())) {
            stream.write(buildJsonTree(new FileInputStream(jsonFile.toString()), format));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void load(Path jsonFile) {
        try {
            load(new FileInputStream(jsonFile.toString()));
        } catch (FileNotFoundException e) {
            log.error("Tried to load a non-existent file.");
        }
    }
    public byte[] buildJsonTree(InputStream inputStream, @NotNull DataFormat format) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(mapperProvider.getWriter(format).writeValueAsBytes(buildJsonTree()));
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
    public void load(InputStream stream) {
        try {
            stream.mark(1);
            DataFormat format = DataFormat.getFormatFromIdentifier((byte) stream.read());
            if (format == null) {
                log.error("Format byte identifier wasn't included. file might be corrupted.");
                return;
            }
            if (format == DataFormat.TEXT_JSON || format == DataFormat.TEXT_PRETTY_JSON) stream.reset();
            loadWithMapper(mapperProvider.getMapper(format), stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    public @Nullable JsonNode buildJsonTree() {
        try {
            ObjectMapper mapper = mapperProvider.getMapper();
            ObjectNode rootNode = mapper.createObjectNode();
            for (GetterSetter<?> getterSetter : gettersSettersWithIDs.keySet()) {
                ObjectNode objectNode = mapper.createObjectNode();
                Getter<Object> getter = (Getter<Object>) getterSetter.getter;
                Object got = getter.get();

                objectNode.put("class", got.getClass().getName());

                JsonNode getJsonNode = mapper.readTree(mapper.writeValueAsString(got));
                objectNode.set("data", getJsonNode);

                rootNode.set(gettersSettersWithIDs.get(getterSetter), objectNode);
            }
            return rootNode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    private void loadWithMapper(ObjectMapper mapper, InputStream stream) {
        try {
            JsonNode rootNode = mapper.readTree(stream);
            for (GetterSetter<?> getterSetter : gettersSettersWithIDs.keySet()) {
                String id = gettersSettersWithIDs.get(getterSetter);
                JsonNode subNode = rootNode.get(id);
                if (subNode == null) {
                    log.error("No data at {} corrupted file?", id);
                    continue;
                }

                Setter<Object> setter = (Setter<Object>) getterSetter.setter;

                Class<?> clazz = Class.forName(subNode.get("class").asText());
                Object obj = mapper.treeToValue(subNode.get("data"), clazz);
                setter.set(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}