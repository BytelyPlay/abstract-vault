package org.abstractvault.bytelyplay.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.abstractvault.bytelyplay.Getter;
import org.abstractvault.bytelyplay.Setter;
import org.abstractvault.bytelyplay.enums.DataFormat;
import org.abstractvault.bytelyplay.utils.GetterSetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DataSetter {
    private final ConcurrentHashMap<GetterSetter<?>, String> gettersSettersWithIDs;

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
    public void save(Path jsonFile, @NotNull @NonNull DataFormat format) {
        try (FileOutputStream stream = new FileOutputStream(jsonFile.toString())) {
            switch (format) {
                case DataFormat.TEXT_JSON -> {
                    ObjectMapper mapper = new ObjectMapper();
                    stream.write(mapper.writeValueAsBytes(getDataToSave()));
                }
                case DataFormat.TEXT_PRETTY_JSON -> {
                    ObjectMapper mapper = new ObjectMapper();
                    stream.write(mapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsBytes(getDataToSave()));
                }
                case BINARY_CBOR -> {
                    ObjectMapper mapper = new ObjectMapper(new CBORFactory());
                    stream.write(format.getIdentifier());
                    stream.write(mapper.writeValueAsBytes(getDataToSave()));
                }
                case BINARY_SMILE -> {
                    ObjectMapper mapper = new ObjectMapper(new SmileFactory());
                    stream.write(format.getIdentifier());
                    stream.write(mapper.writeValueAsBytes(getDataToSave()));
                }
                default -> log.error("Unimplemented DataFormat? defaulted to default.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void load(Path jsonFile) {
        try {
            FileInputStream stream = new FileInputStream(jsonFile.toString());
            DataFormat format = DataFormat.getFormatFromIdentifier((byte) stream.read());
            if (format == null) {
                log.error("Format byte identifier wasn't included. file might be corrupted.");
                return;
            }
            switch (format) {
                case DataFormat.TEXT_JSON, DataFormat.TEXT_PRETTY_JSON -> {
                    stream.close();
                    stream = new FileInputStream(jsonFile.toString());
                    loadWithMapper(new ObjectMapper(), stream);
                }
                case BINARY_CBOR -> loadWithMapper(new ObjectMapper(new CBORFactory()), stream);
                case BINARY_SMILE -> loadWithMapper(new ObjectMapper(new SmileFactory()), stream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    public @Nullable JsonNode getDataToSave() {
        try {
            ObjectMapper mapper = new ObjectMapper();
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
    private void loadWithMapper(ObjectMapper mapper, FileInputStream stream) {
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
