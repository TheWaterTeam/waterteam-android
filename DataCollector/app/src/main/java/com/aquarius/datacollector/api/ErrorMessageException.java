package com.aquarius.datacollector.api;

/**
 * Created by matthewxi on 6/19/17.
 */

public class ErrorMessageException extends Exception {

    private String messageText;

    public ErrorMessageException() {}
    public ErrorMessageException(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageText() {
        return messageText;
    }

}
