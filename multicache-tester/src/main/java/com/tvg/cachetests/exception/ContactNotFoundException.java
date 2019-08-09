package com.tvg.cachetests.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Contact Not Found")
public class ContactNotFoundException extends RuntimeException {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ContactNotFoundException(long pathId) {
        super();
        log.warn(String.format("ContactNotFoundException: path id: %d cannot be found",
                pathId));
    }
}
