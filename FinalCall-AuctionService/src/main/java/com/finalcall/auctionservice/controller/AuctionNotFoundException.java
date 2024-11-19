// src/main/java/com/finalcall/auctionservice/controller/AuctionNotFoundException.java

package com.finalcall.auctionservice.controller;

public class AuctionNotFoundException extends RuntimeException {
    AuctionNotFoundException(Long id) {
        super("Could not find auction " + id);
    }
}
