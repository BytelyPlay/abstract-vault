package org.abstractvault.bytelyplay.data;

import org.abstractvault.bytelyplay.io.ResettableInputStream;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.abstractvault.bytelyplay.Getter;
import org.abstractvault.bytelyplay.Setter;
import org.abstractvault.bytelyplay.enums.DataFormat;
import org.abstractvault.bytelyplay.utils.GetterSetter;
import org.abstractvault.bytelyplay.utils.MapperProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// TODO: Clean this up by splitting this class into multiple helpers.
// TODO: Add a way to for example make a "coordinates: { x: 0, y: 0, z: 0 }" thing (able to make an object node with stuff inside of it, in a way where you can also nest more layers.
@Slf4j
@SuppressWarnings("unused")
public class DataSetter {
    // must be thread-safe
    
    private final Map<GetterSetter<?>, String> gettersSettersWithIDs;
    private final MapperProvider mapperProvider = new MapperProvider();

    public static class Builder {
        private final Map<GetterSetter<?>, String> gettersSettersWithIDs =
                Collections.synchronizedMap(new LinkedHashMap<>());
        private int defaultCounter = 0;

        public DataSetter build() {
            return new DataSetter(this);
        }

        public <T> Builder getterSetter(Getter<T> getter, Setter<T> setter) {
            int id = defaultCounter++;
            while (gettersSettersWithIDs.containsValue(String.valueOf(id))) id++;

            getterSetter(getter, setter, String.valueOf(id), null);
            return this;
        }

        // Recommended if the Getter can return null.
        public <T> Builder getterSetter(Getter<T> getter, Setter<T> setter,
                                        String ID, @Nullable Class<T> clazz) {
            if (gettersSettersWithIDs.containsValue(String.valueOf(ID))) {
                throw new IllegalArgumentException(
                        "Tried to add a getterSetter with an ID that already exists."
                );
            }
            gettersSettersWithIDs.put(
                    new GetterSetter<>(getter, setter, clazz),
                    ID
            );
            return this;
        }
    }
    private DataSetter(Builder builder) {
        this.gettersSettersWithIDs = builder.gettersSettersWithIDs;
    }

    public void save(Path jsonFile, @NotNull DataFormat format) {
        try (OutputStream stream = Files.newOutputStream(jsonFile)) {
            stream.write(serialize(format));
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't save.", e);
        }
    }
    public void load(Path jsonFile) throws IOException {
        load(Files.newInputStream(jsonFile));
    }
    public byte[] serialize(@NotNull DataFormat format) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (format != DataFormat.TEXT_JSON &&
                    format != DataFormat.TEXT_PRETTY_JSON)
                outputStream.write(format.getIdentifier());
            outputStream.write(mapperProvider.getWriter(format).writeValueAsBytes(buildJsonTree()));

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't serialize.", e);
        }
    }
    public void load(InputStream rawIn) throws IOException {
        InputStream in = new ResettableInputStream(rawIn);
        in.mark(1);

        DataFormat format = DataFormat.getFormatFromIdentifier((byte) in.read());

        if (format == null) {
            log.error("Format byte identifier wasn't included. file might be corrupted.");
            return;
        }
        if (format == DataFormat.TEXT_JSON || format == DataFormat.TEXT_PRETTY_JSON)
            in.reset();
        loadWithMapper(mapperProvider.getMapper(format), in);
    }
    @SuppressWarnings("unchecked")
    public @Nullable JsonNode buildJsonTree() throws JacksonException {
        ObjectMapper mapper = mapperProvider.getMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        for (GetterSetter<?> getterSetter : gettersSettersWithIDs.keySet()) {
            Getter<Object> getter = (Getter<Object>) getterSetter.getter;
            Object got = getter.get();
            String id = gettersSettersWithIDs.get(getterSetter);

            if (got == null) {
                rootNode.put(id, "null");
                continue;
            }

            JsonNode gotJsonNode = mapper.readTree(mapper.writeValueAsString(got));
            rootNode.set(id, gotJsonNode);
        }
        return rootNode;
    }
    @SuppressWarnings("unchecked")
    private void loadWithMapper(ObjectMapper mapper, InputStream stream)
            throws NullPointerException, JacksonException {
        JsonNode rootNode = mapper.readTree(stream);

        for (GetterSetter<?> getterSetter : gettersSettersWithIDs.keySet()) {
            Setter<Object> setter = (Setter<Object>) getterSetter.setter;
            Getter<Object> getter = (Getter<Object>) getterSetter.getter;

            String id = gettersSettersWithIDs.get(getterSetter);
            JsonNode subNode = rootNode.get(id);

            Class<?> clazz = getterSetter.getClazz();

            if (subNode == null) {
                log.error("No data at {} corrupted file?", id);
                continue;
            }

            if (subNode.isString() && subNode.asString().equals("null")) {
                setter.set(null);
                continue;
            }

            if (clazz == null)
                throw new NullPointerException("getterSetter.clazz is null, and getter returns null");

            Object obj = mapper.treeToValue(subNode, clazz);
            setter.set(obj);
        }
    }
    public JsonNode buildJsonTree(InputStream stream) throws IOException {
        stream.mark(1);

        DataFormat format = DataFormat.getFormatFromIdentifier((byte) stream.read());
        if (format == DataFormat.TEXT_JSON || format == DataFormat.TEXT_PRETTY_JSON) stream.reset();

        return mapperProvider.getMapper(format).readTree(stream);
    }
}