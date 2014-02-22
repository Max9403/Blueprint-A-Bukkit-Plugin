/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint.queries;

import java.util.List;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public interface QueryCallback {

    void result(List<ResultData> result);
}
