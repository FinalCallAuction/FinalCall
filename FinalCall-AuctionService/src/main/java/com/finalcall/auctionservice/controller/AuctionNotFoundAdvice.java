// src/main/java/com/finalcall/auctionservice/controller/AuctionNotFoundAdvice.java

package com.finalcall.auctionservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class AuctionNotFoundAdvice {
    @ResponseBody
    @ExceptionHandler(AuctionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String auctionNotFoundHandler(AuctionNotFoundException ex) {
        return ex.getMessage();
    }
}
