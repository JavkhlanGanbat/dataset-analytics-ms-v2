# Dataset Analytics Microservices

This is a small Dockerized Spring Boot microservice project for dataset upload and profiling.

## Services

- `api-gateway` — external entry point on port `8080`
- `dataset-service` — CSV ingestion and dataset storage on port `8081`
- `analytics-service` — in-memory statistics calculation on port `8082`
- `frontend` — React demo UI on port `5173`
- `postgres` — Dataset Service database

## Architecture boundary

- Dataset Service owns CSV ingestion, dataset metadata, columns, rows, and PostgreSQL storage.
- Analytics Service owns statistics, data quality scoring, and deterministic insight generation.
- Analytics Service fetches raw rows from Dataset Service through an internal service-to-service HTTP call.
- API Gateway is only for external clients. It routes `/datasets/**` and `/analytics/**`.

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

## Eclipse import

Use:

```text
File -> Import -> Maven -> Existing Maven Projects
```

Select the root folder: `dataset-analytics-ms-v2`.
