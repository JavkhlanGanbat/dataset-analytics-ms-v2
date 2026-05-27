# Dataset Analytics Microservices

This is a small Dockerized Spring Boot microservice project for dataset upload and profiling.

## Services

- `api-gateway` — external entry point on port `8080`
- `dataset-service` — CSV ingestion and dataset storage on internal port `8081`
- `analytics-service` — in-memory statistics calculation on internal port `8082`
- `frontend` — React demo UI on port `5173`
- `postgres` — Dataset Service database

## Architecture boundary

- Dataset Service owns CSV ingestion, dataset metadata, columns, rows, and PostgreSQL storage.
- Analytics Service owns statistics, data quality scoring, and deterministic insight generation.
- Analytics Service fetches raw rows from Dataset Service through an internal service-to-service HTTP call.
- API Gateway is only for external clients. It routes `/datasets/**` and `/analytics/**`.
- The frontend calls only the API Gateway. Dataset Service and Analytics Service are reachable by service name inside Docker Compose, not through browser-facing URLs.

## Run

```bash
mvn clean package
docker compose up --build
```

Open the UI at:

```text
http://localhost:5173
```

## Frontend local development

```bash
cd frontend
npm install
npm run dev
```

The frontend uses `VITE_API_GATEWAY_URL` and defaults to `http://localhost:8080`.

```bash
VITE_API_GATEWAY_URL=http://localhost:8080 npm run dev
```

## Smoke tests

```bash
curl http://localhost:8080/datasets/ping
curl http://localhost:8080/analytics/ping
curl http://localhost:8080/actuator/health
```

Dataset Service and Analytics Service are intentionally not published to the host in the default Compose setup. Use the gateway URLs above for external requests.

## Upload sample CSV

```bash
curl -X POST http://localhost:8080/datasets/upload \
  -F "name=Sales Dataset" \
  -F "file=@samples/sales.csv"
```

The upload response includes the dataset `id`, name, column count, row count, and creation time.

## Dataset API

```bash
curl http://localhost:8080/datasets
curl http://localhost:8080/datasets/1
```

`GET /datasets` returns dataset summaries only. `GET /datasets/{datasetId}` returns metadata and columns, not all row data.

## Analytics API

```bash
curl http://localhost:8080/analytics/datasets/1/summary
curl http://localhost:8080/analytics/datasets/1/profile
```

The profile response includes per-column statistics, inferred column types, data quality score, and generated insights.

## Resilience and errors

Dataset upload validation returns consistent JSON errors for common bad inputs:

- missing or blank dataset name
- missing, empty, or non-CSV file
- empty CSV content
- CSV files with headers but no data rows
- inconsistent row lengths

Example:

```json
{
  "timestamp": "2026-05-28T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "CSV file must contain at least one data row.",
  "path": "/datasets/upload"
}
```

Analytics Service uses timeouts, one retry for transient failures, and a Resilience4j circuit breaker when calling Dataset Service. This matters because in a microservices system a downstream service can be slow, unavailable, or returning errors independently of the caller. The circuit breaker prevents repeated slow failures from tying up Analytics Service and returns a clean `503` while Dataset Service recovers.

Downstream error behavior:

- Dataset not found from Dataset Service -> Analytics returns `404`
- Dataset Service bad request -> Analytics returns `400`
- Dataset Service unavailable or circuit open -> Analytics returns `503`
- Dataset Service timeout -> Analytics returns `504`
- Unexpected errors -> clean `500` without stack traces

To test service-unavailable behavior:

```bash
docker compose stop dataset-service
curl http://localhost:8080/analytics/datasets/1/profile
docker compose start dataset-service
```

Initial requests may return `504` if the call times out. After repeated failures the circuit breaker should fail fast with a `503` response for a short period, then allow trial calls again.

## Eclipse import

Use:

```text
File -> Import -> Maven -> Existing Maven Projects
```

Select the root folder: `dataset-analytics-ms-v2`.
