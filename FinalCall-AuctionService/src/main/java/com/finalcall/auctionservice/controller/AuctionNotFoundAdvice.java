/**
 * Handles exceptions of type AuctionNotFoundException.
 * Provides a centralized way to return error messages and HTTP status codes when an auction is not found.
 */
package com.finalcall.auctionservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class AuctionNotFoundAdvice {
    @ResponseBody
    @ExceptionHandler(AuctionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String auctionNotFoundHandler(AuctionNotFoundException ex) {
        return ex.getMessage();
    }

}