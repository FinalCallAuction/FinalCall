// src/main/java/com/finalcall/catalogueservice/exception/AuctionNotFoundException.java

package com.finalcall.auctionservice.exception;

public class AuctionNotFoundException extends RuntimeException {
    public AuctionNotFoundException(String message) {
        super(message);
    }
}
