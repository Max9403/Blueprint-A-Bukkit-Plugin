package com.emberringstudios.blueprint;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
class ResultData {

    private final ConcurrentHashMap<String, String> data = new ConcurrentHashMap();

    public String getKey(final String key) {
        return data.get(key.toLowerCase());
    }

    public void setKey(final String key, final String theData) {
        data.put(key.toLowerCase(), theData);
    }

    @Override
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
