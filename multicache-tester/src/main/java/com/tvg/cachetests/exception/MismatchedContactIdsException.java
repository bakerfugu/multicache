package com.tvg.cachetests.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Mismatched Ids: path id does not match id in request body")
public class MismatchedContactIdsException extends RuntimeException {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public MismatchedContactIdsException(long pathId, long bodyId) {
        super();
        log.warn(String.format("MismatchedContactIdsException: path id: %d does not match id in request body: %d",
                pathId, bodyId));
    }
}
