package com.example.analytics.adapters.out.datasetclient;

import com.example.analytics.core.domain.DatasetColumn;
import com.example.analytics.core.domain.DatasetData;
import com.example.analytics.core.domain.DatasetNotFoundException;
import com.example.analytics.core.domain.DatasetServiceBadRequestException;
import com.example.analytics.core.domain.DatasetServiceTimeoutException;
import com.example.analytics.core.domain.DatasetServiceUnavailableException;
import com.example.analytics.core.port.out.DatasetClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class HttpDatasetClient implements DatasetClient {
    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public HttpDatasetClient(RestClient.Builder builder, DatasetClientProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()));

        this.restClient = builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
        this.circuitBreaker = CircuitBreaker.of("dataset-service", circuitBreakerConfig(properties));
        this.retry = Retry.of("dataset-service", retryConfig(properties));
    }

    @Override
    public DatasetData fetchDatasetData(Long datasetId) {
        Supplier<DatasetData> supplier = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                Retry.decorateSupplier(retry, () -> fetchDatasetDataOnce(datasetId))
        );
        return supplier.get();
    }

    private DatasetData fetchDatasetDataOnce(Long datasetId) {
        InternalDatasetDataResponse response;
        try {
            response = restClient.get()
                    .uri("/internal/datasets/{datasetId}/data", datasetId)
                    .retrieve()
                    .body(InternalDatasetDataResponse.class);
        } catch (RestClientResponseException exception) {
            throw mapResponseException(datasetId, exception);
        } catch (ResourceAccessException exception) {
            if (causedBy(exception, SocketTimeoutException.class)
                    || causedBy(exception, HttpTimeoutException.class)) {
                throw new DatasetServiceTimeoutException(
                        "Dataset Service timed out. Please try again shortly.",
                        exception
                );
            }
            throw new DatasetServiceUnavailableException(
                    "Dataset Service is temporarily unavailable. Please try again shortly.",
                    exception
            );
        }

        if (response == null) {
            throw new DatasetServiceUnavailableException("Dataset Service returned an empty response.");
        }

        List<DatasetColumn> columns = response.columns() == null
                ? List.of()
                : response.columns().stream()
                        .map(column -> new DatasetColumn(column.name(), column.type()))
                        .toList();
        List<Map<String, String>> rows = response.rows() == null ? List.of() : response.rows();

        return new DatasetData(response.id(), response.name(), columns, rows);
    }

    private RuntimeException mapResponseException(Long datasetId, RestClientResponseException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status == HttpStatus.NOT_FOUND) {
            return new DatasetNotFoundException(datasetId);
        }
        if (status == HttpStatus.BAD_REQUEST) {
            return new DatasetServiceBadRequestException("Dataset Service rejected the request.");
        }
        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return new DatasetServiceTimeoutException(
                    "Dataset Service timed out. Please try again shortly.",
                    exception
            );
        }
        if (status == HttpStatus.SERVICE_UNAVAILABLE
                || exception.getStatusCode().is5xxServerError()) {
            return new DatasetServiceUnavailableException(
                    "Dataset Service is temporarily unavailable. Please try again shortly.",
                    exception
            );
        }
        return new DatasetServiceUnavailableException(
                "Dataset Service returned an unexpected response.",
                exception
        );
    }

    private CircuitBreakerConfig circuitBreakerConfig(DatasetClientProperties properties) {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(properties.getCircuitBreakerFailureRateThreshold())
                .slidingWindowSize(properties.getCircuitBreakerSlidingWindowSize())
                .minimumNumberOfCalls(properties.getCircuitBreakerMinimumNumberOfCalls())
                .waitDurationInOpenState(Duration.ofMillis(properties.getCircuitBreakerWaitDurationMs()))
                .permittedNumberOfCallsInHalfOpenState(properties.getCircuitBreakerPermittedCallsInHalfOpenState())
                .ignoreExceptions(DatasetNotFoundException.class, DatasetServiceBadRequestException.class)
                .build();
    }

    private RetryConfig retryConfig(DatasetClientProperties properties) {
        return RetryConfig.custom()
                .maxAttempts(Math.max(1, properties.getRetryMaxAttempts()))
                .waitDuration(Duration.ofMillis(properties.getRetryWaitMs()))
                .retryOnException(this::isRetryable)
                .build();
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof DatasetServiceUnavailableException
                || throwable instanceof DatasetServiceTimeoutException;
    }

    private boolean causedBy(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private record InternalDatasetDataResponse(
            Long id,
            String name,
            List<ColumnResponse> columns,
            List<Map<String, String>> rows
    ) {}

    private record ColumnResponse(String name, String type) {}
}
