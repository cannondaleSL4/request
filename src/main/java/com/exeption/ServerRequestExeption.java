package com.exeption;

/**
 * Created by dima on 11.12.17.
 */
public class ServerRequestExeption extends Exception {
    public ServerRequestExeption() {
        super();
    }

    public ServerRequestExeption (String message) {
        super(message);
    }

    public ServerRequestExeption(Throwable cause){
        super(cause);
    }

    public ServerRequestExeption (String mesage, Throwable cause){
        super(mesage,cause);
    }
}
