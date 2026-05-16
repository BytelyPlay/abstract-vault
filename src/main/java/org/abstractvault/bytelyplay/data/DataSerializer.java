package org.abstractvault.bytelyplay.data;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.abstractvault.bytelyplay.enums.DataFormat;
import org.abstractvault.bytelyplay.utils.MapperProvider;
import org.jetbrains.annotations.NotNull;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.exc.JsonNodeException;
import tools.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class DataSerializer {
    private final MapperProvider mapperProvider;

    @Getter
    private final JsonTreeMapTreeConverter converter;

    public DataSerializer(MapperProvider provider) {
        this.mapperProvider = provider;
        this.converter = new JsonTreeMapTreeConverter(provider);
    }
    public JsonNode deserializeToJsonTree(InputStream rawIn)
            throws IOException {
        try {
            BufferedInputStream in = new BufferedInputStream(rawIn);
            DataFormat format = getFormat(in);

            return mapperProvider
                    .getMapper(format)
                    .readTree(in);
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }

    /**
     * Deserializes the InputStream's contents to a Map tree.
     * @param rawIn the Input Stream.
     * @param keyWithClass The way to map the key to a class,
     *                     an entry can be null or not there if the thing is null anyway.
     * @return The Map Tree.
     * @throws IOException If anything goes wrong, this is thrown.
     */
    public Map<String, Object> deserializeToMapTree(InputStream rawIn,
                                                    Map<String, Class<?>> keyWithClass)
            throws IOException, IllegalArgumentException {
        try {
            BufferedInputStream in = new BufferedInputStream(rawIn);
            DataFormat format = getFormat(in);

            ObjectNode node = mapperProvider
                    .getMapper(format)
                    .readTree(in)
                    .asObject();
            return converter.jsonTreeToMapTree(node, keyWithClass);
        } catch (JsonNodeException e) {
            throw new IOException(
                    "Seems like this Json tree is extremely corrupted, " +
                            "it isn't even an object node.", e);
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }
    public byte[] serializeJsonTree(JsonNode node, DataFormat format)
            throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            writeFormatIdentifier(format, out);

            out.write(
                    mapperProvider
                    .getWriter(format)
                    .writeValueAsBytes(node)
            );
            return out.toByteArray();
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }
    public byte[] serializeMapTree(Map<String, Object> idAndObj, DataFormat format)
            throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            writeFormatIdentifier(format, out);

            out.write(
                    mapperProvider
                    .getWriter(format)
                    .writeValueAsBytes(
                            converter.mapTreeToJsonTree(idAndObj)
                    )
            );
            return out.toByteArray();
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }
    private @NotNull DataFormat getFormat(InputStream in)
            throws IOException {
        in.mark(1);

        DataFormat format = DataFormat.getFormatFromIdentifier((byte) in.read());
        if (format == null)
            throw new IOException("format == null, " +
                    "data may be corrupted since the format identifier isn't recognized.");

        if (format.isJson())
            in.reset();
        return format;
    }
    private void writeFormatIdentifier(DataFormat format, OutputStream out)
            throws IOException {
        if (!format.isJson())
            out.write(format.getIdentifier());
    }
}
