package io.github.hossensyedriadh.cbs.aspect;

import io.github.hossensyedriadh.cbs.enumerator.CustomHttpAttributes;
import io.github.hossensyedriadh.cbs.util.HttpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Log4j2
@Aspect
@Component
public class LoggingAspect {
    @Pointcut("execution(* io.github.hossensyedriadh.cbs.*.*.*.*.*(..))")
    public void executeForAllControllers() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.InitBinder)")
    public void excludeInitBinders() {
    }

    @Pointcut("@annotation(org.springframework.beans.factory.annotation.Value)")
    public void excludeSetterMethods() {
    }

    @Pointcut("@annotation(jakarta.annotation.PostConstruct)")
    public void excludePostConstructs() {
    }

    @Before("executeForAllControllers() && !excludeInitBinders() && !excludeSetterMethods() && !excludePostConstructs()")
    public void inbound(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            HttpUtil httpUtil = new HttpUtil(request);
            log.info("Inbound request -> Src: {} | Request-URI: {} | Request-ID: {} | Method: {}.", httpUtil.parseClientAddress(), String.join(" ", request.getMethod(), request.getRequestURI()),
                    request.getAttribute(CustomHttpAttributes.X_REQUEST_ID.getAttributeName()), methodSignature);
        }
    }

    @AfterReturning(value = "executeForAllControllers() && !excludeInitBinders() && !excludeSetterMethods() && !excludePostConstructs()")
    public void onSuccess(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = methodSignature.getReturnType();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpUtil httpUtil = new HttpUtil(attributes.getRequest());
            log.info("Success sample -> Dest: {} | Request-ID: {} | Method: {} | returned {}.", httpUtil.parseClientAddress(),
                    attributes.getRequest().getAttribute(CustomHttpAttributes.X_REQUEST_ID.getAttributeName()), methodSignature, returnType);
        }
    }

    @AfterThrowing(value = "executeForAllControllers() && !excludeInitBinders() && !excludeSetterMethods() && !excludePostConstructs()", throwing = "throwable")
    public void onFailure(JoinPoint joinPoint, Throwable throwable) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = methodSignature.getReturnType();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpUtil httpUtil = new HttpUtil(attributes.getRequest());
            log.error("Error sample -> Dest: {} | Request-ID: {} | Method: {} | Return Type: {} has thrown {}.", httpUtil.parseClientAddress(),
                    attributes.getRequest().getAttribute(CustomHttpAttributes.X_REQUEST_ID.getAttributeName()), methodSignature, returnType, throwable);
        }
    }
}
