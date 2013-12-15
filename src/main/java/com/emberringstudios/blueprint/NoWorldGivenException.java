/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class NoWorldGivenException extends Exception {

    public NoWorldGivenException() {
        super();
    }

    public NoWorldGivenException(String string) {
        super(string);
    }

    public NoWorldGivenException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public NoWorldGivenException(Throwable thrwbl) {
        super(thrwbl);
    }

    protected NoWorldGivenException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }
}
