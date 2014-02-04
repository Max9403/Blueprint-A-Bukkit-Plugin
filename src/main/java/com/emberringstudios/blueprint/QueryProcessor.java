/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.PatPeter.SQLibrary.Database;
import org.bukkit.Bukkit;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class QueryProcessor implements Runnable {

    public static final List<QueryData> queries = new CopyOnWriteArrayList<QueryData>();

    public static void addQuery(QueryData data) {
        queries.add(data);
    }

    public void run() {
        for (QueryData query : queries) {
            try {
                List<ResultData> data = new CopyOnWriteArrayList();
                final Database tempDB = ConfigHandler.getTheDataHub();
                if (tempDB.getConnection() == null || tempDB.getConnection().isClosed()) {
                    if (!tempDB.open()) {
                        Blueprint.error("Could not work with database");
                    }
                }
                try {
                    ResultSet result = tempDB.query(query.getQuery());
                    ResultSetMetaData meta = result.getMetaData();
                    List<String> columns = new CopyOnWriteArrayList();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        columns.add(meta.getColumnName(i));
                    }
                    while (result.next()) {
                        ResultData temp = new ResultData();
                        for (int col = 0; col < columns.size(); col++) {
                            temp.setKey(columns.get(col), result.getString(columns.get(col)));
                        }
                        data.add(temp);
                    }
                    result.close();
                } catch (SQLException ex) {
                    if (query.runError(ex)) {
                        Blueprint.error("An error occured while running a query: \n" + query.getQuery(), ex);
                    } else {
                        queries.remove(query);
                    }
                }
                query.getCallback().result(data);
                queries.remove(query);
            } catch (SQLException ex) {
                Blueprint.error("Error occured while running queries", ex);
            }
        }
        Bukkit.getServer().getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), this);
    }

}
