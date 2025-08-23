package org.abstractvault.bytelyplay.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.abstractvault.bytelyplay.Getter;
import org.abstractvault.bytelyplay.Setter;
import org.abstractvault.bytelyplay.utils.GetterSetter;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JsonDataSetter {
    private final ArrayList<GetterSetter<Object>> gettersSetters;

    public static class Builder {
        private ArrayList<GetterSetter<Object>> gettersSetters = new ArrayList<>();
        public JsonDataSetter build() {
            return new JsonDataSetter(this);
        }
        public Builder getterSetter(Getter<Object> getter, Setter<Object> setter) {
            gettersSetters.add(new GetterSetter<>(getter, setter));
            return this;
        }
    }
    private JsonDataSetter(Builder builder) {
        this.gettersSetters = builder.gettersSetters;
    }
    public void save() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();
            for (int i = 0; i < gettersSetters.size(); i++) {
                GetterSetter<Object> getterSetter = gettersSetters.get(i);
                Getter<Object> getter = getterSetter.getter;
                Object got = getter.get();
                JsonNode getJsonNode = mapper.readTree(mapper.writeValueAsString(got));
                if (getJsonNode.isObject()) {
                    ObjectNode getNode = (ObjectNode) getJsonNode;
                    getNode.put("class", got.getClass().getName());
                    rootNode.set(String.valueOf(i), getNode);
                } else {
                    log.error("Couldn't save all data, getJsonNode.isObject() is false.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void load(Path jsonFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new FileInputStream(jsonFile.toString()));
            for (int i = 0; i < gettersSetters.size(); i++) {
                JsonNode subNode = rootNode.get(String.valueOf(i));
                if (subNode.isObject()) {
                    ObjectNode subObjectNode = (ObjectNode) subNode;
                    GetterSetter<Object> getterSetter = gettersSetters.get(i);
                    Setter<Object> setter = getterSetter.setter;
                    Class<?> clazz = Class.forName(subNode.get("class").asText());
                    subObjectNode.remove("class");
                    Object obj = mapper.treeToValue(subNode, clazz);
                    setter.set(obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
