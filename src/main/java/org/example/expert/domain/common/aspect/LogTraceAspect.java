package org.example.expert.domain.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class LogTraceAspect {

    @Pointcut("@annotation(org.example.expert.domain.common.annotation.LogTrace)")
    public void loggerPointcut() {
    }

    @Around("loggerPointcut()")
    public Object doLogTrace(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        final ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper) request;

        String traceId = UUID.randomUUID().toString().substring(0, 8);
        String requestBody = getRequestBody(cachingRequest);
        if (!StringUtils.hasText(requestBody)) {
            requestBody = "empty";
        }
        String method = cachingRequest.getMethod();
        LocalDateTime requestTime = LocalDateTime.now();

        // setAttribute()로 저장한 값을 읽어옴
        Long userId = (Long) cachingRequest.getAttribute("userId");

        log.info(toRequestLog(traceId, method, userId, cachingRequest.getRequestURL(), requestTime, requestBody));

        Object result = proceedingJoinPoint.proceed();  // proxy 객체가 target 객체의 메서드를 호출하고 나온 result
        if (!StringUtils.hasText((CharSequence) result)) {
            result = "empty";
        }

        log.info(toResponseLog(traceId, method, cachingRequest.getRequestURL(), userId, requestTime, result));
        return result;
    }

    // 요청 본문 읽어오기
    private String getRequestBody(ContentCachingRequestWrapper cachingRequest) {
        String requestBody = "";
        try {
            requestBody = new String(cachingRequest.getContentAsByteArray(), cachingRequest.getCharacterEncoding());
        } catch (UnsupportedEncodingException uee) {
            throw new InvalidRequestException("Logging 과정에서 에러가 발생했습니다.");
        }
        return requestBody;
    }

    public String toRequestLog(
        String traceId,
        String method,
        Long userId,
        StringBuffer requestUrl,
        LocalDateTime requestTime,
        String requestBody
    ) {
        return String.format(
            "%n========== HTTP REQUEST LOG ==========%n" +
                "Trace ID        : %s%n" +
                "HTTP Method     : %s%n" +
                "Request URI     : %s%n" +
                "Request User ID : %s%n" +
                "Request Time    : %s%n" +
                "Request Body    : %s%n" +
                "======================================",
            traceId, method, requestUrl, userId, requestTime, requestBody
        );
    }

    public String toResponseLog(
        String traceId,
        String method,
        StringBuffer requestUrl,
        Long userId,
        LocalDateTime requestTime,
        Object result
    ) {
        return String.format(
            "%n========== HTTP RESPONSE LOG ==========%n" +
                "Trace ID        : %s%n" +
                "HTTP Method     : %s%n" +
                "Request URI     : %s%n" +
                "Request User ID : %s%n" +
                "Request Time    : %s%n" +
                "Response Body   : %s%n" +
                "======================================",
            traceId, method, requestUrl, userId, requestTime, result
        );
    }
}
