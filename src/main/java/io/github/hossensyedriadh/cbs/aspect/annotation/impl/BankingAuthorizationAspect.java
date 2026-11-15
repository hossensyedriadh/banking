package io.github.hossensyedriadh.cbs.aspect.annotation.impl;

import io.github.hossensyedriadh.cbs.aspect.annotation.AuthorizeAccountAccess;
import io.github.hossensyedriadh.cbs.authorization.api.service.BankingAuthorizationService;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Aspect
@Component
public class BankingAuthorizationAspect {
    private final BankingAuthorizationService bankingAuthorizationService;

    @Autowired
    public BankingAuthorizationAspect(BankingAuthorizationService bankingAuthorizationService) {
        this.bankingAuthorizationService = bankingAuthorizationService;
    }

    @Before("@annotation(authorizeAccountAccess)")
    public void authorizeAccount(JoinPoint joinPoint, AuthorizeAccountAccess authorizeAccountAccess) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        String[] paramNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        String methodName = methodSignature.getName();

        String accountIdParam = authorizeAccountAccess.accountIdParam();
        String cardIdParam = authorizeAccountAccess.cardIdParam();

        if (accountIdParam.isBlank() && cardIdParam.isBlank()) {
            log.error("@AuthorizeAccountAccess must specify accountIdParam or cardIdParam {}", methodName);
            throw new IllegalStateException("Account ID or Card ID is required for authorization");
        }

        if (!accountIdParam.isBlank() && !cardIdParam.isBlank()) {
            Long accountId = this.getParameterValue(accountIdParam, paramNames, args, methodName);
            Long cardId = this.getParameterValue(cardIdParam, paramNames, args, methodName);
            this.bankingAuthorizationService.authorizeCardAccess(accountId, cardId);
        } else if (!accountIdParam.isBlank()) {
            Long accountId = this.getParameterValue(accountIdParam, paramNames, args, methodName);
            this.bankingAuthorizationService.authorizeAccountAccess(accountId);
        } else {
            Long cardId = this.getParameterValue(cardIdParam, paramNames, args, methodName);
            this.bankingAuthorizationService.authorizeAccountAccess(cardId);
        }
    }

    private Long getParameterValue(String parameterName, String[] paramNames, Object[] args, String methodName) {
        for (int i = 0; i < paramNames.length; i++) {
            if (parameterName.equals(paramNames[i])) {
                if (!(args[i] instanceof Long value)) {
                    log.error("Parameter {} must be a Long in method {}", parameterName, methodName);
                    throw new IllegalStateException("Invalid parameter type");
                }

                return value;
            }
        }

        log.error("Parameter ({}) not found in method: {}", parameterName, methodName);
        throw new IllegalStateException("Required parameter not found for authorization");
    }
}
