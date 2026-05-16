package org.abstractvault.bytelyplay.data;

import org.abstractvault.bytelyplay.utils.MapperProvider;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonTreeMapTreeConverter {
    private final MapperProvider mapperProvider;

    public JsonTreeMapTreeConverter(MapperProvider provider) {
        this.mapperProvider = provider;
    }

    public ObjectNode mapTreeToJsonTree(Map<String, Object> idAndObj)
            throws IOException {
        try {
            ObjectNode rootNode = mapperProvider.getMapper().createObjectNode();

            for (Map.Entry<String, Object> entry : idAndObj.entrySet())
                rootNode.putPOJO(entry.getKey(), entry.getValue());
            return rootNode;
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }

    /**
     * Converts a Json tree into a Map tree.
     * @param jsonTree The JSON tree
     * @param keyWithClass The key to class map.
     * @return The Map Tree
     *
     * @throws IllegalArgumentException This is thrown when a class for a key is null, when it shouldn't be.
     * @throws IOException This is thrown when a JacksonException is thrown.
     */
    public Map<String, Object> jsonTreeToMapTree(ObjectNode jsonTree,
                                                 Map<String, Class<?>> keyWithClass)
            throws IllegalArgumentException, IOException {
        try {
            HashMap<String, Object> mapTree = new HashMap<>();
            ObjectMapper mapper = mapperProvider.getMapper();

            for (Map.Entry<String, JsonNode> entry : jsonTree.properties()) {
                Class<?> classForKey = keyWithClass.get(entry.getKey());
                JsonNode value = entry.getValue();

                if (classForKey == null && !(entry.getValue() instanceof NullNode)) {
                    throw new IllegalArgumentException(
                            "Class is null in keyWithClass when it shouldn't be, the other possibility is, " +
                                    "there is an entry in the Json file that shouldn't be there."
                    );
                }
                mapTree.put(
                        entry.getKey(),
                        value == null
                                ? mapper.nullNode()
                                : mapper.treeToValue(value, classForKey)
                );
            }
            return mapTree;
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }
}
