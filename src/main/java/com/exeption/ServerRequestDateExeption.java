package com.exeption;

/**
 * Created by dima on 11.12.17.
 */
public class ServerRequestDateExeption extends Exception {
    public ServerRequestDateExeption() {
        super();
    }

    public ServerRequestDateExeption (String message) {
        super(message);
    }

    public ServerRequestDateExeption(Throwable cause){
        super(cause);
    }

    public ServerRequestDateExeption (String mesage, Throwable cause){
        super(mesage,cause);
    }
}
