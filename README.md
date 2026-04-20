# E-commerce Platform

Микросервисная платформа для интернет-магазина на Spring WebFlux с реактивным программированием.

## Технологии

- **Java 21** - LTS версия с виртуальными потоками
- **Spring Boot 3.2.1** - основной фреймворк
- **Spring WebFlux** - реактивный веб-фреймворк
- **Spring Data R2DBC** - реактивный доступ к PostgreSQL
- **PostgreSQL** - основное хранилище (3 инстанса)
- **Elasticsearch 8.11.0** - полнотекстовый поиск
- **Redis 7** - кэширование и корзины
- **RabbitMQ 3.12** - асинхронная коммуникация
- **Spring Cloud Stream** - абстракция над messaging
- **Flyway** - миграции базы данных
- **Testcontainers** - интеграционное тестирование

## Микросервисы

- **API Gateway** (8080) - маршрутизация, rate limiting, circuit breaker
- **Product Service** (8081) - управление товарами, категории, теги
- **Search Service** (8082) - полнотекстовый поиск по Elasticsearch
- **Cart Service** (8083) - корзины покупок в Redis
- **Inventory Service** (8084) - управление остатками и резервациями
- **Order Service** (8085) - обработка заказов, saga pattern
- **Recommendation Service** (8086) - рекомендательная система

## Быстрый старт

### С помощью Makefile
```bash
# Запустить инфраструктуру
make infra-up

# Собрать все модули
make build

# Запустить все сервисы
make services-up

# Запустить всё сразу
make all-up

# Остановить всё
make all-down

# Проверить статус сервисов
make infra-status

# Проверить здоровье всех сервисов
make check-all
```

### С помощью Docker Compose
```bash
# Запустить инфраструктуру и сервисы
docker compose up -d

# Проверить статус
docker compose ps

# Просмотр логов
docker compose logs -f
```

## Порты сервисов

### Микросервисы
- API Gateway: 8080
- Product Service: 8081
- Search Service: 8082
- Cart Service: 8083
- Inventory Service: 8084
- Order Service: 8085
- Recommendation Service: 8086

### Инфраструктура
- PostgreSQL (Product): 5435
- PostgreSQL (Order): 5433
- PostgreSQL (Inventory): 5434
- Redis: 6379
- RabbitMQ AMQP: 5673
- RabbitMQ Management UI: http://localhost:15673 (ecommerce/ecommerce123)
- Elasticsearch: http://localhost:9200
- Kibana: http://localhost:5601

## API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Дополнительная документация

- [Testing Guide](TESTING_GUIDE.md) - руководство по тестированию API
- [Project Description](PROJECT_DESCRIPTION_RU.md) - подробное техническое описание

## Сборка проекта

```bash
# Сборка без тестов
mvn clean install -DskipTests

# Сборка с тестами
mvn clean install

# Запуск тестов
mvn test
``