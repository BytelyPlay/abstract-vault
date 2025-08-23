package org.abstractvault.bytelyplay.utils;

import org.abstractvault.bytelyplay.Getter;
import org.abstractvault.bytelyplay.Setter;

public class GetterSetter<T> {
    public final Getter<T> getter;
    public final Setter<T> setter;

    public GetterSetter(Getter<T> getter, Setter<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }
}
