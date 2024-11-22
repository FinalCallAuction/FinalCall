// src/main/java/com/finalcall/catalogueservice/exception/AuctionNotActiveException.java

package com.finalcall.auctionservice.exception;

public class AuctionNotActiveException extends RuntimeException {
    public AuctionNotActiveException(String message) {
        super(message);
    }
}
