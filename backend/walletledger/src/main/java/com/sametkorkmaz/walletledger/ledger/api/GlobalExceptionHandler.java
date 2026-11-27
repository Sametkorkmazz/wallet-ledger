package com.sametkorkmaz.walletledger.ledger.api;

import com.sametkorkmaz.walletledger.ledger.domain.LedgerRuleException;
import com.sametkorkmaz.walletledger.shared.domain.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(LedgerRuleException.class)
    public ProblemDetail handleLedgerRuleException(LedgerRuleException ex) {
        log.warn("Ledger rule violation = {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problemDetail.setTitle(ex.getErrorCode().getTitle());
        problemDetail.setProperty("errorCode", ex.getErrorCode().getCode());
        return problemDetail;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        log.warn("Resource not found = {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Resource not found");
        problemDetail.setProperty("errorCode", ex.getCode());
        return problemDetail;
    }
}
