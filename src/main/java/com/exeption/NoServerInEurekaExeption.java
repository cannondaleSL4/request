package com.exeption;

/**
 * Created by dima on 02.06.18.
 */
public class NoServerInEurekaExeption extends Exception {

    public NoServerInEurekaExeption(String message) {
        super(String.format("There is no server %s is available",message));
    }

}
