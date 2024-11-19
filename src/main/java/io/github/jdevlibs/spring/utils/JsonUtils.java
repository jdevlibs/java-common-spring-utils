/*
 * ---------------------------------------------------------------------------
 *  Copyright (c)  2023-2023.  the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ---------------------------------------------------------------------------
 */

package io.github.jdevlibs.spring.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jdevlibs.spring.ConfigProperties;
import io.github.jdevlibs.utils.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author supot.jdev
 * @version 1.0
 */
public final class JsonUtils {
    private static final JsonMapper mapper;
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private JsonUtils() {}

    static {
        mapper = JsonMapper.builder().build();
        mapperConfig();
    }

    private static void mapperConfig() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDateFormat(new SimpleDateFormat(ConfigProperties.getJsonDateFormat()));
        mapper.registerModule(new JavaTimeModule());

        // Only serialization class attributes or member ignore all get/set and isMethod.
        mapper.setVisibility(mapper.getVisibilityChecker()
                .withFieldVisibility(Visibility.ANY)
                .withGetterVisibility(Visibility.NONE)
                .withSetterVisibility(Visibility.NONE)
                .withCreatorVisibility(Visibility.NONE)
                .withIsGetterVisibility(Visibility.NONE));
    }

    /**
     * Serialize object value to JSON data format
     * @param obj The object to serialize.
     * @return Serialize value as string
     */
    public static String json(Object obj) {
        return json(obj, false);
    }

    /**
     * Serialize object value to JSON data format
     * @param obj The object to convert.
     * @param prettyOutput Write out pretty readable.
     * @return Serialize value as string
     */
    public static String json(Object obj, boolean prettyOutput) {
        String jsonData = null;
        try {
            if (prettyOutput) {
                jsonData = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            } else {
                jsonData = mapper.writeValueAsString(obj);
            }
        } catch (JsonProcessingException ex) {
            logger.error("json", ex);
        }

        return jsonData;
    }

    /**
     * Deserialize JSON data format to Object class.
     * @param <T> The type of class for deserializing.
     * @param json JSON data format.
     * @param clazz The class for deserializing.
     * @return Object class
     */
    public static <T> T model(String json, Class<T> clazz) {
        if (Validators.isEmpty(json)) {
            return null;
        }
        return model(json.getBytes(), clazz);
    }

    /**
     * <pre>
     * Deserialize JSON data format to Object class.
     * In case need full generic type information
     * </pre>
     * @param <T> The type of class for deserializing.
     * @param json JSON data format.
     * @param type JavaType of deserialize.
     * @return Object class
     */
    public static <T> T model(String json, JavaType type) {
        if (Validators.isEmptyOne(json, type)) {
            return null;
        }
        return model(json.getBytes(), type);
    }

    /**
     * Deserialize JSON data format to Object class.
     * @param json JSON String data format.
     * @param type TypeReference<T> type
     * @return Object class
     * @param <T> Generic type
     */
    public static <T> T model(String json, TypeReference<T> type) {
        if (Validators.isEmptyOne(json, type)) {
            return null;
        }
        return model(json.getBytes(), type);
    }

    /**
     * Deserialize JSON data format to Object class.
     * @param jsonData JSON byte[] data format.
     * @param type TypeReference<T> type
     * @return Object class
     * @param <T> Generic type
     */
    public static <T> T model(byte[] jsonData, TypeReference<T> type) {
        try {
            if (Validators.isEmpty(jsonData)) {
                return null;
            }

            return mapper.readValue(jsonData, type);
        } catch (IOException ex) {
            logger.error("model", ex);
        }

        return null;
    }

    /**
     * Deserialize JSON data format to List of a model
     * @param <T> The type of class for deserializing.
     * @param json JSON data format must be arrayed.
     * @param clazz The class for deserializing.
     * @return Object class
     */
    public static <T> List<T> models(String json, Class<T> clazz) {
        if (Validators.isEmpty(json)) {
            return Collections.emptyList();
        }
        return models(json.getBytes(), clazz);
    }

    /**
     * Serialize object value to JSON data format
     * @param obj The object to serialize.
     * @return Serialize value as byte[] arrays
     */
    public static byte[] jsonAsBytes(Object obj) {
        byte[] jsonData = null;
        try {
            jsonData = mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException ex) {
            logger.error("jsonAsBytes", ex);
        }

        return jsonData;
    }

    /**
     * Deserialize JSON data format to Object class.
     * @param <T> The type of class for deserializing.
     * @param jsonData JSON byte[] data format.
     * @param clazz The class for deserializing.
     * @return Object class
     */
    public static <T> T model(byte[] jsonData, Class<T> clazz) {
        try {
            if (Validators.isEmpty(jsonData)) {
                return null;
            }

            return mapper.readValue(jsonData, clazz);
        } catch (IOException ex) {
            logger.error("model", ex);
        }

        return null;
    }

    /**
     * <pre>
     * Deserialize JSON data format to Object class.
     * In case need full generic type information
     * </pre>
     * @param <T> The type of class for deserializing.
     * @param jsonData JSON byte[] data format.
     * @param type JavaType of deserialize.
     * @return Object class
     */
    public static <T> T model(byte[] jsonData, JavaType type) {
        try {
            if (Validators.isEmpty(jsonData)) {
                return null;
            }

            return mapper.readValue(jsonData, type);
        } catch (IOException ex) {
            logger.error("model", ex);
        }

        return null;
    }

    /**
     * Deserialize JSON data format to List of a model.
     * @param <T> The type of class for deserializing.
     * @param jsonData JSON byte[] data format.
     * @param clazz The class for deserializing.
     * @return Object class
     */
    public static <T> List<T> models(byte[] jsonData, Class<T> clazz) {
        try {
            if (Validators.isEmpty(jsonData)) {
                return Collections.emptyList();
            }

            JavaType javaType = collectionType(List.class, clazz);
            return mapper.readValue(jsonData, javaType);
        } catch (IOException ex) {
            logger.error("models", ex);
        }

        return Collections.emptyList();
    }

    /**
     * Deserialize JSON data format to Map class.
     * @param json json JSON data format.
     * @return Deserialize result of Map.
     */
    public static Map<String, Object> map(String json) {
        if (Validators.isEmpty(json)) {
            return Collections.emptyMap();
        }
        return map(json.getBytes());
    }

    /**
     * Deserialize JSON data format to Map class.
     * @param jsonData JSON byte[] data format.
     * @return Deserialize result of Map.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> map(byte[] jsonData) {
        Map<String, Object> maps = JsonUtils.model(jsonData, Map.class);
        if (Validators.isNull(maps)) {
            maps = new HashMap<>();
        }

        return maps;
    }

    /**
     * Deserialize JSON data format to Map class.
     * @param <K> Map key type
     * @param <V> Map value type
     * @param json JSON data format.
     * @param keyClass Map key class
     * @param valueClass Map value class
     * @return Deserialize result of Map.
     */
    public static <K, V> Map<K, V> map(String json, Class<K> keyClass, Class<V> valueClass) {
        if (Validators.isEmpty(json) || Validators.isNullOne(keyClass, valueClass)) {
            return Collections.emptyMap();
        }
        return map(json.getBytes(), keyClass, valueClass);
    }

    /**
     * Deserialize JSON data format to Map class.
     * @param <K> Map key type
     * @param <V> Map value type
     * @param jsonData JSON byte[] data format.
     * @param keyClass Map key class
     * @param valueClass Map value class
     * @return Deserialize result of Map.
     */
    public static <K, V> Map<K, V> map(byte[] jsonData, Class<K> keyClass, Class<V> valueClass) {
        try {
            if (Validators.isEmpty(jsonData)
                    || Validators.isNullOne(keyClass, valueClass)) {
                return Collections.emptyMap();
            }

            JavaType javaType = mapType(Map.class, keyClass, valueClass);
            return mapper.readValue(jsonData, javaType);
        } catch (IOException ex) {
            logger.error("map", ex);
        }

        return Collections.emptyMap();
    }

    /**
     * Create CollectionType {@link CollectionType} instance.
     * @param collClass The Collection class type
     * @param elementClazz The element of collection class type
     * @return {@link CollectionType}
     */
    @SuppressWarnings("rawtypes")
    public static JavaType collectionType(Class<? extends Collection> collClass, Class<?> elementClazz) {
        return mapper.getTypeFactory().constructCollectionType(collClass, elementClazz);
    }

    /**
     * Create MapType {@link MapType} instance.
     * @param mapClass The class of map type
     * @param keyClass The class of map key
     * @param valueClass The class of map value
     * @return {@link MapType}
     */
    @SuppressWarnings("rawtypes")
    public static JavaType mapType(Class<? extends Map> mapClass,
                                   Class<?> keyClass, Class<?> valueClass) {
        return mapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }
}