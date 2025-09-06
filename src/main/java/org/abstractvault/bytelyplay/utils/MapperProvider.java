package org.abstractvault.bytelyplay.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import org.abstractvault.bytelyplay.enums.DataFormat;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class MapperProvider {
    private final ConcurrentHashMap<DataFormat, ObjectMapper> objectMappers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<DataFormat, ObjectWriter> writersForDataFormats = new ConcurrentHashMap<>();
    public MapperProvider() {
        ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
        ObjectMapper smileMapper = new ObjectMapper(new SmileFactory());
        ObjectMapper jsonMapper = new ObjectMapper();

        objectMappers.put(DataFormat.BINARY_CBOR, cborMapper);
        objectMappers.put(DataFormat.BINARY_SMILE, smileMapper);
        objectMappers.put(DataFormat.TEXT_JSON, jsonMapper);
        objectMappers.put(DataFormat.TEXT_PRETTY_JSON, jsonMapper);

        writersForDataFormats.put(DataFormat.BINARY_CBOR, cborMapper.writer());
        writersForDataFormats.put(DataFormat.BINARY_SMILE, smileMapper.writer());
        writersForDataFormats.put(DataFormat.TEXT_JSON, jsonMapper.writer());
        writersForDataFormats.put(DataFormat.TEXT_PRETTY_JSON, jsonMapper.writerWithDefaultPrettyPrinter());
    }

    public ObjectMapper getMapper(DataFormat format) {
        return objectMappers.get(format);
    }
    public ObjectMapper getMapper() {
        return objectMappers.get(DataFormat.TEXT_JSON);
    }
    public ObjectWriter getWriter(DataFormat format) {
        return writersForDataFormats.get(format);
    }
}
