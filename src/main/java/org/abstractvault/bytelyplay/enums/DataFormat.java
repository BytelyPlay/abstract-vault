package org.abstractvault.bytelyplay.enums;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public enum DataFormat {
    BINARY_SMILE((byte) 0x00),
    BINARY_CBOR((byte) 0x01),
    TEXT_JSON((byte) '{'),
    TEXT_PRETTY_JSON((byte) '{');

    @Getter
    private final byte identifier;

    DataFormat(byte b) {
        this.identifier = b;
    }
    public static @Nullable DataFormat getFormatFromIdentifier(byte identifier) {
        for (DataFormat format : DataFormat.values()) {
            if (format.identifier == identifier) return format;
        }
        return null;
    }
}
