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

@Slf4j
public class DataSetter {
    private final ArrayList<GetterSetter<?>> gettersSetters;

    public static class Builder {
        private ArrayList<GetterSetter<?>> gettersSetters = new ArrayList<>();
        public DataSetter build() {
            return new DataSetter(this);
        }
        public <T> Builder getterSetter(Getter<T> getter, Setter<T> setter) {
            gettersSetters.add(new GetterSetter<T>(getter, setter));
            return this;
        }
    }
    private DataSetter(Builder builder) {
        this.gettersSetters = builder.gettersSetters;
    }
    public void save(Path jsonFile, @NotNull @NonNull DataFormat format) {
        try (FileOutputStream stream = new FileOutputStream(jsonFile.toString())) {
            switch (format) {
                case DataFormat.TEXT_JSON -> {
                    ObjectMapper mapper = new ObjectMapper();
                    stream.write(format.getIdentifier());
                    stream.write(mapper.writeValueAsBytes(getDataToSave()));
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
        try (FileInputStream stream = new FileInputStream(jsonFile.toString())) {
            DataFormat format = DataFormat.getFormatFromIdentifier((byte) stream.read());
            if (format == null) {
                log.error("Format byte identifier wasn't included. file might be corrupted.");
                return;
            }
            switch (format) {
                case DataFormat.TEXT_JSON -> loadWithMapper(new ObjectMapper());
                case BINARY_CBOR -> loadWithMapper(new ObjectMapper(new CBORFactory()));
                case BINARY_SMILE -> loadWithMapper(new ObjectMapper(new SmileFactory()));
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
            for (int i = 0; i < gettersSetters.size(); i++) {
                GetterSetter<?> getterSetter = gettersSetters.get(i);

                ObjectNode objectNode = mapper.createObjectNode();
                Getter<Object> getter = (Getter<Object>) getterSetter.getter;
                Object got = getter.get();

                objectNode.put("class", got.getClass().getName());

                JsonNode getJsonNode = mapper.readTree(mapper.writeValueAsString(got));
                objectNode.set("data", getJsonNode);

                rootNode.set(String.valueOf(i), objectNode);
            }
            return rootNode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    private void loadWithMapper(ObjectMapper mapper) {
        try {
            JsonNode rootNode = mapper.readTree(new FileInputStream(jsonFile.toString()));
            for (int i = 0; i < gettersSetters.size(); i++) {
                JsonNode subNode = rootNode.get(String.valueOf(i));

                GetterSetter<?> getterSetter = gettersSetters.get(i);
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
