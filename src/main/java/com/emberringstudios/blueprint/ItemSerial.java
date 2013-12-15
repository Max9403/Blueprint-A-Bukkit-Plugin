/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Benjamin
 */
public class ItemSerial {

    public static List<Map<String, Object>> serializeItemList(ConfigurationSerializable[] list) {
        List<Map<String, Object>> returnVal = new ArrayList<Map<String, Object>>();
        for (ConfigurationSerializable cs : list) {

            returnVal.add(cs == null ? null : serialize(cs));
        }
        return returnVal;
    }

    public static Map<String, Object> serialize(ConfigurationSerializable cs) {
        Map<String, Object> serialized = recreateMap(cs.serialize());
        for (Entry<String, Object> entry : serialized.entrySet()) {
            if (entry.getValue() instanceof ConfigurationSerializable) {
                entry.setValue(serialize((ConfigurationSerializable) entry.getValue()));
            }
        }
        serialized.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(cs.getClass()));
        return serialized;
    }

    public static Map<String, Object> recreateMap(Map<String, Object> original) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Entry<String, Object> entry : original.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    // Time for Deserialization
    @SuppressWarnings("unchecked")
    public static ConfigurationSerializable deserialize(Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
// Check if any of its sub-maps are ConfigurationSerializable. They need to be done first.
            if (entry.getValue() instanceof Map && ((Map) entry.getValue()).containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                entry.setValue(deserialize((Map) entry.getValue()));
            }
        }
        return ConfigurationSerialization.deserializeObject(map);
    }

    public static List<ConfigurationSerializable> deserializeItemList(List<Map<String, Object>> itemList) {
        List<ConfigurationSerializable> returnVal = new ArrayList<ConfigurationSerializable>();
        for (Map<String, Object> map : itemList) {
            returnVal.add(map == null ? null : deserialize(map));
        }
        return returnVal;
    }
}
