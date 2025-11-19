# TREEdu

## Observability (Prometheus + Grafana)

- Prometheus UI: http://localhost:9090
- Grafana UI: http://localhost:3001

### Datasource
Grafana → Connections → Prometheus → URL: `http://prometheus:9090`

### Scrape targets (inside Docker network)
- `api-gateway:9005`, `auth-service:9005`
- `application-service:8080`, `monitoring-service:8080`, `materials-service:8080`, `user-service:8080`, `personal-flashcard-service:8080`,`testing-service`, `payment-service`,`management-service`

### Verify endpoints
```bash
curl -s http://api-gateway:9005/actuator/prometheus | head
for s in application-service monitoring-service materials-service user-service personal-flashcard-service payment-service management-service ; do
  curl -s http://$s:8080/actuator/prometheus | head
done