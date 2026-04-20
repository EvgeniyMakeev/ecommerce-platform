.PHONY: help infra-up infra-down infra-logs infra-clean build test rebuild services-up services-down services-logs all-up all-down

help: ## Показать помощь
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

infra-up: ## Запустить инфраструктуру (Docker Compose)
	docker compose up -d postgres-product postgres-order postgres-inventory redis rabbitmq elasticsearch kibana
	@echo "Ожидание запуска сервисов..."
	@sleep 10
	@echo "Инфраструктура запущена!"
	@echo "RabbitMQ Management: http://localhost:15673 (ecommerce/ecommerce123)"
	@echo "Kibana: http://localhost:5601"
	@echo "Elasticsearch: http://localhost:9200"

infra-down: ## Остановить инфраструктуру
	docker compose down

infra-logs: ## Показать логи инфраструктуры
	docker compose logs -f

infra-clean: ## Удалить инфраструктуру и данные
	docker compose down -v
	@echo "Все данные удалены!"

infra-status: ## Показать статус контейнеров
	docker compose ps

build: ## Собрать все модули
	mvn clean install -DskipTests

build-with-tests: ## Собрать все модули с тестами
	mvn clean install

test: ## Запустить тесты
	mvn test

rebuild: ## Пересобрать и перезапустить все сервисы
	mvn clean install -DskipTests
	docker compose up -d --build
	@echo "Сервисы пересобраны и перезапущены!"

# Health checks
check-postgres: ## Проверить PostgreSQL
	@docker exec -it postgres-product psql -U ecommerce -d product_db -c "SELECT 1;"

check-redis: ## Проверить Redis
	@docker exec -it ecommerce-redis redis-cli ping

check-rabbitmq: ## Проверить RabbitMQ
	@curl -u ecommerce:ecommerce123 http://localhost:15673/api/overview 2>/dev/null | grep -q "rabbit" && echo "RabbitMQ: OK" || echo "RabbitMQ: FAIL"

check-elasticsearch: ## Проверить Elasticsearch
	@curl -s http://localhost:9200/_cluster/health | grep -q "green\|yellow" && echo "Elasticsearch: OK" || echo "Elasticsearch: FAIL"

check-all: check-postgres check-redis check-rabbitmq check-elasticsearch ## Проверить все сервисы

services-up: ## Запустить все Spring Boot сервисы
	docker compose up -d product-service search-service cart-service inventory-service order-service api-gateway recommendation-service
	@echo "Все сервисы запущены!"
	@echo "API Gateway: http://localhost:8080"
	@echo "Product Service: http://localhost:8081"
	@echo "Search Service: http://localhost:8082"
	@echo "Cart Service: http://localhost:8083"
	@echo "Inventory Service: http://localhost:8084"
	@echo "Order Service: http://localhost:8085"
	@echo "Recommendation Service: http://localhost:8086"

services-down: ## Остановить все Spring Boot сервисы
	docker compose stop product-service search-service cart-service inventory-service order-service api-gateway recommendation-service

services-logs: ## Показать логи всех сервисов
	docker compose logs -f product-service search-service cart-service inventory-service order-service api-gateway recommendation-service

all-up: ## Запустить инфраструктуру и все сервисы
	docker compose up -d
	@echo "Ожидание запуска сервисов..."
	@sleep 15
	@echo "Все сервисы запущены!"
	@echo "API Gateway: http://localhost:8080"
	@echo "Product Service: http://localhost:8081"
	@echo "Search Service: http://localhost:8082"
	@echo "Cart Service: http://localhost:8083"
	@echo "Inventory Service: http://localhost:8084"
	@echo "Order Service: http://localhost:8085"
	@echo "Recommendation Service: http://localhost:8086"
	@echo "RabbitMQ Management: http://localhost:15673 (ecommerce/ecommerce123)"
	@echo "Kibana: http://localhost:5601"
	@echo "Elasticsearch: http://localhost:9200"

all-down: ## Остановить все сервисы и инфраструктуру
	docker compose down
