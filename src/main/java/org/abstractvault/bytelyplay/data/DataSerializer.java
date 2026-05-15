package org.abstractvault.bytelyplay.data;

import lombok.extern.slf4j.Slf4j;
import org.abstractvault.bytelyplay.Getter;
import org.abstractvault.bytelyplay.Setter;
import org.abstractvault.bytelyplay.enums.DataFormat;
import org.abstractvault.bytelyplay.io.ResettableInputStream;
import org.abstractvault.bytelyplay.utils.GetterSetter;
import org.abstractvault.bytelyplay.utils.MapperProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

@Slf4j
public class DataSerializer {
    private final MapperProvider mapperProvider;
    private final JsonTreeMapTreeConverter converter;

    public DataSerializer(MapperProvider provider) {
        this.mapperProvider = provider;
        this.converter = new JsonTreeMapTreeConverter(provider);
    }
    public JsonNode deserializeToJsonTree(InputStream rawIn)
            throws IOException {
        try {
            ResettableInputStream in = new ResettableInputStream(rawIn);
            in.mark(1);

            DataFormat format = DataFormat.getFormatFromIdentifier((byte) in.read());
            if (format.isJson()) in.reset();

            return mapperProvider
                    .getMapper(format)
                    .readTree(in);
        } catch (JacksonException | NullPointerException e) {
            throw new IOException(e);
        }
    }
    public byte[] serializeJsonTree(JsonNode node, DataFormat format) {
        try {
            return mapperProvider
                    .getWriter(format)
                    .writeValueAsBytes(node);
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }
    public byte[] serializeTree(Map<String, Object> idAndObj, DataFormat format)
            throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            if (!format.isJson())
                out.write(format.getIdentifier());

            return mapperProvider
                    .getWriter(format)
                    .writeValueAsBytes(converter.buildJsonTreeFromTree(idAndObj));
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }
}
