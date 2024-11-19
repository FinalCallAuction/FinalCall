/**
 * Custom exception class thrown when an auction with a specific ID is not found.
 * Used to signal error conditions in the controller methods.
 */

package com.finalcall.auctionservice.controller;

public class AuctionNotFoundException extends RuntimeException {
    AuctionNotFoundException(Long id) {
        super("Could not find auction " + id);
    }
}
