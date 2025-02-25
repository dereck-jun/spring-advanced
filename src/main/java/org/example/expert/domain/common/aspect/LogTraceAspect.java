package org.example.expert.domain.common.aspect;

import io.jsonwebtoken.Claims;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogTraceAspect {

    private final JwtUtil jwtUtil;

    @Pointcut("@annotation(org.example.expert.domain.common.annotation.LogTrace)")
    public void loggerPointcut() {
    }

    @Around("loggerPointcut()")
    public Object doLogTrace(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        final ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper) request;

        String traceId = UUID.randomUUID().toString().substring(0, 8);
        String requestBody = getRequestBody(cachingRequest, request);
        if (requestBody == null || StringUtils.isBlank(requestBody)) {
            requestBody = "empty";
        }
        String method = cachingRequest.getMethod();
        LocalDateTime requestTime = LocalDateTime.now();

        // userId를 가져오기 위해 헤더에서 토큰을 가져온 뒤 Claims 추출
        String accessToken = request.getHeader("Authorization");
        String substringBearer = jwtUtil.substringToken(accessToken);
        Claims claims = jwtUtil.extractClaims(substringBearer);

        log.info(toRequestLog(traceId, method, claims.getSubject(), cachingRequest.getRequestURL(), requestTime, requestBody));

        Object result = proceedingJoinPoint.proceed();  // proxy 객체가 target 객체의 메서드를 호출하고 나온 result
        if (result == null || StringUtils.isBlank(result.toString())) {
            result = "empty";
        }

        log.info(toResponseLog(traceId, method, cachingRequest.getRequestURL(), claims.getSubject(), requestTime, result));
        return result;
    }

    private String getRequestBody(ContentCachingRequestWrapper cachingRequest, HttpServletRequest request) {
        String requestBody = "";
        try {
            requestBody = new String(cachingRequest.getContentAsByteArray(), request.getCharacterEncoding());
        } catch (UnsupportedEncodingException uee) {
            throw new InvalidRequestException("Logging 과정에서 에러가 발생했습니다.");
        }
        return requestBody;
    }

    public String toRequestLog(
        String traceId,
        String method,
        String subject,
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
            traceId, method, requestUrl, subject, requestTime, requestBody
        );
    }

    public String toResponseLog(
        String traceId,
        String method,
        StringBuffer requestUrl,
        String subject,
        LocalDateTime requestTime,
        Object result
    ) {
        return String.format(
            "%n========== HTTP REQUEST LOG ==========%n" +
                "Trace ID        : %s%n" +
                "HTTP Method     : %s%n" +
                "Request URI     : %s%n" +
                "Request User ID : %s%n" +
                "Request Time    : %s%n" +
                "Response Body   : %s%n" +
                "======================================",
            traceId, method, requestUrl, subject, requestTime, result
        );
    }
}
