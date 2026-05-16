package org.abstractvault.bytelyplay.data;

import lombok.extern.slf4j.Slf4j;
import org.abstractvault.bytelyplay.Getter;
import org.abstractvault.bytelyplay.Setter;
import org.abstractvault.bytelyplay.enums.DataFormat;
import org.abstractvault.bytelyplay.utils.GetterSetter;
import org.abstractvault.bytelyplay.utils.MapperProvider;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// TODO: Add a way to for example make a "coordinates: { x: 0, y: 0, z: 0 }" thing (able to make an object node with stuff inside of it, in a way where you can also nest more layers.
@Slf4j
@SuppressWarnings("unused")
public class DataSetter {
    @lombok.Getter
    private final DataSerializer serializer;

    private final Map<GetterSetter<?>, String> getterSetterId;

    // Immutable
    public static class Builder {
        // The GetterSetter<?> and a String which is the ID.
        private Map<GetterSetter<?>, String> getterSetterId =
                new LinkedHashMap<>();;
        private int counter = 0;
        private boolean built = false;

        public DataSetter build() {
            built = true;
            getterSetterId = Collections.unmodifiableMap(getterSetterId);

            return new DataSetter(this);
        }

        public <T> Builder getterSetter(
                Getter<T> getter, Setter<T> setter
        ) {
            throwIfBuilt();

            return getterSetter(getter, setter, String.valueOf(
                    counter++
            ));
        }

        public <T> Builder getterSetter(
                Getter<T> getter, Setter<T> setter, String id
        ) {
            throwIfBuilt();

            return getterSetter(getter, setter, id, null);
        }

        public <T> Builder getterSetter(
                Getter<T> getter,
                Setter<T> setter,
                String id,
                @Nullable Class<T> clazz
        ) {
            throwIfBuilt();

            getterSetterId.put(new GetterSetter<>(getter, setter, clazz), id);
            return this;
        }
        private void throwIfBuilt() {
            if (built)
                throw new IllegalStateException(
                        "Tried to do something in Builder after it is built."
                );
        }
    }

    private DataSetter(Builder builder) {
        this.serializer = new DataSerializer(new MapperProvider());
        this.getterSetterId = builder.getterSetterId;
    }

    public void save(Path file, DataFormat format) throws IOException {
        serialize(Files.newOutputStream(file), format);
    }

    /**
     * Loads everything from disk.
     *
     * @param file The file.
     * @throws IOException              When something goes wrong IO related.
     * @throws IllegalArgumentException If the ID to Class<?> map has a null Class when it
     *                                  shouldn't be null, this is impossible to be thrown if all getters
     *                                  either always return non-null or you put in the classes for each entry.
     */
    public void load(Path file)
            throws IOException, IllegalArgumentException {
        deserialize(Files.newInputStream(file));
    }

    public void serialize(OutputStream out, DataFormat format)
            throws IOException {
        out.write(
                serializer.serializeMapTree(
                        createMapTreeFromGetters(), format
                )
        );
    }

    public void deserialize(InputStream in)
            throws IOException, IllegalArgumentException {
        setValuesFromMapTree(
                serializer.deserializeToMapTree(in, createKeyClassMap())
        );
    }

    private Map<String, Object> createMapTreeFromGetters() {
        LinkedHashMap<String, Object> mapTree = new LinkedHashMap<>();

        for (Map.Entry<GetterSetter<?>, String> entry : getterSetterId.entrySet()) {
            Getter<?> getter = entry.getKey().getter;

            mapTree.put(entry.getValue(),
                    getter.get());
        }
        return Collections.unmodifiableMap(mapTree);
    }

    // This maps each ID to its own Class.
    private Map<String, Class<?>> createKeyClassMap() {
        LinkedHashMap<String, Class<?>> idClassMap = new LinkedHashMap<>();

        for (Map.Entry<GetterSetter<?>, String> entry : getterSetterId.entrySet()) {
            idClassMap.put(entry.getValue(),
                    entry.getKey().getClazz());
        }
        return Collections.unmodifiableMap(idClassMap);
    }

    @SuppressWarnings("unchecked")
    private void setValuesFromMapTree(Map<String, Object> mapTree) {
        for (Map.Entry<GetterSetter<?>, String> entry : getterSetterId.entrySet()) {
            Setter<Object> setter = (Setter<Object>) entry.getKey().setter;
            String id = entry.getValue();
            Object obj = mapTree.get(id);

            setter.set(obj);
        }
    }
}