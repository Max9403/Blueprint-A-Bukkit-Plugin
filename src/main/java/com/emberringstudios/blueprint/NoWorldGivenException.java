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

    /**
     *
     */
    public NoWorldGivenException() {
        super();
    }

    /**
     *
     * @param string
     */
    public NoWorldGivenException(String string) {
        super(string);
    }

    /**
     *
     * @param string
     * @param thrwbl
     */
    public NoWorldGivenException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    /**
     *
     * @param thrwbl
     */
    public NoWorldGivenException(Throwable thrwbl) {
        super(thrwbl);
    }

    /**
     *
     * @param string
     * @param thrwbl
     * @param bln
     * @param bln1
     */
    protected NoWorldGivenException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }
}
