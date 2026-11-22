package io.github.hossensyedriadh.internet_banking.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class CoreBankingTransactionNotFoundException extends CoreBankingException {
    @Serial
    private static final long serialVersionUID = -9091546743856678045L;

    public CoreBankingTransactionNotFoundException(String message, Throwable cause) {
        super(message, HttpStatus.NOT_FOUND, cause);
    }
}
