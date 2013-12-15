/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Benjamin
 */
class ResultData {

    private final ConcurrentHashMap<String, String> data = new ConcurrentHashMap();

    public String getKey(final String key) {
        return data.get(key.toLowerCase());
    }

    public void setKey(final String key, final String theData) {
        data.put(key.toLowerCase(), theData);
    }

    public String toString() {
        String result = "";
        Iterator it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            result += (pairs.getKey() + " = " + pairs.getValue()) + "\n";
            it.remove(); // avoids a ConcurrentModificationException
        }
        return result;
    }
}
