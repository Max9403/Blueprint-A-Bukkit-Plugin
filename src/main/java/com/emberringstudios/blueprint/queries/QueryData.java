/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint.queries;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class QueryData {

    private final QueryCallback callback;
    private final String query;

    public QueryData(final String query, final QueryCallback callback) {
        this.query = query;
        this.callback = callback;

    }

    /**
     * @return the callback
     */
    public QueryCallback getCallback() {
        return callback;
    }

    public boolean runError(Exception error) {
        return true;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }
}
