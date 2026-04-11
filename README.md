# E-commerce Platform

Микросервисная платформа для интернет-магазина на Spring WebFlux с Elasticsearch, Redis и RabbitMQ.

## Технологии

- **Java 21**
- **Spring Boot 3.2.1**
- **Spring WebFlux** - реактивное программирование
- **PostgreSQL** - основное хранилище
- **Elasticsearch** - полнотекстовый поиск
- **Redis** - кэширование и корзины
- **RabbitMQ** - асинхронная коммуникация
- **Spring Cloud Stream** - абстракция над messaging

## Архитектура

### Планируемые микросервисы:
- **API Gateway** (8080) - маршрутизация и rate limiting
- **Product Service** (8081) - управление товарами
- **Search Service** (8082) - поиск по Elasticsearch
- **Order Service** (8083) - обработка заказов
- **Cart Service** (8084) - корзина покупок
- **Inventory Service** (8086) - управление остатками
- **Recommendation Service** (8085) - рекомендации

## Быстрый старт

### 1. Запуск инфраструктуры

```bash
# Запуск всех зависимостей
docker-compose up -d

# Проверка статуса
docker-compose ps

# Просмотр логов
docker-compose logs -f
```

### 2. Проверка сервисов

После запуска доступны:
- **PostgreSQL (Product)**: localhost:5432
- **PostgreSQL (Order)**: localhost:5433
- **PostgreSQL (Inventory)**: localhost:5434
- **Redis**: localhost:6379
- **RabbitMQ AMQP**: localhost:5672
- **RabbitMQ Management UI**: http://localhost:15672 (ecommerce/ecommerce123)
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601

### 3. Остановка

```bash
# Остановка всех контейнеров
docker-compose down

# Остановка с удалением volumes (данные будут потеряны!)
docker-compose down -v
```

## Проверка работоспособности

```bash
# PostgreSQL
docker exec -it postgres-product psql -U ecommerce -d product_db -c "SELECT version();"

# Redis
docker exec -it ecommerce-redis redis-cli ping

# RabbitMQ
curl -u ecommerce:ecommerce123 http://localhost:15672/api/overview

# Elasticsearch
curl http://localhost:9200/_cluster/health?pretty
```