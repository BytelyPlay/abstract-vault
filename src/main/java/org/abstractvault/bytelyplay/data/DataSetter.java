package org.abstractvault.bytelyplay.data;

import lombok.extern.slf4j.Slf4j;
import org.abstractvault.bytelyplay.Getter;
import org.abstractvault.bytelyplay.Setter;
import org.abstractvault.bytelyplay.utils.GetterSetter;
import org.abstractvault.bytelyplay.utils.MapperProvider;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO: Clean this up by splitting this class into multiple helpers.
// TODO: Add a way to for example make a "coordinates: { x: 0, y: 0, z: 0 }" thing (able to make an object node with stuff inside of it, in a way where you can also nest more layers.
@Slf4j
@SuppressWarnings("unused")
public class DataSetter {
    @lombok.Getter
    private final DataSerializer serializer;

    public static class Builder {
        // must be thread-safe
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
        this.serializer = new DataSerializer(new MapperProvider());
    }

}