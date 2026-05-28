package com.datasetanalytics.analytics.adapters.out.datasetclient;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services.dataset")
public class DatasetClientProperties {
    private String baseUrl = "http://localhost:8081";
    private int connectTimeoutMs = 1000;
    private int timeoutMs = 2000;
    private int retryMaxAttempts = 2;
    private int retryWaitMs = 150;
    private float circuitBreakerFailureRateThreshold = 50;
    private int circuitBreakerSlidingWindowSize = 5;
    private int circuitBreakerMinimumNumberOfCalls = 3;
    private int circuitBreakerWaitDurationMs = 10000;
    private int circuitBreakerPermittedCallsInHalfOpenState = 2;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    public void setRetryMaxAttempts(int retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }

    public int getRetryWaitMs() {
        return retryWaitMs;
    }

    public void setRetryWaitMs(int retryWaitMs) {
        this.retryWaitMs = retryWaitMs;
    }

    public float getCircuitBreakerFailureRateThreshold() {
        return circuitBreakerFailureRateThreshold;
    }

    public void setCircuitBreakerFailureRateThreshold(float circuitBreakerFailureRateThreshold) {
        this.circuitBreakerFailureRateThreshold = circuitBreakerFailureRateThreshold;
    }

    public int getCircuitBreakerSlidingWindowSize() {
        return circuitBreakerSlidingWindowSize;
    }

    public void setCircuitBreakerSlidingWindowSize(int circuitBreakerSlidingWindowSize) {
        this.circuitBreakerSlidingWindowSize = circuitBreakerSlidingWindowSize;
    }

    public int getCircuitBreakerMinimumNumberOfCalls() {
        return circuitBreakerMinimumNumberOfCalls;
    }

    public void setCircuitBreakerMinimumNumberOfCalls(int circuitBreakerMinimumNumberOfCalls) {
        this.circuitBreakerMinimumNumberOfCalls = circuitBreakerMinimumNumberOfCalls;
    }

    public int getCircuitBreakerWaitDurationMs() {
        return circuitBreakerWaitDurationMs;
    }

    public void setCircuitBreakerWaitDurationMs(int circuitBreakerWaitDurationMs) {
        this.circuitBreakerWaitDurationMs = circuitBreakerWaitDurationMs;
    }

    public int getCircuitBreakerPermittedCallsInHalfOpenState() {
        return circuitBreakerPermittedCallsInHalfOpenState;
    }

    public void setCircuitBreakerPermittedCallsInHalfOpenState(int circuitBreakerPermittedCallsInHalfOpenState) {
        this.circuitBreakerPermittedCallsInHalfOpenState = circuitBreakerPermittedCallsInHalfOpenState;
    }
}
