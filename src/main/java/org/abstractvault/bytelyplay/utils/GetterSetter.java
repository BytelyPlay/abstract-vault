package org.abstractvault.bytelyplay.utils;

import lombok.extern.slf4j.Slf4j;
import org.abstractvault.bytelyplay.Getter;
import org.abstractvault.bytelyplay.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import tools.jackson.core.type.TypeReference;

import java.lang.reflect.TypeVariable;

@Slf4j
public class GetterSetter<T> {
    public final @NotNull Getter<T> getter;
    public final @NotNull Setter<T> setter;
    private Class<T> clazz;

    @SuppressWarnings("unchecked")
    public GetterSetter(Getter<T> getter, Setter<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }
    public GetterSetter(Getter<T> getter, Setter<T> setter, @Nullable Class<T> clazz) {
        this.getter = getter;
        this.setter = setter;
        this.clazz = clazz;
    }
    @SuppressWarnings("unchecked")
    public @Nullable Class<T> getClazz() {
        if (clazz != null) return clazz;

        T got = getter.get();
        if (got != null) return (Class<T>) got.getClass();

        return null;
    }
}
